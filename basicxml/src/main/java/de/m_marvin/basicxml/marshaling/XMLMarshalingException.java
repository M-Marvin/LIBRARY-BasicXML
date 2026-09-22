package de.m_marvin.basicxml.marshaling;

import de.m_marvin.basicxml.XmlStream;

/**
 * Indicates an error while parsing or writing XML data by an (un)marshaler
 */
public class XmlMarshalingException extends Exception {
	
	private static final long serialVersionUID = -7153809693116902097L;
	
	public XmlMarshalingException() {
		super();
	}

	public XmlMarshalingException(String msg) {
		super(msg);
	}

	public XmlMarshalingException(String msg, Exception e) {
		super(msg, e);
	}

	public XmlMarshalingException(XmlStream stream,String msg) {
		super(stream.xmlStackPath() + " : " + msg);
	}

	public XmlMarshalingException(XmlStream stream,String msg, Exception e) {
		super(stream.xmlStackPath() + " : " + msg, e);
	}
	
}
