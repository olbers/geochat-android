package org.instedd.geochat.api;

import java.util.TreeSet;

public class User implements Comparable<User> {
	
	public String login;
	public String displayName;
	public double lat;
	public double lng;
	public TreeSet<String> groups;
	
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

	public int compareTo(User another) {
		return login.toLowerCase().compareTo(another.login.toLowerCase());
	}
	
	@Override
	public String toString() {
		if (displayName == null || displayName.length() == 0)
			return login;
		return login + " (" + displayName + ")";
	}

}
