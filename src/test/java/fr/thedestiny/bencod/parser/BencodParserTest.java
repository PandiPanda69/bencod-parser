package fr.thedestiny.bencod.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.thedestiny.bencod.io.BencodFileInputStream;

public class BencodParserTest {

	@Test
	public void parseStringTest() throws IOException, BencodFileFormatException {
		
		File file = new File(this.getClass().getResource("/string.bencod").getFile());
		
		BencodFileInputStream bfis = new BencodFileInputStream(file);
		BencodParser parser = new BencodParser(bfis);
		
		Object obj = parser.parse();
		assertEquals("Hello world", obj);
		
		bfis.close();
	}
	
	@Test
	public void parseIntegerTest() throws IOException, BencodFileFormatException {
		File file = new File(this.getClass().getResource("/integer.bencod").getFile());
		
		BencodFileInputStream bfis = new BencodFileInputStream(file);
		BencodParser parser = new BencodParser(bfis);
		
		Object obj = parser.parse();
		assertEquals(42, obj);
		
		bfis.close();
	}
	
	@Test
	public void parseListTest()  throws IOException, BencodFileFormatException {
		File file = new File(this.getClass().getResource("/list.bencod").getFile());
		
		BencodFileInputStream bfis = new BencodFileInputStream(file);
		BencodParser parser = new BencodParser(bfis);
		
		Object obj = parser.parse();
		if(!(obj instanceof List)) {
			fail();
		}
		
		@SuppressWarnings( "unchecked" )
		List<Object> list = (List<Object>) obj;
		assertEquals(2, list.size());
		
		assertEquals("Hello world", list.get(0));
		assertEquals(42, list.get(1));
		
		bfis.close();
	}
	
	@Test
	public void parseDictionaryTest() throws IOException, BencodFileFormatException {
		File file = new File(this.getClass().getResource("/dictionary.bencod").getFile());
		
		BencodFileInputStream bfis = new BencodFileInputStream(file);
		BencodParser parser = new BencodParser(bfis);
		
		Object obj = parser.parse();
		if(!(obj instanceof Map)) {
			fail();
		}
		
		@SuppressWarnings( "unchecked" )
		Map<String, Object> dictionary = (Map<String, Object>) obj;
		
		Object A = dictionary.get("A");
		Object B = dictionary.get("B");
		
		assertNotNull(A);
		assertNotNull(B);

		if(!(A instanceof List)) {
			fail();
		}

		@SuppressWarnings( "unchecked" )
		List<Object> subList = (List<Object>) A;
		assertEquals("Hello world", subList.get(0));
		assertEquals(42, subList.get(1));
		
		assertEquals("OK", B);

		bfis.close();
	}
}
