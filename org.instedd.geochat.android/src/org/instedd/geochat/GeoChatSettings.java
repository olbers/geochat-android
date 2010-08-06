package org.instedd.geochat;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.RestClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GeoChatSettings {
	
	public final static String SHARED_PREFS_NAME = "org.instedd.geochat.settings";
	
	public final static String USER = "user";
	public final static String PASSWORD = "password";
	public final static String NEW_MESSAGES_COUNT = "new_messages";
	public final static String COMPOSE_GROUP = "compose_group";
	public final static String SILENT_REPORT_LOCATIONS = "silent_report_locations";
	public final static String GEOCHAT_NUMBER = "geochat_number";
	
	private final Context context;
	
	public GeoChatSettings(Context context) {
		this.context = context;
	}
	
	public String getUser() {
		return openRead().getString(USER, null);
	}
	
	public String getPassword() {
		return openRead().getString(PASSWORD, null);
	}
	
	public void setUserAndPassword(String user, String password) {
		Editor editor = openWrite();
		editor.putString(USER, user);
		editor.putString(PASSWORD, password);
		editor.commit();
	}
	
	public String getComposeGroup() {
		return openRead().getString(COMPOSE_GROUP, null);
	}
	
	public void setComposeGroup(String group) {
		Editor editor = openWrite();
		if (group == null) {
			editor.remove(COMPOSE_GROUP);
		} else {
			editor.putString(COMPOSE_GROUP, group);
		}
		editor.commit();
	}
	
	public void clearNewMessagesCount() {
		Editor editor = openWrite();
		editor.remove(NEW_MESSAGES_COUNT);
		editor.commit();
	}
	
	public int getNewMessagesCount() {
		return openRead().getInt(NEW_MESSAGES_COUNT, 0);
	}
	
	public void setNewMessagesCount(int count) {
		Editor editor = openWrite();
		editor.putInt(NEW_MESSAGES_COUNT, count);
		editor.commit();
	}
	
	public boolean isSilentReportLocationsEnabled() {
		return openRead().getBoolean(SILENT_REPORT_LOCATIONS, true);
	}
	
	public String getGeoChatNumber() {
		return openRead().getString(GEOCHAT_NUMBER, null);
	}
	
	public IGeoChatApi newApi() {
		return new GeoChatApi(new RestClient(), getUser(), getPassword());
	}
	
	private SharedPreferences openRead() {
		return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	private Editor openWrite() {
		return openRead().edit();
	}

	public void clearUserData() {
		Editor editor = openRead().edit();
		editor.remove(NEW_MESSAGES_COUNT);
		editor.remove(COMPOSE_GROUP);
		editor.commit();
	}

}
