package org.instedd.geochat.sync;

import org.instedd.geochat.GeoChatSettings;
import org.instedd.geochat.Notifier;
import org.instedd.geochat.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class GeoChatService extends CompatibilityService implements OnSharedPreferenceChangeListener {
	
	public class LocalBinder extends Binder {
		public GeoChatService getService() {
            return GeoChatService.this;
        }
    }
	
	final IBinder mBinder = new LocalBinder();
	
	Synchronizer synchronizer;
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
		
		this.displayForegroundNotification();
		
		// Listen for preference changes to resync when that happens
		this.getSharedPreferences(GeoChatSettings.SHARED_PREFS_NAME, 0)
			.registerOnSharedPreferenceChangeListener(this);
		
		// Listen for network changes
		this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	private void displayForegroundNotification() {
		String title = getResources().getString(R.string.app_name);
		String content = getResources().getString(R.string.signed_in_as_user, new GeoChatSettings(this).getUser());
		Notification notification = new Notification(R.drawable.ic_stat_geochat, null, System.currentTimeMillis());
		notification.setLatestEventInfo(this, title, content, PendingIntent.getActivity(this, 0, Notifier.getViewMessagesIntent(this), 0));
		startForegroundCompat(Notifier.SERVICE, notification);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (GeoChatSettings.USER.equals(key)
				|| GeoChatSettings.PASSWORD.equals(key)
				|| GeoChatSettings.REFRESH_RATE.equals(key)) {
			synchronizer.resync();
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		synchronizer.start();
	}
	
	@Override
	public void onDestroy() {
		stopSynchronizing();
		
		// Stop listening for preference changes
		this.getSharedPreferences(GeoChatSettings.SHARED_PREFS_NAME, 0)
			.unregisterOnSharedPreferenceChangeListener(this);
		
		// Unregister the network changes receiver
		this.unregisterReceiver(receiver);
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void stopSynchronizing() {
		synchronizer.stop();
		
		// Remove foreground notification
		stopForegroundCompat(Notifier.SERVICE);
	}
	
	public void resyncMessages() {
		synchronizer.resyncMessages();
	}
	
	public void resyncMessages(String groupAlias) {
		synchronizer.resyncMessages(groupAlias);
	}

}
