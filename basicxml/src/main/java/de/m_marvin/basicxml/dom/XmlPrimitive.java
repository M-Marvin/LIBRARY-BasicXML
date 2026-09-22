package de.m_marvin.basicxml.dom;

import java.util.Objects;

public class XmlPrimitive implements XmlElement {
	
	private String value;
	private boolean cdata;
	
	public XmlPrimitive(String value, boolean cdata) {
		this.value = value;
		this.cdata = cdata;
	}
	
	public XmlPrimitive(String value) {
		this(value, false);
	}
	
	public XmlPrimitive(Number number) {
		this(number.toString());
	}

	public XmlPrimitive(Boolean bool) {
		this(bool.toString());
	}
	
	@Override
	public boolean isTag() {
		return false;
	}
	
	@Override
	public boolean isPrimitive() {
		return true;
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
		return this;
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
		throw new UnsupportedOperationException();
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void setCData(boolean cdata) {
		this.cdata = cdata;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isCData() {
		return cdata;
	}
	
	/* alternate format accessors */
	
	public String getAsString() {
		return this.value;
	}
	
	public Long getAsLong() {
		try {
			return Long.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Integer getAsInt() {
		try {
			return Integer.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Short getAsShort() {
		try {
			return Short.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Byte getAsByte() {
		try {
			return Byte.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Boolean getAsBool() {
		try {
			return Boolean.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Double getAsDouble() {
		try {
			return Double.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Float getAsFloat() {
		try {
			return Float.valueOf(this.value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void setString(String value) {
		this.value = value;
	}
	
	public void setLong(long value) {
		this.value = Long.toString(value);
	}

	public void setInt(int value) {
		this.value = Integer.toString(value);
	}

	public void setShort(short value) {
		this.value = Short.toString(value);
	}

	public void setByte(byte value) {
		this.value = Byte.toString(value);
	}

	public void setBool(boolean value) {
		this.value = Boolean.toString(value);
	}

	public void setDouble(double value) {
		this.value = Double.toString(value);
	}

	public void setFloat(float value) {
		this.value = Float.toString(value);
	}

	@Override
	public String toString() {
		return String.format("XmlPrimitive{value=%s}", this.value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XmlPrimitive other) {
			return	Objects.equals(this.value, other.value);
		}
		return false;
	}
	
}
