package org.instedd.geochat.api;

public class User implements Comparable<User> {
	
	public String login;
	public String displayName;
	
	public User() {
	}
	
	public User(String login, String displayName) {
		this.login = login;
		this.displayName = displayName;
	}
	
	@Override
	public int hashCode() {
		return login.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof User)) {
			return false;
		}
		User other = (User) o;
		return login.toLowerCase().equals(other.login.toLowerCase());
	}

	@Override
	public int compareTo(User another) {
		return login.toLowerCase().compareTo(another.login.toLowerCase());
	}

}
