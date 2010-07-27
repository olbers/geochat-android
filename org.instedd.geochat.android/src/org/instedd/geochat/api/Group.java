package org.instedd.geochat.api;

public class Group implements Comparable<Group> {
	
	public String alias;
	public String name;
	public double lat;
	public double lng;
	
	@Override
	public int hashCode() {
		return alias.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Group)) {
			return false;
		}
		Group other = (Group) o;
		return alias.toLowerCase().equals(other.alias.toLowerCase());
	}

	@Override
	public int compareTo(Group another) {
		return alias.toLowerCase().compareTo(another.alias.toLowerCase());
	}

}
