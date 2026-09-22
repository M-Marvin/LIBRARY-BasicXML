package de.m_marvin.basicxml.dom;

import java.util.Objects;

public class XmlNamed implements XmlElement {
	
	private String name;
	private XmlElement value;
	
	public XmlNamed(String name, XmlElement value) {
		this.name = name;
		this.value = value;
	}
	
	public XmlNamed(XmlElement value) {
		this(null, value);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(XmlElement value) {
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public XmlElement getValue() {
		return value;
	}
	
	@Override
	public boolean isTag() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isNamed() {
		return true;
	}
	
	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public XmlPrimitive getAsPrimitive() {
		throw new UnsupportedOperationException();
	}

	@Override
	public XmlTag getAsTag() {
		throw new UnsupportedOperationException();
	}

	@Override
	public XmlNamed getAsNamed() {
		return this;
	}
	
	@Override
	public XmlList getAsList() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isInnerText() {
		return this.name == null && this.value.isPrimitive();
	}
	
	public boolean isAttribute() {
		return this.name != null && this.value.isPrimitive();
	}
	
	public boolean isInnerTag() {
		return this.value.isTag();
	}
	
	@Override
	public String toString() {
		if (this.name == null)
			return String.format("XmlNamed{value=%s}", this.value);
		else
			return String.format("XmlNamed{name=%s, value=%s}", this.name, this.value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XmlNamed other) {
			return	Objects.equals(this.name, other.name) &&
					Objects.equals(this.value, other.value);
		}
		return false;
	}
	
}

