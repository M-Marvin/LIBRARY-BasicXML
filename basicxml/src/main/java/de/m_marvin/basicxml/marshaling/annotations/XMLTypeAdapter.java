package de.m_marvin.basicxml.marshaling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.m_marvin.basicxml.marshaling.adapter.XMLClassFieldAdapter;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
/**
 * Defines an type adapter to convert the string XML data into the desired class
 */
public @interface XMLTypeAdapter {
	
	public Class<? extends XMLClassFieldAdapter<?, ?>> value();

	/**
	 * The parent class type of the data type of this type adapter.
	 * This has only an effect if the class is static and thus has no parent by default, and allows to manually define an parent type for data instances parsed with this adapter.
	 */
	public Class<?> parent() default Void.class;
	
}
