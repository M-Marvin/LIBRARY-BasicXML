package de.m_marvin.basicxml;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * Interface implemented by XMLInputStream and XMLOutputStream, defines some common methods and types
 */
public interface XmlStream {

	/**
	 * Describes if the element was opened, closed or is self closing
	 */
	public static enum DescType {
		OPEN,
		CLOSE,
		SELF_CLOSING
	}
	
	/**
	 * Describes an element that was parsed from the XML file
	 */
	public static record ElementDescriptor(DescType type, URI namespace, String name, Map<String, String> attributes) {

		public boolean isSameField(ElementDescriptor other) {
			return Objects.equals(other.namespace, namespace) && Objects.equals(other.name, name);
		}
		
		@Override
		public final String toString() {
			return "namespace: " + this.namespace + " element: " + this.name;
		}
		
		public ElementDescriptor getClosingTag() {
			if (type != DescType.OPEN)
				throw new UnsupportedOperationException();
			return new ElementDescriptor(DescType.CLOSE, namespace, name, null);
		}
		
	}
	public String xmlStackPath();
	
}
