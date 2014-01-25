package fr.thedestiny.bencod.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import fr.thedestiny.bencod.parser.BencodParser;

/**
 * Input stream required by {@link BencodParser} to read bencod file. 
 * @author Sébastien
 */
public class BencodFileInputStream extends InputStream {

	private byte[] buffer;
	
	private int offset;
	private int bufferSize;
	
	/**
	 * Constructor
	 * @param filename File name of the file to read
	 * @throws IOException File cannot be read.
	 */
	public BencodFileInputStream(String filename) throws IOException {
		this(new File(filename));
	}
	
	/**
	 * Constructor
	 * @param file File instance of the file to read
	 * @throws IOException File cannot be read
	 */
	public BencodFileInputStream(File file) throws IOException {
		
		// Check file is a regular file.
		if(file == null || !file.exists() || !file.isFile()) {
			throw new FileNotFoundException();
		}
		
		bufferizeData(file);
	}
	
	/**
	 * Bufferize file to simplify file operations.
	 * @param file File to be read
	 * @throws IOException An error occured while reading the file.
	 */
	private void bufferizeData(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		
		this.bufferSize = fis.available() + 1;
		this.offset = 0;
		
		buffer = new byte[this.bufferSize];
		
		fis.read(buffer);

		fis.close();
	}
	
	@Override
	public int available() {
		return (bufferSize - offset);
	}
	
	@Override
	public int read() throws IOException {
		
		if((this.offset + 1) >= this.bufferSize) {
			return -1;
		}
		
		return this.buffer[this.offset++];
	}
	
	/**
	 * Move forward/backward of <em>off</em> bytes.
	 * If <em>off<em> < 0 then, it moves backward, else forward.
	 * @param off Number of bytes the current cursor must move.
	 */
	public void move(int off) {
		if((this.offset + off) >= this.bufferSize) {
			this.offset = this.bufferSize;
		}
		else if((this.offset + off) <= 0) {
			this.offset = 0;
		}
		else {
			this.offset += off;
		}
	}
}
