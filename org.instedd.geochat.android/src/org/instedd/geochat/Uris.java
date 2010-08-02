package org.instedd.geochat;

import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Users;

import android.net.Uri;

public final class Uris {	
	
	public static Uri userId(int userId) {
		return Uri.withAppendedPath(Users.CONTENT_URI, String.valueOf(userId));
	}
	
	public static Uri userLogin(String login) {
		return Uri.withAppendedPath(Users.CONTENT_URI, login);
	}
	
	public static Uri userMessages(String login) {
		return Uri.withAppendedPath(Uri.withAppendedPath(Users.CONTENT_URI, String.valueOf(login)), "messages");
	}
	
	public static Uri groupId(int groupId) {
		return Uri.withAppendedPath(Groups.CONTENT_URI, String.valueOf(groupId));
	}
	
	private static Uri groupAlias(String groupAlias) {
		return Uri.withAppendedPath(Groups.CONTENT_URI, groupAlias);
	}
	
	public static Uri groupUsers(String groupAlias) {
		return Uri.withAppendedPath(groupAlias(groupAlias), "users");
	}
	
	public static Uri groupMessages(String groupAlias) {
		return Uri.withAppendedPath(groupAlias(groupAlias), "messages");
	}
	
	public static Uri groupOldMessages(String groupAlias) {
		return Uri.withAppendedPath(groupMessages(groupAlias), "old");
	}
	
	public static Uri groupLastMessage(String groupAlias) {
		return Uri.withAppendedPath(groupMessages(groupAlias), "last");
	}
	
	private Uris() { }

}
