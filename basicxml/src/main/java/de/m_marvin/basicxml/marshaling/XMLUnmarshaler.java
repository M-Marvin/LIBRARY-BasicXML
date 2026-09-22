package de.m_marvin.basicxml.marshaling;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

import de.m_marvin.basicxml.XmlException;
import de.m_marvin.basicxml.XmlReader;
import de.m_marvin.basicxml.XmlStream.DescType;
import de.m_marvin.basicxml.XmlStream.ElementDescriptor;
import de.m_marvin.basicxml.marshaling.internal.XmlClassField;
import de.m_marvin.basicxml.marshaling.internal.XmlClassType;

public class XmlUnmarshaler {
	
	private final Map<Class<?>, XmlClassType<?, ?>> types = new HashMap<>();
	
	public XmlUnmarshaler(boolean ignoreNamespaces, Class<?>... types) {
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
	
	public <T> T unmarshall(XmlReader xmlStream, Class<T> objectType, URI fallbackNamespace) throws IOException, XmlException, XmlMarshalingException {
		return unmarshall(xmlStream, objectType, fallbackNamespace, true);
	}
		
	public <T> T unmarshall(XmlReader xmlStream, Class<T> objectType, URI fallbackNamespace, boolean closeStream) throws IOException, XmlException, XmlMarshalingException {
		
		if (fallbackNamespace != null)
			xmlStream.getNamespaces().put("", fallbackNamespace);
		ElementDescriptor element = xmlStream.readNext();
		if (element == null) return null;
		T xmlObject = makeObjectFromXML(xmlStream, element, objectType, new Stack<Object>());
		
		if (closeStream)
			xmlStream.close();
		
		return xmlObject;
		
	}

	public <T> T unmarshall(XmlReader xmlStream, Class<T> objectType) throws IOException, XmlException, XmlMarshalingException {
		return unmarshall(xmlStream, objectType, true);
	}
	
	public <T> T unmarshall(XmlReader xmlStream, Class<T> objectType, boolean closeStream) throws IOException, XmlException, XmlMarshalingException {
		return unmarshall(xmlStream, objectType, null, closeStream);
	}
	
	private <T, P> void fillAttributeFromXML(Object xmlClassObject, XmlClassField<T, P> attributeField, String attributeName, XmlReader xmlStream, String valueStr, Stack<Object> objectStack) throws XmlMarshalingException {

		objectStack.push(xmlClassObject);
		T value = null;
		if (attributeField.adapter() != null) {
			@SuppressWarnings("unchecked")
			P parentObject = attributeField.parentType() == null ? null : 
				(P) findTopMost(objectStack, attributeField.parentType()::isInstance);
			try {
				value = attributeField.adapter().adaptType(valueStr, parentObject);
			} catch (XmlException e) {
				throw new XmlMarshalingException(xmlStream, "error while invoking type adapter: " + attributeField.field(), e);
			} catch (ClassCastException e) {
				// if this happens, it indicates that the adapters parent object-type type-argument was probably set to some arbitrary value because the parent argument is not required.
				try {
					value = attributeField.adapter().adaptType(valueStr, null);
				} catch (XmlException e1) {
					throw new XmlMarshalingException(xmlStream, "error while invoking type adapter: " + attributeField.field(), e1);
				}
			}
		} else if (attributeField.isPrimitive()) {
			value = XmlClassField.adaptPrimitive(attributeField.type(), valueStr);
		} else {
			throw new XmlMarshalingException(xmlStream, "attribute is not XML primitive and has no adapter: " + attributeField.field());
		}
		objectStack.pop();
		
		attributeField.assign(xmlClassObject, value, attributeName);
		
	}
	
	private <T, P> void fillElementFromXML(Object xmlClassObject, XmlClassField<T, P> elementField, String elementName, XmlReader xmlStream, ElementDescriptor openingElement, Stack<Object> objectStack) throws IOException, XmlException, XmlMarshalingException {
		
		objectStack.push(xmlClassObject);
		T value = makeObjectFromXML(xmlStream, openingElement, elementField.type(), objectStack);
		objectStack.pop();
		
		elementField.assign(xmlClassObject, value, elementName);
		
	}
	
	private <T, P> T makeObjectFromXML(XmlReader xmlStream, ElementDescriptor openingElement, Class<T> objectType, Stack<Object> objectStack) throws IOException, XmlException, XmlMarshalingException {
		assert openingElement.type() != DescType.CLOSE : "element descriptor can not be a closing element";
	
		@SuppressWarnings("unchecked")
		XmlClassType<T, P> xmlClassType = (XmlClassType<T, P>) this.types.get(objectType);
		if (xmlClassType == null)
			 throw new XmlMarshalingException("the supplied type is not recognized by this marshaler: " + objectType.getName());
		
		@SuppressWarnings("unchecked")
		P parentObject = xmlClassType.isStatic() ? null : (P) findTopMost(objectStack, xmlClassType.parentType()::isInstance);
		if (!xmlClassType.isStatic() && parentObject == null)
			throw new XmlMarshalingException(xmlStream, "non-static class hierarchical error, unable to identify closest parent class to construct from: " + xmlClassType.parentType());
		T xmlClassObject = xmlClassType.factory().makeType(parentObject);
		
		for (String attributeName : openingElement.attributes().keySet()) {
			XmlClassField<?, ?> attributeField = xmlClassType.attributes().get(attributeName);
			if (attributeField == null) {
				attributeField = xmlClassType.attributes().get(XmlClassType.REMAINING_MAP_FIELD);
				if (attributeField == null) continue;
			}
			fillAttributeFromXML(xmlClassObject, attributeField, attributeName, xmlStream, openingElement.attributes().get(attributeName), objectStack);
		}
		
		if (openingElement.type() != DescType.SELF_CLOSING) {
			StringBuffer elementText = new StringBuffer();
			readelements: while (true) {
				
				ElementDescriptor element;
				while ((element = xmlStream.readNext()) != null) {
					
					if (element.type() == DescType.CLOSE) {
						if (!element.isSameField(openingElement))
							throw new XmlMarshalingException(xmlStream, "improper element close order, element not closed: " + openingElement.namespace() + " > " + openingElement.name()); // this would indicate a problem with the stream
						break readelements;
					} else {
						XmlClassField<?, ?> xmlElementField = xmlClassType.elements().get(element.namespace(), element.name());
						if (xmlElementField == null) {
							xmlElementField = xmlClassType.elements().get(element.namespace(), XmlClassType.REMAINING_MAP_FIELD);
							if (xmlElementField == null) {
								if (element.type() == DescType.OPEN) {
									// skip the element, read until close reached
									skipelement: while (true) {
										ElementDescriptor e;
										while ((e = xmlStream.readNext()) != null)
											if (e.isSameField(element)) break skipelement;
										if (xmlStream.readAllText() == null)
											throw new XmlMarshalingException(xmlStream, "unexpected EOF while skipping element: " + element.namespace() + " > " + element.name());
									}
								}
								continue;
							}
						}
						
						if (xmlElementField.isPrimitive() || xmlElementField.adapter() != null) {
							// read only text data of the element
							StringBuffer text = new StringBuffer();
							if (element.type() != DescType.SELF_CLOSING) {
								readtext: while (true) {
									ElementDescriptor e;
									while ((e = xmlStream.readNext()) != null)
										if (e.isSameField(element)) break readtext;
									String s = xmlStream.readAllText();
									if (s == null)
										throw new XmlMarshalingException(xmlStream, "unexpected EOF while reading element text: " + element.namespace() + " > " + element.name());
									text.append(s);
								}
							}
							// write variable as if it was an attribute
							fillAttributeFromXML(xmlClassObject, xmlElementField, element.name(), xmlStream, text.toString(), objectStack);
						} else {
							fillElementFromXML(xmlClassObject, xmlElementField, element.name(), xmlStream, element, objectStack);
						}
					}
					
				}
				
				String s = xmlStream.readAllText();
				if (s == null)
					throw new XmlMarshalingException(xmlStream, "unexpected end of XML stream"); // this would indicate a problem with the stream
				elementText.append(s);
				
			}
			
			XmlClassField<?, ?> xmlTextField = xmlClassType.attributes().get(XmlClassType.TEXT_VALUE_FIELD);
			if (xmlTextField != null) {
				fillAttributeFromXML(xmlClassObject, xmlTextField, null, xmlStream, elementText.toString(), objectStack);
			}
		}
		
		return xmlClassObject;
		
	}
	
	public static <T> T findTopMost(Stack<T> stack, Predicate<T> predicate) {
		return stack.stream().filter(predicate).reduce((a, b) -> b).orElseGet(null);
	}
	
}
