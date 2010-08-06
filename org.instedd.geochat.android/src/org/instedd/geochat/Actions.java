package org.instedd.geochat;

import java.util.List;

import org.instedd.geochat.map.GeoChatMapActivity;
import org.instedd.geochat.map.LatLng;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

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
	
	public static void settings(Context context) {
		startActivity(context, GeoChatPreferences.class);
	}
	
	public static void showUserInMap(Context context, String login) {
		startActivity(context, GeoChatMapActivity.class, Uris.userLogin(login));
	}
	
	public static void showMessageInMap(Context context, int id) {
		startActivity(context, GeoChatMapActivity.class, Uris.messageId(id));
	}
	
	public static void openUser(Context context, String login) {
		startActivity(context, MessagesActivityWithTitleBar.class, Uris.userMessages(login));
	}
	
	public static void openGroup(Context context, int groupId) {
		startActivity(context, GroupActivity.class, Uris.groupId(groupId));
	}
	
	public static void openGroup(Context context, String groupAlias) {
		startActivity(context, GroupActivity.class, Uris.groupAlias(groupAlias));
	}
	
	public static void reportMyLocation(final Context context, final Handler handler) {
		final Resources res = context.getResources();
		
		final Toast toast = Toast.makeText(context, res.getString(R.string.retreiving_your_location), Toast.LENGTH_LONG);
		toast.show();
		
		new Thread() {
			public void run() {
				final LatLng location = getLocation(context);
				
				handler.post(new Runnable() {
					public void run() {
						toast.cancel();
					}
				});
				
		        if (location == null) {
		        	handler.post(new Runnable() {
						public void run() {
							Toast.makeText(context, res.getString(R.string.could_not_retrieve_your_location), Toast.LENGTH_LONG).show();
						}
					});
		        } else {
		        	GeoChatSettings settings = new GeoChatSettings(context);
		        	final String message;
		        	if (settings.isSilentReportLocationsEnabled()) {
		        		message = "#my location " + location.lat + ", " + location.lng;	
		        	} else {
		        		message = "at " + location.lat + ", " + location.lng;
		        	}
		        	
		        	Messenger messenger = new Messenger(context);
		        	int result = messenger.sendMessage(message);
		        	switch(result) {
		        	case Messenger.SENT_VIA_API:
		        	case Messenger.SENT_VIA_SMS:
		        		handler.post(new Runnable() {
							public void run() {
								Toast.makeText(context, res.getString(R.string.sent) + ": " + message, Toast.LENGTH_LONG).show();
							}
						});
		        		break;
		        	case Messenger.NOT_SENT_NO_GEOCHAT_NUMBER:
		        		handler.post(new Runnable() {
		        			public void run() {
		        				Toast.makeText(context, context.getResources().getString(R.string.message_could_not_be_sent), Toast.LENGTH_LONG).show();
		        			}
		        		});
		        		break;
		        	}
		        }
			};
		}.start();
	}
	
	public static LatLng getLocation(Context context) {
		LocationManager man = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		List<String> providerNames = man.getAllProviders();
		
		double bestTime = 0;
		double lat = 0;
		double lng = 0;
        
		// TODO this just checks the most recent location,
		// without taking into account the provider's accuracy
        for(String providerName : providerNames) {
        	Location location = man.getLastKnownLocation(providerName);
        	if (location != null) {
        		double time = location.getTime();
        		if (time <= bestTime)
        			continue;
        		
        		bestTime = time;
	        	lat = location.getLatitude();
	        	lng = location.getLongitude();
        	}
        }
        
        if (bestTime == 0) {
        	return null;
        } else {
        	return new LatLng(lat, lng);
        }
	}
	
	private static void startActivity(Context context, Class<?> clazz) {
		context.startActivity(new Intent().setClass(context, clazz));
	}
	
	private static void startActivity(Context context, Class<?> clazz, Uri data) {
		context.startActivity(new Intent().setClass(context, clazz).setData(data));
	}
	
	private Actions() { }

}
