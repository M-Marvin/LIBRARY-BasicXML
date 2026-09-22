package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import de.m_marvin.basicxml.XmlReader;
import de.m_marvin.basicxml.XmlWriter;
import de.m_marvin.basicxml.dom.XmlDOM;
import de.m_marvin.basicxml.dom.XmlList;
import de.m_marvin.basicxml.dom.XmlNamed;
import de.m_marvin.basicxml.dom.XmlPrimitive;
import de.m_marvin.basicxml.marshaling.XmlMarshaler;
import de.m_marvin.basicxml.marshaling.XmlUnmarshaler;

public class Test {
	
	public static void main(String... args) throws Exception, URISyntaxException {
		
		File dir = new File(Test.class.getProtectionDomain().getCodeSource().getLocation().toURI().toURL().getPath(), "../../");
		
		OutputStream output = new FileOutputStream(new File(dir, "/test/test2.xml"));
		
		XmlWriter xmlOut = new XmlWriter(output);
		
		InputStream input = new FileInputStream(new File(dir, "/test/test.xml"));
		
		XmlReader xmlIn = new XmlReader(input);
		
//		String text;
//		ElementDescriptor element;
//		do {
//			element = xmlIn.readNext();
//			xmlOut.writeNext(element);
//			text = xmlIn.readAllText();
//			if (text != null && !text.isBlank())
//				xmlOut.writeAllText(text, false);
//		} while (text != null);
//		
//		xmlIn.close();
//		xmlOut.close();
		
		XmlUnmarshaler unmarshaller = new XmlUnmarshaler(true, TestType.class);
		
		var object = unmarshaller.unmarshall(xmlIn, TestType.class);
		
		System.out.println(object.testlist.testitem.get(0).value);
		
		for (String k : object.remaining.keySet()) {
			System.out.println(k + " = " + object.remaining.get(k));
		}
		
		System.out.println(object.zzz);
		
		XmlMarshaler marshaler = new XmlMarshaler(false, TestType.class);
		
		marshaler.marshal(xmlOut, object);
		
//		System.out.println("Version: " + xmlIn.getVersion());
//		System.out.println("Encoding: " + xmlIn.getEncoding());
//		
//		for (int i = 0; i < 100; i++) {
//			var element = xmlIn.readNext();
//			if (element == null) {
//				String text = xmlIn.readAllText();
//				if (text == null) break;
//				System.out.println(text);
//			} else {
//				System.out.println(element);
//			}
//		}
//		
//		xmlIn.close();
		
		XmlDOM parser = new XmlDOM();

		input = new FileInputStream(new File(dir, "/test/test.xml"));
		
		xmlIn = new XmlReader(input);
		
		XmlNamed tag = parser.read(xmlIn);
		
		tag.getValue().getAsTag().addEntry("test", XmlList.of(new XmlPrimitive("Test1"), new XmlPrimitive("Test2")));
		
		output = new FileOutputStream(new File(dir, "/test/test3.xml"));
		
		xmlOut = new XmlWriter(output);
		
		parser.write(xmlOut, tag);
		
		System.out.println(tag);
		
	}
	
}
