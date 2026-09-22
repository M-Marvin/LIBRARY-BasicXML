package de.m_marvin.basicxml.dom;

public interface XmlElement {
	
	public boolean isTag();
	public boolean isPrimitive();
	public boolean isNamed();
	public boolean isList();
	
	public XmlPrimitive getAsPrimitive();
	public XmlTag getAsTag();
	public XmlNamed getAsNamed();
	public XmlList getAsList();
	
}
