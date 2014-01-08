package util;

/**
 * Cleans up, after LoadSimulator, in case you want to get rid of additional
 * user-credentials and unifies all files in the 'files' directory for easier
 * manual testing across computers.
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