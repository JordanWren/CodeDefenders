/* no package name */

import org.junit.*;
import static org.junit.Assert.*;

public class TestXmlElement {
	@Test(timeout = 4000)
	public void test() throws Throwable {
		java.util.Hashtable<String,String> hm=new java.util.Hashtable<String,String>(); 
		hm.put("name","tes");
		XmlElement e = new XmlElement("na", hm);
		assertEquals(e.getName(), "na");
		assertEquals(e.getAttribute("name"), "tes");
	}
}