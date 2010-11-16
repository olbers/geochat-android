package org.instedd.geochat;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

public class GeoChatPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private GeoChatSettings settings;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.settings);
        getPreferenceManager().setSharedPreferencesName(GeoChatSettings.SHARED_PREFS_NAME);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        this.settings = new GeoChatSettings(this);
        updateGeoChatNumber();
        updateRefreshRate();
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (GeoChatSettings.GEOCHAT_NUMBER.equals(key)) {
			updateGeoChatNumber();
		} else if (GeoChatSettings.REFRESH_RATE.equals(key)) {
			updateRefreshRate();
		}
	}

	private void updateGeoChatNumber() {
		String number = new GeoChatSettings(this).getGeoChatNumber();
		Preference preference = findPreference(GeoChatSettings.GEOCHAT_NUMBER);
		if (number == null || TextUtils.getTrimmedLength(number) == 0) {
			preference.setSummary(R.string.geochat_number_summary);
		} else {
			preference.setSummary(getResources().getString(R.string.geochat_number_set_summary, number));
		}
	}
	
	private void updateRefreshRate() {
		String refreshRate = String.valueOf(settings.getRefreshRateInMinutes());
		ListPreference preference = (ListPreference) findPreference(GeoChatSettings.REFRESH_RATE);
		for (int i = 0; i < preference.getEntryValues().length; i++) {
			CharSequence entry = preference.getEntryValues()[i];
			if (entry.equals(refreshRate)) {
				preference.setSummary(preference.getEntries()[i]);
				break;
			}
		}
	}

}
