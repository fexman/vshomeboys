package solution.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Various functions to read/write files to/from path
 * @author Felix
 *
 */

public class FileUtils {
	
	/**
	 * 
	 * @param path Path of file to read
	 * @return byte-array of files content
	 * @throws IOException in case of read error
	 */
	public static byte[] getBytesFromFile(String path) throws IOException {
		
		File f = new File(path);
		if (!f.exists()) {
			throw new FileNotFoundException("File: \"" + path + "\" not found!");
		}
		
		FileInputStream fin = new FileInputStream(f);
		byte[] content = new byte[(int)f.length()];
		fin.read(content);
		fin.close();
		return content;
		
	}
	
	/**
	 * 
	 * @param path Path of file to write
	 * @param content content of file to write
	 * @throws IOException IOException in case of read error
	 */
	public static void writeBytesToFile(String path, byte[] content) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			fos.write(content);
			fos.close();
		} catch (IOException e) {
			if (fos != null) {
				fos.close();
			}
			throw new IOException(e);
		}	
	}
	
	/**
	 * Constructs java.io.File from path
	 * @param path Path of file to read
	 * @return
	 * @throws FileNotFoundException if file w. given path does not exist
	 */
	public static File getFile(String path) throws FileNotFoundException {
		File f = new File(path);
		if (!f.exists()) {
			throw new FileNotFoundException("File: \"" + path + "\" not found!");
		}
		return f;
	}
}
