package org.instedd.geochat;

import org.instedd.geochat.map.GeoChatMapActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class Actions {
	
	private final static String PREFIX = "org.instedd.geochat.";
	
	public final static String VIEW_MESSAGES = PREFIX + "view_messages";
	
	public static void home(Context context) {
		context.startActivity(new Intent().setClass(context, HomeActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	public static void compose(Context context) {
		compose(context, null);
	}
	
	public static void compose(Context context, Uri data) {
		startActivity(context, ComposeActivity.class, data);
	}
	
	public static void compose(Context context, int groupId) {
		compose(context, Uris.groupId(groupId));
	}
	
	public static void map(Context context) {
		startActivity(context, GeoChatMapActivity.class);
	}
	
	public static void showUserInMap(Context context, String login) {
		startActivity(context, GeoChatMapActivity.class, Uris.userLogin(login));
	}
	
	public static void viewUserMessages(Context context, String login) {
		startActivity(context, MessagesActivityWithTitleBar.class, Uris.userMessages(login));
	}
	
	public static void openGroup(Context context, int groupId) {
		startActivity(context, GroupActivity.class, Uris.groupId(groupId));
	}
	
	private static void startActivity(Context context, Class<?> clazz) {
		context.startActivity(new Intent().setClass(context, clazz));
	}
	
	private static void startActivity(Context context, Class<?> clazz, Uri data) {
		context.startActivity(new Intent().setClass(context, clazz).setData(data));
	}
	
	private Actions() { }

}
