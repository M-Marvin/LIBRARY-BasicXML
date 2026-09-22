package de.m_marvin.basicxml;

/**
 * Indicates an error while parsing XML data by an XML stream
 */
public class XmlException extends Exception {
	
	private static final long serialVersionUID = -7153809693116902097L;
	
	public XmlException() {
		super();
	}

	public XmlException(String msg) {
		super(msg);
	}

	public XmlException(String msg, Exception e) {
		super(msg, e);
	}

	public XmlException(XmlStream stream,String msg) {
		super(stream.xmlStackPath() + " : " + msg);
	}

	public XmlException(XmlStream stream,String msg, Exception e) {
		super(stream.xmlStackPath() + " : " + msg, e);
	}
	
}
