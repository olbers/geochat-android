package org.instedd.geochat;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.RestClient;

import android.content.Context;
import android.content.SharedPreferences.Editor;


public class GeoChatSettings {
	
	public final static String SHARED_PREFS_NAME = "org.instedd.geochat.settings";
	
	public final static String USER = "user";
	public final static String PASSWORD = "password";

	private final Context context;
	
	public GeoChatSettings(Context context) {
		this.context = context;
	}
	
	public String getUser() {
		return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getString(USER, "");
	}
	
	public String getPassword() {
		return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getString(PASSWORD, "");
	}
	
	public void setUserAndPassword(String user, String password) {
		Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(USER, user);
		editor.putString(PASSWORD, password);
		editor.commit();
	}
	
	public IGeoChatApi newApi() {
		return new GeoChatApi(new RestClient(), getUser(), getPassword());
	}

}
