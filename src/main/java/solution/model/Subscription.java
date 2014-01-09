package solution.model;

import java.io.Serializable;

public class Subscription implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String user;
	private int count;
	private String filename;
	
	public Subscription() {
		user = "";
		count = 0;
		filename = "";
	}
	
	public Subscription(String user, int count) {
		this.user = user;
		this.count = count;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
