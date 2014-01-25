package fr.thedestiny.bencod.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.thedestiny.bencod.io.BencodFileInputStream;

/**
 * Class parsing a bencod file.
 * Bencod format contains several chunks that can one of those types:
 * <ul>
 * 	<li>string: A simple string</li>
 *  <li>integer: A simple integer</li>
 * 	<li>dictionary: A map with string keys and object values</li>
 *  <li>list: A list of objects</li>
 * </ul>
 * 
 * Here's what standard defines this format:
 * <ul>
 * 	<li>_string: _contentLength,_delimiter,_content</li>
 *  <li>_integer: <b>i</b>,_number,<b>e</b>
 *  <li>_dictionary: <b>d</b>,_string,(_string|_integer|_dictionary|_list)<b>e</b>
 * 	<li>_list: <b>l</b>,(_string|_integer|_dictionary|_list),<b>e</b></li>
 * </ul>
 * 
 * with expression defined as follow:
 * <ul>
 *  <li>_delimiter: <b>:</b></li>
 * 	<li>_contentLength: [0-9][0-9]*</li>
 *  <li>_content: .*</li>
 *  <li>_number: [0-9]*</li>
 * </ul>
 *  
 * @author Sébastien
 */
public class BencodParser {

	private static final char BENCOD_DELIMITER = ':';
	
	private BencodFileInputStream bfis;
	
	/**
	 * Constructor 
	 * @param file File to be read
	 */
	public BencodParser(BencodFileInputStream bfis) {
		this.bfis = bfis;
	}
	
	@SuppressWarnings( "unchecked" )
	public Object parse() throws IOException, BencodFileFormatException {
		
		Object object = null;
		while(bfis.available() > 0) {
			BencodDataMarker marker = determineType();
			
			if(marker == null) {
				break;
			}
		
			Object current = null;
			switch(marker) {
				case STRING:
					current = readString();
					break;
				case INTEGER:
					current = readInteger();
					break;
				case DICTIONARY: 
					current = readDictionary();
					break;
				case LIST:
					current = readList();
					break;
				case MARKER_END:
					continue;
			}
			
			if(object == null) {
				object = current;
			}
			else {
				if(object instanceof List) {
					((List<Object>) object).add(current);
				}
				else {
					List<Object> tmp = new ArrayList<Object>();
					tmp.add(object);
					tmp.add(current);
					
					object = tmp;
				}
			}
		}
		
		return object;
	}

	/**
	 * Determine type of the next chunk
	 * @return Detected type
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException Cannot determine the type, file mays be corrupted.
	 */
	private BencodDataMarker determineType() throws IOException, BencodFileFormatException {

		int nextByte = bfis.read();
		if(nextByte == -1) {
			return null;
		}
		
		char bencodMarker = (char) nextByte;
		switch(bencodMarker) {
			case 'd':
				return BencodDataMarker.DICTIONARY;
			case 'l':
				return BencodDataMarker.LIST;
			case 'i':
				return BencodDataMarker.INTEGER;
			case 'e':
				return BencodDataMarker.MARKER_END;
		}
		
		// If character is a digit, then it's the size of the next string.
		// Do not forget to replace cursor since we began to read string length.
		if(bencodMarker >= 48 && bencodMarker <= 57) {
			bfis.move(-1);
			return BencodDataMarker.STRING;
		}
		
		throw new BencodFileFormatException("Data type cannot be determined. Found '" + bencodMarker + "'.");
	}
	
	/**
	 * Read a string.
	 * @return Parsed string
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException An unexpected character has been found: invalid format?
	 */
	private String readString() throws IOException, BencodFileFormatException {
		
		long size = readSize();
		
		readDelimiter();
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 0; i < size; i++) {
			buffer.append((char) bfis.read());
		}
		
		return buffer.toString();
	}

	/**
	 * Read an integer
	 * @return Parsed integed
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException An unexpected character has been found: invalid format?
	 */
	private Integer readInteger() throws IOException, BencodFileFormatException {
		
		// Since read the size is an integer, then call the method which do the job.
		// FIXME: It could be better the readSize method use readInteger, it makes more sense. 
		int buffer = readSize();
		
		char nextMarker = (char) bfis.read();
		if(nextMarker != 'e') {
			throw new BencodFileFormatException("'e' expected. Found '" + nextMarker + "'");
		}
		
		return buffer;
	}
	
	/**
	 * Read a dictionary.
	 * @return Parsed dictionary.
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException An unexpected character has been found: invalid format?
	 */
	private Map<String, Object> readDictionary() throws IOException, BencodFileFormatException {
		
		Map<String, Object> result = new TreeMap<String, Object>();
		BencodDataMarker marker;
		
		do {
			String key;
			
			
			// Read the key (a string)
			key = readString();
			// Then read the value and add this couple to the result map.
			Object value = readValue();

			result.put(key, value);
			
			// Recheck type to see if dictionary is ended or not.
			// If not, loop until it ends. Since key still is a string,
			// the method handles the cursor rollback.
			marker = determineType();
			
		} while(marker != BencodDataMarker.MARKER_END);
		
		return result;
	}
	
	/**
	 * Read of list.
	 * @return Parsed list.
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException An unexpected character has been found: invalid format?
	 */
	private List<Object> readList() throws IOException, BencodFileFormatException {
		List<Object> result = new ArrayList<Object>();
		BencodDataMarker marker;
		
		
		do {
			Object value = readValue();
			result.add(value);
			
			// Recheck type to see if list is ended or not.
			// If not, loop until it ends. Value can be of various
			// type, that's why it must replace the cursor manually,
			// except for string (already done).
			marker = determineType();
			if(marker != BencodDataMarker.MARKER_END && marker != BencodDataMarker.STRING) {
				bfis.move(-1);
			}
			
		} while(marker != BencodDataMarker.MARKER_END);
		
		return result;
	}
	
	/**
	 * Read string length (extended to read any number)
	 * @return String length
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException An unexpected character has been found: invalid format?
	 */
	private int readSize() throws IOException, BencodFileFormatException {
		
		int size = 0;
		
		boolean isFirst = true;
		int currentDigit;
		do {
			currentDigit = bfis.read();

			// As soon as a digit is found, multiply by 10 to shift and add new digit.
			if(currentDigit >= 48 && currentDigit <= 57) {
				size *= 10;
				size += (currentDigit - 48);
				isFirst = false;
			}
			// At least one digit must be found
			else if(isFirst) {
				throw new BencodFileFormatException("Digit expected. Found : '" + ((char) currentDigit) + "/" + currentDigit + "'");
			}
		}
		// Continue until no digit are available anymore 
		while(currentDigit >= 48 && currentDigit <= 57);
		
		// If the next character is not a digit, rollback cursor.
		bfis.move(-1);
		
		return size;
	}
	
	/**
	 * Read delimiter and check if it is valid
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException Delimiter is not correct.
	 */
	private void readDelimiter() throws BencodFileFormatException, IOException {
		
		int delim = bfis.read();
		if(((char) delim) != BENCOD_DELIMITER) {
			throw new BencodFileFormatException("Character ':' expected. Found : '" + ((char) delim) + "'");
		}
	}
	
	/**
	 * Read a collection value.
	 * @return Collection value of type {@link Object}.
	 * @throws IOException File cannot be read properly
	 * @throws BencodFileFormatException An unexpected character has been found: invalid format?
	 */
	private Object readValue() throws BencodFileFormatException, IOException {
		Object value = null;
		BencodDataMarker marker = determineType();
		
		if(marker == null) {
			throw new BencodFileFormatException("No marker. 'e' expected.");
		}
		
		switch(marker) {
			case STRING:
				value = readString();
				break;
			case INTEGER:
				value = readInteger();
				break;
			case DICTIONARY: 
				value = readDictionary();
				break;
			case LIST:
				value = readList();
				break;
			case MARKER_END:
				break;
		}
		
		return value;
	}
}
