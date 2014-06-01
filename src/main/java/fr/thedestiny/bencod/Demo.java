package fr.thedestiny.bencod;

import fr.thedestiny.bencod.io.BencodFileInputStream;
import fr.thedestiny.bencod.parser.BencodParser;

public class Demo {

	private static String getExecutableFile() {
		return Demo.class.getProtectionDomain().getCodeSource().getLocation().getFile();
	}
	
	private static String getUsage(String filename) {
		return filename + " <bencoded_file>\n" +
				"\tbencoded_file: File to parse.\n";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if(args.length != 1) {
			System.out.println(getUsage(getExecutableFile()));
			System.exit(1);
		}
		
		BencodFileInputStream bfis = new BencodFileInputStream(args[0]);
		BencodParser reader = new BencodParser(bfis);
		
		long A = System.currentTimeMillis();
		Object result = reader.parse();
		long B = System.currentTimeMillis();
		
		System.out.println("Read in " + (B-A) + " ms.");
		System.out.println(result);
		
		bfis.close();
	}

}
