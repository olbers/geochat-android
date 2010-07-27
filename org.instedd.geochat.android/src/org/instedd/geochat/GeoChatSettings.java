package org.instedd.geochat;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class GeoChatSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public final static String SHARED_PREFS_NAME = "org.instedd.geochat.settings";
	
	public final static String USER = "user";
	public final static String PASSWORD = "password";
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);
        addPreferencesFromResource(R.layout.settings);
        
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        updateUser();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (USER.equals(key)) {
			updateUser();
		}
	}
	
	private void updateUser() {
		String user = getPreferenceManager().getSharedPreferences().getString(USER, null);
		if (user != null) {
        	((EditTextPreference) getPreferenceScreen().findPreference(USER)).setSummary(user);
        } else {
        	((EditTextPreference) getPreferenceScreen().findPreference(USER)).setSummary(R.string.user_summary);
        }
	}

}
