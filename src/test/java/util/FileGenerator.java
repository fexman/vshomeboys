package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Util class that handles all kinds of file manipulation in the 'files'
 * directory, like reseting or generating new files with random content. It also
 * keeps track of already existing files in the 'files/client' directory.
 */
public class FileGenerator {

	public final static int UTF8_RANGE_MIN = 0x30; // '0' alpha-numerical
	public final static int UTF8_RANGE_MAX = 0x7E; // '~' values
	public final static String UPLOAD_TXT = "dslab13";
	public final static String SHORT_TXT = "Bitte die Lehrveranstaltung im TISS bewerten. ;-)";
	public final static String LONG_TXT = "#####\n#####\n\n#####\n#####\n\n#####\n#####\n\n#####\n";

	private ArrayList<String> existing; // client files
	private Random random;
	private int id, size;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            > 0, size, that generated files will have
	 */
	public FileGenerator(int size) {

		existing = new ArrayList<String>();
		random = new Random();
		this.size = size;
		id = 0;

		resetDirectories();

		File dir = new File("files/client");

		for (File f : dir.listFiles()) {

			existing.add(f.getName());
		}
	}

	/**
	 * Returns an already existing file in the client directory
	 * 
	 * @return name of the file
	 */
	public String getExistingFile() {

		return existing.get(Math.abs(random.nextInt()) % existing.size());
	}

	/**
	 * Generates a new file with random content an unique name in the client
	 * directory
	 * 
	 * @return name of the file
	 */
	public String getNewFile() {

		String name = "f" + (id++);
		generateRandomFile("files/client", name, size);
		existing.add(name);
		return name;
	}

	/**
	 * Generates a file with random content (alpha-numerical values in the range
	 * between 0x30 and 0x7E in UTF-8 encoding) with specified size and writes
	 * it to the given directory
	 * 
	 * @param directory
	 *            != null, directory, in which the file is stored
	 * @param name
	 *            != filename the file is given
	 * @param size
	 *            > 0, size in kB the file will have
	 */
	public void generateRandomFile(String directory, String name, int size) {

		char[] content = new char[size * 1000];

		for (int i = 0; i < content.length; i++) {
			content[i] = (char) ((Math.abs(random.nextInt()) % (UTF8_RANGE_MAX
					- UTF8_RANGE_MIN + 1)) + UTF8_RANGE_MIN);
		}

		generateFile(directory, name, new String(content));
	}

	/**
	 * Resets all the files/x directories to equal the template, where x is
	 * /client, and /fileserver1-4. That means: client: delete all files except
	 * upload.txt; fileservers: delete all files except short.txt, long.txt and
	 * multiline.txt
	 */
	public static void resetDirectories() {

		String[] fs = { "fileserver1", "fileserver2", "fileserver3",
				"fileserver4" };

		for (String s : fs) {
			generateFile("files/" + s, "short.txt", SHORT_TXT);
			generateFile("files/" + s, "long.txt", LONG_TXT);
		}

		generateFile("files/client", "upload.txt", UPLOAD_TXT);

		deleteAllFilesOfDirectoryExcept("files/client", "upload.txt");
		deleteAllFilesOfDirectoryExcept("files/fileserver1", "long.txt",
				"short.txt");
		deleteAllFilesOfDirectoryExcept("files/fileserver2", "long.txt",
				"short.txt");
		deleteAllFilesOfDirectoryExcept("files/fileserver3", "long.txt",
				"short.txt");
		deleteAllFilesOfDirectoryExcept("files/fileserver4", "long.txt",
				"short.txt");
	}

	private static void generateFile(String directory, String name,
			String content) {

		PrintWriter print = null;

		try {

			print = new PrintWriter(directory + "/" + name, "UTF-8");
			print.print(content);

		} catch (FileNotFoundException e) {
			System.err.println("Error: Writing file.");
			e.printStackTrace();

		} catch (UnsupportedEncodingException e) {
			System.err.println("Error: Encoding file.");
			e.printStackTrace();

		} finally {
			print.close();
		}
	}

	private static void deleteAllFilesOfDirectoryExcept(String directory,
			String... exceptions) {

		File dir = new File(directory);

		for (File f : dir.listFiles()) {
			if (!Arrays.asList(exceptions).contains(f.getName())) {
				f.delete();
			}
		}
	}
}