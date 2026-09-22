package test;

import java.util.ArrayList;
import java.util.HashMap;

import de.m_marvin.basicxml.marshaling.adapter.XmlClassFieldAdapter;
import de.m_marvin.basicxml.marshaling.annotations.XmlField;
import de.m_marvin.basicxml.marshaling.annotations.XmlField.FieldType;
import de.m_marvin.basicxml.marshaling.annotations.XmlRootType;
import de.m_marvin.basicxml.marshaling.annotations.XmlType;
import de.m_marvin.basicxml.marshaling.annotations.XmlTypeAdapter;

@XmlType
@XmlRootType(value = "testtype", namespace = "")
public class TestType {
	
	public static final String NS = "";
	
	@XmlField(value = FieldType.ATTRIBUTE, namespace = NS)
	public boolean test;
	
	@XmlField(value = FieldType.ELEMENT, namespace = NS)
	public TestSubType testsubtype;
	
	@XmlType
	public class TestSubType {

		@XmlField(value = FieldType.ATTRIBUTE, namespace = NS)
		public String attribute1;

		@XmlField(value = FieldType.ATTRIBUTE, namespace = NS)
		public String attribute2;
		
	}

	@XmlType
	public class TestList { @XmlField(value = FieldType.ELEMENT_COLLECTION, type = TestItem.class, namespace = NS) public ArrayList<TestItem> testitem; }
	@XmlField(value = FieldType.ELEMENT, namespace = NS)
	public TestList testlist;
	
	@XmlType
	public class TestItem extends TestSubType {

		@XmlField(value = FieldType.TEXT, namespace = NS)
		public String value;
		
	}
	
	@XmlTypeAdapter(TestDataClass.class)
	public static class TestDataClass implements XmlClassFieldAdapter<TestDataClass, Void> {
		
		public String text;

		@Override
		public TestDataClass adaptType(String str, Void parentObject) {
			TestDataClass testData = new TestDataClass();
			testData.text = str;
			return testData;
		}

		@Override
		public String typeString(TestDataClass value) {
			return value.text;
		}
		
	}
	
	@XmlField(value = FieldType.REMAINING_ELEMENT_MAP, type = TestDataClass.class, namespace = NS)
	public HashMap<String, TestDataClass> remaining;
	
	@XmlField(value = FieldType.ELEMENT, namespace = NS)
	public TestEnum zzz;
	
	public static enum TestEnum {
		
		TEST1,TEST2;
		
	}
	
}
