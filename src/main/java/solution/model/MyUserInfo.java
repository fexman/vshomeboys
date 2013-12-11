package solution.model;

import model.UserInfo;

public class MyUserInfo {
	
	private String name;
	private String password;
	private Long credits;
	private volatile boolean online;
	
	public MyUserInfo(String name) {
		this.name = name;
		this.online = false;
	}

	public MyUserInfo(String name, long credits, String password) {
		this.name = name;
		this.password = password;
		this.credits = credits;
		this.online = false;
	}

	public long getCredits() {
		return credits;
	}

	public void setCredits(long credits) {
		this.credits = credits;
	}
	
	public synchronized void modifyCredits(long credits) {
		this.credits += credits;
	}

	public boolean isOnline() {
		return online;
	}

	public synchronized boolean login(String password) {
		if (!password.equals(this.password) || this.online) {
			return false;
		}
		
		online = true;
		
		return online;
	}
	
	public void logout() {
		online = false;
	}

	public String getName() {
		return name;
	}

	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public UserInfo toUserInfo() {
		return new UserInfo(name,credits,online);
	}
	
	public boolean isValid() {
		return (credits != null && password != null);
	}

}
