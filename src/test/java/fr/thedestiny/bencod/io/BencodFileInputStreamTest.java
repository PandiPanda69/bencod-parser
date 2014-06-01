package fr.thedestiny.bencod.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class BencodFileInputStreamTest {

	@Test
	public void readFileTest() throws IOException {
		
		File file = new File(this.getClass().getResource("/string.bencod").getFile());
		
		BencodFileInputStream bfis = new BencodFileInputStream(file);
		
		int length = bfis.available();
		assertEquals(15, length);
		
		int firstChar = bfis.read();
		assertEquals('1', (char) firstChar);
		
		int newLength = bfis.available();
		assertEquals(14, newLength);
		
		bfis.move(4);
		int afterMoveLength = bfis.available();
		assertEquals(10, afterMoveLength);
		
		int fifthChar = bfis.read();
		assertEquals('l', (char) fifthChar);
		
		bfis.move(-1);
		int afterNegativeMoveLength = bfis.available();
		assertEquals(10, afterNegativeMoveLength);
		
		bfis.move(100000);
		int afterMegaMoveLength = bfis.available();
		assertEquals(0, afterMegaMoveLength);
		
		int eof = bfis.read();
		assertEquals(-1, eof);
		
		bfis.move(-1000000);
		int afterMegaRetromoveLength = bfis.available();
		assertEquals(15, afterMegaRetromoveLength);
		
		byte[] buffer = new byte[5];
		int rangeBytesRead = bfis.read(buffer);
		int afterRangeBytesRead = bfis.available();
		String dataRead = new String(buffer);
		
		assertEquals(5, rangeBytesRead);
		assertEquals(10, afterRangeBytesRead);
		assertEquals("11:He", dataRead);
		
		bfis.close();
	}
}
