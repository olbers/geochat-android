package org.instedd.geochat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GeoChatPreferences extends PreferenceActivity {
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(GeoChatSettings.SHARED_PREFS_NAME);
        addPreferencesFromResource(R.layout.settings);
    }

}
