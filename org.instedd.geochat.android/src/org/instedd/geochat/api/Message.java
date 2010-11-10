package org.instedd.geochat.api;

public class Message {
	
	public String guid;
	public String fromUser;
	public String toGroup;
	public String message;
	public double lat;
	public double lng;
	public long createdDate;
	
	@Override
	public String toString() {
		return message + " (from " + fromUser + " to " + toGroup + ")";
	}

}
