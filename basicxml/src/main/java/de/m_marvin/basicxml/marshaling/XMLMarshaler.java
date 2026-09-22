package de.m_marvin.basicxml.marshaling;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.m_marvin.basicxml.XmlException;
import de.m_marvin.basicxml.XmlStream.DescType;
import de.m_marvin.basicxml.XmlStream.ElementDescriptor;
import de.m_marvin.basicxml.XmlWriter;
import de.m_marvin.basicxml.marshaling.annotations.XmlCData;
import de.m_marvin.basicxml.marshaling.annotations.XmlField;
import de.m_marvin.basicxml.marshaling.annotations.XmlRootType;
import de.m_marvin.basicxml.marshaling.annotations.XmlType;
import de.m_marvin.basicxml.marshaling.internal.NamespaceMap;
import de.m_marvin.basicxml.marshaling.internal.NamespaceMap.Entry;
import de.m_marvin.basicxml.marshaling.internal.NamespaceMap.Key;
import de.m_marvin.basicxml.marshaling.internal.XmlClassField;
import de.m_marvin.basicxml.marshaling.internal.XmlClassType;

public class XmlMarshaler {

	private final Map<Class<?>, XmlClassType<?, ?>> types = new HashMap<>();
	
	public XmlMarshaler(boolean ignoreNamespaces, Class<?>... types) {
		for (Class<?> type : types) {
			resolveTypeObjects(type, null, ignoreNamespaces);
		}
	}
	
	private void resolveTypeObjects(Class<?> type, Class<?> parent, boolean ignoreNamespace) {
		var typeObj = XmlClassType.makeFromClass(type, parent, ignoreNamespace);
		this.types.put(type, typeObj);
		for (Class<?> subTypes : typeObj.subTypes()) {
			resolveTypeObjects(subTypes, type, ignoreNamespace);
		}
	}

	public <T> void marshal(XmlWriter xmlStream, T object) throws XmlMarshalingException, IOException, XmlException {
		marshal(xmlStream, object, true);
	}
	
	public <T> void marshal(XmlWriter xmlStream, T object, boolean closeStrean) throws XmlMarshalingException, IOException, XmlException {
		
		if (!object.getClass().isAnnotationPresent(XmlType.class))
			throw new XmlMarshalingException("class is not annotated as XML type");
		
		XmlRootType xmlRootAnnotation = object.getClass().getAnnotation(XmlRootType.class);
		if (xmlRootAnnotation == null)
			throw new XmlMarshalingException("class is not annotated as XML root type");
		
		URI namespace = null;
		if (!xmlRootAnnotation.namespace().equals(XmlField.NULL_STR))
			try {
				namespace = new URI(xmlRootAnnotation.namespace());
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("supplied namspace is not a vald URI: " + xmlRootAnnotation.namespace(), e);
			}
		
		writeElementObject(xmlStream, namespace, xmlRootAnnotation.value(), object);
		
		if (closeStrean)
			xmlStream.close();
		
	}
	
	protected <T, P> void writeElement(XmlWriter xmlStream, URI namespace, String name, XmlClassField<T, P> xmlField, Object xmlClassObject) throws XmlMarshalingException, IOException, XmlException {
		
		T[] value = xmlField.query(xmlClassObject, name);
		
		if (xmlField.adapter() != null || xmlField.isPrimitive()) {
			for (T elementValue : value) {
				if (elementValue == null) continue;
				String elementValueStr = xmlField.adapter() != null ? 
						xmlField.adapter().typeString(elementValue) : 
						XmlClassField.primitiveString(xmlField.type(), elementValue);
				
				// write element text data
				ElementDescriptor openingElement = new ElementDescriptor(DescType.OPEN, namespace, name, null);
				xmlStream.writeNext(openingElement);
				boolean useCData = xmlField.field().isAnnotationPresent(XmlCData.class);
				xmlStream.writeAllText(elementValueStr, useCData);
				xmlStream.writeNext(openingElement.getClosingTag());
			}
		} else {
			for (T elementValue : value) {
				if (elementValue == null) continue;
				// write element
				writeElementObject(xmlStream, namespace, name, elementValue);
			}
		}
		
	}
	
	protected <T, V> void writeElementObject(XmlWriter xmlStream, URI namespace, String name, T xmlObject) throws XmlMarshalingException, IOException, XmlException {
		
		@SuppressWarnings("unchecked")
		XmlClassType<T, ?> type = (XmlClassType<T, ?>) this.types.get(xmlObject.getClass());
		if (type == null)
			throw new XmlMarshalingException(xmlStream, "object class unknown to marshaler: " + xmlObject.getClass());
		
		// collect and order attributes
		Map<String, XmlClassField<?, ?>> attributeMap = new LinkedHashMap<>();
		for (String attributeName : type.attributeOrder())
			attributeMap.put(attributeName, type.attributes().get(attributeName));
		for (String attributeName : type.attributes().keySet()) {
			if (attributeMap.get(attributeName) != null) continue;
			if (attributeName.equals(XmlClassType.TEXT_VALUE_FIELD)) continue;
			var attributeField = type.attributes().get(attributeName);
			if (attributeName.equals(XmlClassType.REMAINING_MAP_FIELD)) {
				for (String attributeName2 : attributeField.queryKeys(xmlObject))
					attributeMap.put(attributeName2, attributeField);
			} else {
				attributeMap.put(attributeName, attributeField);
			}
		}

		// collect attribute values
		Map<String, String> attributes = new LinkedHashMap<String, String>();
		for (var attribute : attributeMap.entrySet()) {
			@SuppressWarnings("unchecked")
			XmlClassField<V, ?> attributeField = (XmlClassField<V, ?>) attribute.getValue();
			V[] value = attributeField.query(xmlObject, attribute.getKey());
			if (value.length == 0) continue;
			if (value[0] == null) continue;
			String attributeValue;
			if (attributeField.adapter() != null)
				attributeValue = attributeField.adapter().typeString(value[0]);
			else if (attributeField.isPrimitive())
				attributeValue = XmlClassField.primitiveString(attributeField.type(), value[0]);
			else
				throw new XmlMarshalingException(xmlStream, "attribute type needs type adapter: " + attribute.getKey());
			attributes.put(attribute.getKey(), attributeValue);
		}
		
		// check for text data field
		@SuppressWarnings("unchecked")
		XmlClassField<V, ?> textField = (XmlClassField<V, ?>) type.attributes().get(XmlClassType.TEXT_VALUE_FIELD);
		String textData = null;
		boolean useCData = false;
		if (textField != null) {
			useCData = textField.field().isAnnotationPresent(XmlCData.class);
			V[] value = textField.query(xmlObject, null);
			if (value.length > 0) {
				if (textField.adapter() != null) {
					textData = textField.adapter().typeString(value[0]);
				} else if (textField.isPrimitive()) {
					textData = XmlClassField.primitiveString(textField.type(), value[0]);
				} else {
					throw new XmlMarshalingException(xmlStream, "text data field requires type adapter");
				}
			}
		}
		
		// collect and order elements
		NamespaceMap<XmlClassField<?, ?>> elementMap = new NamespaceMap<>(false);
		for (String elementName : type.elementOrder())
			for (Entry<XmlClassField<?, ?>> element : type.elements().entrySet()) {
				if (!element.name().equals(elementName)) continue;
				elementMap.put(element.namespace(), element.name(), element.value());
			}
		for (Key elementKey : type.elements().keySet()) {
			if (elementMap.get(elementKey.namespace(), elementKey.name()) != null) continue;
			var elementField = type.elements().get(elementKey.namespace(), elementKey.name());
			if (elementKey.name().equals(XmlClassType.REMAINING_MAP_FIELD)) {
				for (String elementName : elementField.queryKeys(xmlObject))
					elementMap.put(elementKey.namespace(), elementName, elementField);
			} else {
				elementMap.put(elementKey.namespace(), elementKey.name(), elementField);
			}
		}
		
		// create opening element descriptor
		boolean isSelfClosing = textData == null && elementMap.isEmpty();
		ElementDescriptor openingElement = new ElementDescriptor(isSelfClosing ? DescType.SELF_CLOSING : DescType.OPEN, namespace, name, attributes);
		xmlStream.writeNext(openingElement);
		
		if (isSelfClosing) return;
		
		// write text data
		if (textData != null)
			xmlStream.writeAllText(textData, useCData);
		
		// marshal elements
		for (Entry<XmlClassField<?, ?>> element : elementMap.entrySet()) {
			writeElement(xmlStream, element.namespace(), element.name(), element.value(), xmlObject);
		}
		
		// create closing element descriptor
		xmlStream.writeNext(openingElement.getClosingTag());
		
	}
	
}
