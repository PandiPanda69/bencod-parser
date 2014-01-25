package fr.thedestiny.bencod.parser;

/**
 * Exception thrown when bencod file are corrupted.
 * @author Sébastien
 */
public class BencodFileFormatException extends Exception {

	private static final long	serialVersionUID	= -1366819382705074371L;
	
	/**
	 * Constructor
	 * @param msg Cause of the exception.
	 */
	public BencodFileFormatException(String msg) {
		super(msg);
	}

}
