package solution.util;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import solution.model.MyUserInfo;

public class UserConfigParser {

	/**
	 * Returns a HashMap with username as key and MyUserInfo as value.
	 * Data is parsed from user.properties.
	 * If incomplete or invalid data is found a RunTimeException will be thrown
	 * If no valid data found a empty map will be returned.
	 * @return
	 */
	public static ConcurrentHashMap<String, MyUserInfo> getUserMap() {
		
		ConcurrentHashMap<String, MyUserInfo> users = new ConcurrentHashMap<String, MyUserInfo>();
		
		ResourceBundle bundle = ResourceBundle.getBundle("user");
		Enumeration<String> values =bundle.getKeys();
		
		while (values.hasMoreElements()) {

			String nextLine = values.nextElement();
			String[] line = nextLine.split("\\.",2);
			
			if (line.length != 2) { //Structure valid?
				throw new RuntimeException("Invalid structure: " + line.length + line[0]);
			}
			
			MyUserInfo user;
			if (users.containsKey(line[0])) { //Credits / password of user have already been read?
				user = users.get(line[0]);
			} else {
				user = new MyUserInfo(line[0]);
				users.put(line[0], user);
			}
			
			if (line[1].equals("credits")) { //Set credits or password
				user.setCredits(Long.parseLong(bundle.getString(nextLine)));
			} else if (line[1].equals("password")) {
				user.setPassword(bundle.getString(nextLine));
			} else {
				throw new RuntimeException("Invalid property: " + line[1]);
			}
			
		}
		
		for (MyUserInfo u : users.values()) { //Everything set?
			if (!u.isValid()) {
				throw new RuntimeException("Invalid user: \"" + u.getName() + "\" - credits or password not set.");
			}
		}
		
		
		return users;
	}
}
