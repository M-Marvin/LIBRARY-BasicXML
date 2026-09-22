package de.m_marvin.basicxml.dom;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

import de.m_marvin.basicxml.XmlException;
import de.m_marvin.basicxml.XmlReader;
import de.m_marvin.basicxml.XmlStream.DescType;
import de.m_marvin.basicxml.XmlStream.ElementDescriptor;
import de.m_marvin.basicxml.XmlWriter;

public class XmlDOM {
	
	public XmlNamed read(XmlReader xmlReader) throws IOException, XmlException {
		return read(xmlReader, true);
	}
	
	public XmlNamed read(XmlReader xmlReader, boolean closeReader) throws IOException, XmlException {
		ElementDescriptor tag = xmlReader.readNext();
		if (tag == null) {
			if (closeReader)
				xmlReader.close();
			return null;
		}
		XmlNamed xmlNamed = new XmlNamed(tag.name(), fromXml(xmlReader, tag));
		if (closeReader)
			xmlReader.close();
		return xmlNamed;
	}
	
	private XmlTag fromXml(XmlReader xmlReader, ElementDescriptor tag) throws IOException, XmlException {
		
		// parse name and attributes
		XmlTag xmlTag = new XmlTag(tag.namespace());
		if (tag.attributes() != null)
			tag.attributes().forEach((name, value) -> xmlTag.addEntry(name, new XmlPrimitive(value)));
		
		if (tag.type() == DescType.SELF_CLOSING)
			return xmlTag;
		
		// read inner text and tags
		String innerText;
		while ((innerText = xmlReader.readAllText()) != null) {
			
			// parse inner text
			if (!innerText.isBlank())
				xmlTag.addEntry(null, new XmlPrimitive(innerText.strip()));
			
			// read inner tag
			ElementDescriptor tag2 = xmlReader.readNext();
			
			// check for closing tag
			if (tag2.type() == DescType.CLOSE) {
				if (!tag2.name().equals(tag.name()))
					throw new XmlException("improper element close order, element not closed: " + tag.namespace() + " > " + tag.name());
				return xmlTag;
			}
			
			// parse inner tag
			xmlTag.addEntry(tag2.name(), fromXml(xmlReader, tag2));
			
		}

		throw new IOException("element missing closing tag: " + tag.namespace() + " > " + tag.name());
		
	}
	
	public void write(XmlWriter xmlWriter, XmlNamed xmlNamed) throws IOException, XmlException {
		write(xmlWriter, xmlNamed, true);
	}
	
	public void write(XmlWriter xmlWriter, XmlNamed xmlNamed, boolean closeWriter) throws IOException, XmlException {
		if (!xmlNamed.isInnerTag())
			throw new IllegalArgumentException("root element has to be an named inner tag");
		toXml(xmlWriter, xmlNamed.getName(), xmlNamed.getValue().getAsTag());
		if (closeWriter)
			xmlWriter.close();
	}
	
	private void toXml(XmlWriter xmlWriter, String name, XmlTag xmlTag) throws IOException, XmlException {
		
		// write tag
		ElementDescriptor tag = new ElementDescriptor(
				xmlTag.getInnerElements().count() > 0 ? DescType.OPEN : DescType.SELF_CLOSING, 
				xmlTag.getNamespace(), 
				name, 
				xmlTag.getAttributes().collect(Collectors.toMap(XmlNamed::getName, xmlEntry -> xmlEntry.getValue().getAsPrimitive().getValue()))
		);
		xmlWriter.writeNext(tag);
		
		if (tag.type() == DescType.SELF_CLOSING)
			return;
		
		// write inner elements
		Iterator<XmlNamed> iter = xmlTag.getInnerElements().iterator();
		while (iter.hasNext()) {
			XmlNamed entry = iter.next();
			
			// write inner text
			if (entry.isInnerText())
				xmlWriter.writeAllText(entry.getValue().getAsPrimitive().getValue(), entry.getValue().getAsPrimitive().isCData());
			
			// write inner tag
			if (entry.isInnerTag())
				toXml(xmlWriter, entry.getName(), entry.getValue().getAsTag());
			
		}
		
		// write closing tag
		xmlWriter.writeNext(tag.getClosingTag());
		
	}
	
}
