package org.instedd.geochat;

import org.instedd.geochat.map.GeoChatMapActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Actions {
	
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
		context.startActivity(new Intent().setClass(context, ComposeActivity.class).setData(data));
	}
	
	public static void map(Context context) {
		context.startActivity(new Intent().setClass(context, GeoChatMapActivity.class));
	}

}
