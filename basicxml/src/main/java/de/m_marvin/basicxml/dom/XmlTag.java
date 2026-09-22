package de.m_marvin.basicxml.dom;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class XmlTag implements XmlElement {
	
	private URI namespace;
	private final List<XmlNamed> entries;
	
	public XmlTag(URI namespace, List<XmlNamed> entries) {
		this.namespace = namespace;
		this.entries = new ArrayList<XmlNamed>(entries);
	}
	
	public XmlTag(URI namespace) {
		this(namespace, new ArrayList<>());
	}

	public XmlTag(List<XmlNamed> entries) {
		this(null, entries);
	}
	
	public XmlTag(XmlPrimitive primitive) {
		this();
		addEntry(null, primitive);
	}
	
	public XmlTag() {
		this((URI) null);
	}
	
	public URI getNamespace() {
		return namespace;
	}
	
	public void setNamespace(URI namespace) {
		this.namespace = namespace;
	}
	
	@Override
	public boolean isTag() {
		return true;
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
		return false;
	}
	
	@Override
	public XmlPrimitive getAsPrimitive() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XmlTag getAsTag() {
		return this;
	}
	
	@Override
	public XmlNamed getAsNamed() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XmlList getAsList() {
		throw new UnsupportedOperationException();
	}
	
	public void setEntries(List<XmlNamed> innerEntries) {
		this.entries.clear();
		this.entries.addAll(innerEntries);
	}
	
	public List<XmlNamed> getEntries() {
		return entries;
	}
	
	public void addEntry(String name, XmlElement element) {
		if (element.isList())
			element.getAsList().flatten(name).forEach(this.entries::add);
		else
			this.entries.add(new XmlNamed(name, element));
	}
	
	public XmlList getEntryList(String name) {
		XmlElement entry = getEntry(name);
		if (entry.isList())
			return entry.getAsList();
		else
			return XmlList.of(Collections.singletonList(entry));
	}
	
	public XmlElement getEntry(String name) {
		List<XmlElement> entries = getEntries(name);
		if (entries.size() == 0)
			return null;
		else if (entries.size() == 1)
			return entries.get(0);
		else
			return XmlList.of(entries);
	}
	
	public List<XmlElement> getEntries(String name) {
		return this.entries.stream().filter(named -> Objects.equals(named.getName(), name)).map(XmlNamed::getValue).toList();
	}
	
	public Stream<XmlNamed> getInnerElements() {
		return this.entries.stream()
				.filter(e -> e.isInnerText() || e.isInnerTag());
	}
	
	public Stream<XmlNamed> getAttributes() {
		return this.entries.stream()
				.filter(XmlNamed::isAttribute);
	}

	@Override
	public String toString() {
		return String.format("XmlTag{namespace=%s, entries=%s}", this.namespace, this.entries);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.namespace, this.entries);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XmlTag other) {
			return	Objects.equals(this.namespace, other.namespace) &&
					Objects.equals(this.entries, other.entries);
		}
		return false;
	}
	
}
