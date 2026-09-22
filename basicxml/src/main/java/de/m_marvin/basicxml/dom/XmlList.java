package de.m_marvin.basicxml.dom;

import java.util.List;
import java.util.stream.Stream;

public record XmlList(List<XmlTag> list) implements XmlElement {

	public static XmlList of(XmlElement... elements) {
		return new XmlList(Stream.of(elements)
			.map(element -> element.isPrimitive() ? new XmlTag(element.getAsPrimitive()) : element.getAsTag())
			.toList());
	}
	
	public static XmlList of(List<XmlElement> elements) {
		return new XmlList(elements.stream()
			.map(element -> element.isPrimitive() ? new XmlTag(element.getAsPrimitive()) : element.getAsTag())
			.toList());
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
		return false;
	}

	@Override
	public boolean isList() {
		return true;
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
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XmlList getAsList() {
		return this;
	}

	public List<XmlNamed> flatten(String name) {
		return this.list.stream().flatMap(entry -> {
			if (entry.isPrimitive())
				return Stream.of(new XmlNamed(name, new XmlTag(entry.getAsPrimitive())));
			else if (entry.isTag())
				return Stream.of(new XmlNamed(name, entry.getAsTag()));
			else if (entry.isNamed())
				return Stream.of(entry.getAsNamed());
			else if (entry.isList())
				return entry.getAsList().flatten(name).stream();
			return Stream.empty();
		}).toList();
	}
	
	@Override
	public String toString() {
		return String.format("XmlList{list=%s}", this.list);
	}
	
}

