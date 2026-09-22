package de.m_marvin.basicxml.marshaling.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.m_marvin.basicxml.marshaling.annotations.XmlField;
import de.m_marvin.basicxml.marshaling.annotations.XmlOrder;
import de.m_marvin.basicxml.marshaling.annotations.XmlType;

/**
 * Describes information about an class type required for XML marshaling
 * @param <T> The type of the class
 * @param <P> The type of the parent class of this class, used when construction non-static classes
 */
public record XmlClassType<T, P>(
		/** true if this class is static and can be constructed without an parent instance **/
		boolean isStatic,
		/** the required type of the parent instacen if this class is non-static **/
		Class<P> parentType,
		/** the factory used to construct this class **/
		TypeFactory<T, P> factory,
		/** the classes which are defined by this class and also take part in XML marshaling **/
		Set<Class<?>> subTypes,
		/** the attribute fields defined in this class **/
		Map<String, XmlClassField<?, ?>> attributes,
		/** the element fields defined in this class **/
		NamespaceMap<XmlClassField<?, ?>> elements,
		/** order in which attributes are written to XML **/
		List<String> attributeOrder,
		/** order in which elements are written to XML **/
		List<String> elementOrder
		) {
	
	@FunctionalInterface
	public static interface TypeFactory<T, P> {
		public T makeType(P parentObject) throws LayerInstantiationException;
	}
	
	public static <T, P> XmlClassType<T, P> makeFromClass(Class<T> type, Class<P> parentType, boolean ignoreNamespaces) {
		Objects.requireNonNull(type, "type can not be null");
		
		if (!type.isAnnotationPresent(XmlType.class))
			throw new IllegalArgumentException("the supplied class is not annotated as an XML type object: " + type);
		
		boolean isStatic = type.getEnclosingClass() == null || Modifier.isStatic(type.getModifiers());
		if (!isStatic && parentType == null)
			throw new IllegalArgumentException("type is not a static class but parent type is null");
		try {
			Constructor<T> constructor = isStatic ? type.getDeclaredConstructor() : type.getDeclaredConstructor(parentType);
			TypeFactory<T, P> factory = isStatic ? parentObject -> {
				try {
					return constructor.newInstance();
				} catch (ExceptionInInitializerError | InvocationTargetException e) {
					throw new LayerInstantiationException("unable to construct type object: " + type , e);
				} catch (IllegalArgumentException | InstantiationException | IllegalAccessException e) {
					throw new LayerInstantiationException("construction of object threw an error: " + type, e);
				}
			} : parentObject -> {
				try {
					return constructor.newInstance(parentObject);
				} catch (ExceptionInInitializerError | InvocationTargetException e) {
					throw new LayerInstantiationException("unable to construct type object: " + type + " parent: " + parentObject, e);
				} catch (IllegalArgumentException | InstantiationException | IllegalAccessException e) {
					throw new LayerInstantiationException("construction of object threw an error: " + type + " parent: " + parentObject, e);
				}
			};
			
			XmlOrder xmlOrderAnnotation = type.getAnnotation(XmlOrder.class);
			List<String> attributeOrder = xmlOrderAnnotation == null ? Collections.emptyList() : Arrays.asList(xmlOrderAnnotation.attributes());
			List<String> elementOrder = xmlOrderAnnotation == null ? Collections.emptyList() : Arrays.asList(xmlOrderAnnotation.elements());
			
			XmlClassType<T, P> xmlClassType = new XmlClassType<T, P>(isStatic, parentType, factory, new HashSet<>(), new LinkedHashMap<>(), new NamespaceMap<>(ignoreNamespaces), attributeOrder, elementOrder);
			findFieldsAndTypes(type, xmlClassType);
			return xmlClassType;
			
		} catch (NoSuchMethodException e) {
			throw new LayerInstantiationException("the supplied type class has no default constructor", e);
		}
	}

	public static final String TEXT_VALUE_FIELD = "!TEXT!";
	public static final String REMAINING_MAP_FIELD = "!REMAINING!";
	
	private static void findFieldsAndTypes(Class<?> clazz, XmlClassType<?, ?> xmlClassType) {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass.isAnnotationPresent(XmlType.class))
			findFieldsAndTypes(superclass, xmlClassType);
		
		for (Class<?> in : clazz.getInterfaces())
			if (in.isAnnotationPresent(XmlType.class))
				findFieldsAndTypes(in, xmlClassType);
		
		for (Field field : clazz.getDeclaredFields()) {
			XmlField xmlField = field.getAnnotation(XmlField.class);
			if (xmlField == null) continue;
			String name = xmlField.name().equals(XmlField.NULL_STR) ? field.getName() : xmlField.name();
			String namespace = xmlField.namespace().equals(XmlField.NULL_STR) ? null : xmlField.namespace();
			
			XmlClassField<?, ?> xmlClassField = XmlClassField.makeFromField(field.getType(), field);
			
			switch (xmlField.value()) {
			case ATTRIBUTE: 
				xmlClassType.attributes.put(name, xmlClassField); 
				break;
			case ELEMENT: 
			case ELEMENT_COLLECTION:
				xmlClassType.elements.put(namespace, name, xmlClassField); 
				break;
			case REMAINING_ATTRIBUTE_MAP:
				xmlClassType.attributes.put(REMAINING_MAP_FIELD, xmlClassField); 
				break;
			case REMAINING_ELEMENT_MAP:
				xmlClassType.elements.put(namespace, REMAINING_MAP_FIELD, xmlClassField); 
				break;
			case TEXT: 
				xmlClassType.attributes.put(TEXT_VALUE_FIELD, xmlClassField); 
				break;
			}
		}
		
		for (Class<?> type : clazz.getDeclaredClasses()) {
			if (type.isAnnotationPresent(XmlType.class))
				xmlClassType.subTypes.add(type);
		}
	}
	
}
