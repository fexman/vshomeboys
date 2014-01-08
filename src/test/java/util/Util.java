package util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Util {

	public static final int WAIT_FOR_COMPONENT_STARTUP = 2000;

	/**
	 * User values according to template
	 */
	public static final String STANDARD_USERS = "alice.credits=200\nalice.password=12345\nbill.credits=200\nbill.password=23456";

	/**
	 * Adds users with credits and passwords to the users.property file. The
	 * names follow the pattern "u" + n, where n starts with 0 and is
	 * incremented by 1, and the password is n.
	 * 
	 * @param noOfUsers
	 *            > 0, number of users created
	 */
	public static void addUsers(int noOfUsers) {

		PrintWriter printer = null;

		try {

			printer = new PrintWriter("src/main/resources/user.properties");
			printer.println(STANDARD_USERS);
			for (int i = 0; i < noOfUsers; i++) {

				printer.println("u" + i + ".credits=1000");
				printer.println("u" + i + ".password=12345");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} finally {

			printer.close();
		}
	}
}