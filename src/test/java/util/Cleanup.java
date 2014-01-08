package util;

/**
 * Cleans up, after LoadSimulator, in case you want to get rid of additional
 * user-credentials and files. Make sure, you have following files already in
 * the files directory (they will not be affected): files/client/upload.txt
 * files/fs#/short.txt files/fs#/long.txt files/fs#/multiline.txt
 */
public class Cleanup {

	public static void main(String[] args) {

		cleanup();
	}
	
	public static void cleanup() {
		
		Util.resetUsers();
		FileGenerator.resetDirectories();
	}
}