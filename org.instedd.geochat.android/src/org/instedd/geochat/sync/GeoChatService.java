package org.instedd.geochat.sync;

import org.instedd.geochat.GeoChatSettings;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;

public class GeoChatService extends Service implements OnSharedPreferenceChangeListener {
	
	private Synchronizer synchronizer;
	final Handler handler = new Handler();
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				synchronizer.connectivityChanged();
				synchronizer.resync();
			}
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.synchronizer = new Synchronizer(this, handler);
		
		// Listen for preference changes to resync when that happens
		this.getSharedPreferences(GeoChatSettings.SHARED_PREFS_NAME, 0)
			.registerOnSharedPreferenceChangeListener(this);
		
		// Listen for network changes
		this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (GeoChatSettings.USER.equals(key)
				|| GeoChatSettings.PASSWORD.equals(key)) {
			synchronizer.resync();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		synchronizer.start();
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		synchronizer.stop();
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}	

}
