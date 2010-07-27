package org.instedd.geochat;

import java.util.Set;
import java.util.TreeSet;

import org.instedd.geochat.GeoChat.Groups;
import org.instedd.geochat.GeoChat.Locations;
import org.instedd.geochat.GeoChat.Messages;
import org.instedd.geochat.GeoChat.Users;
import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.Message;
import org.instedd.geochat.api.RestClient;
import org.instedd.geochat.api.User;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

public class SyncService extends Service implements OnSharedPreferenceChangeListener {
	
	private IGeoChatApi api;
	private SyncThread syncThread;
	private boolean running;
	private boolean resync;
	private boolean connectivityChanged;
	private LocationResolver locationResolver;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				connectivityChanged = true;
				resync();
			}
		}
	};
	
	private synchronized void startSync() {
		if (running) return;
		
		running = true;
		
		syncThread = new SyncThread();
		syncThread.start();
	}
	
	private synchronized void stopSync() {
		running = false;
	}
	
	private synchronized void resync() {
		if (!connectivityChanged) {
			recreateApi();
		}
		this.resync = true;
	}
	
	private synchronized void resyncFinished() {
		this.resync = false;
	}
	
	private void recreateApi() {
		SharedPreferences prefs = this.getSharedPreferences(GeoChatSettings.SHARED_PREFS_NAME, 0);
		String user = prefs.getString(GeoChatSettings.USER, null);
		if (TextUtils.isEmpty(user)) {
			api = null;
			return;
		}
		
		String password = prefs.getString(GeoChatSettings.PASSWORD, null);
		
		api = new GeoChatApi(new RestClient(), user, password);	
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.locationResolver = new LocationResolver(this);
		
		recreateApi();
		
		// Listen for preference changes to resync when that happens
		this.getSharedPreferences(GeoChatSettings.SHARED_PREFS_NAME, 0)
			.registerOnSharedPreferenceChangeListener(this);
		
		// Listen for network changes
		this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (GeoChatSettings.USER.equals(key)
				|| GeoChatSettings.PASSWORD.equals(key)) {
			resync();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		startSync();
	}
	
	@Override
	public void onDestroy() {
		stopSync();
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class SyncThread extends Thread {
		@Override
		public void run() {
			while(running) {
				if (api != null) {
					boolean hasConnectivity = hasConnectivity();
					boolean credentialsAreValid = false;
					if (hasConnectivity) {
						try {
							credentialsAreValid = api.credentialsAreValid();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (resync) {
						// Delete everything if credentials changed
						if (!connectivityChanged) {
							if (credentialsAreValid) {
								getContentResolver().delete(Groups.CONTENT_URI, null, null);
								getContentResolver().delete(Users.CONTENT_URI, null, null);
								getContentResolver().delete(Messages.CONTENT_URI, null, null);
								getContentResolver().delete(Locations.CONTENT_URI, null, null);
							} else {
								notifyWrongCredentials();
							}
						}
						connectivityChanged = false;
						
						resyncFinished();
					}
					
					try {
						if (hasConnectivity && credentialsAreValid) {
							Group[] groups = syncGroups();
							if (resync) continue;
							
							syncUsers(groups);
							if (resync) continue;
							
							syncMessages(groups);
							if (resync) continue;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				// Wait 15 minutes in intervals of 5 seconds,
				// so wait
				try {
					for(int i = 0; i < 3 * 60 * 6000; i++) {
						sleep(5);
						if (resync) break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void notifyWrongCredentials() {
		}
		
		private void notifyNewMessages(int count) {
			
		}

		private boolean hasConnectivity() {
			ConnectivityManager conn = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo info = conn.getActiveNetworkInfo();
			return info != null && info.isConnected();
		}

		private Group[] syncGroups() {
			// Get groups from server and sort them by alias
			Set<Group> serverGroupsList = new TreeSet<Group>();
			int page = 1;
			while(true) {
				if (resync) return null;
				
				try {
					Group[] serverGroups = api.getGroups(page);
					if (serverGroups.length == 0) {
						break;
					}
					for(Group serverGroup : serverGroups) {
						serverGroupsList.add(serverGroup);
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				page++;
			}
			if (resync) return null;
			
			Group[] serverGroups = serverGroupsList.toArray(new Group[serverGroupsList.size()]);
			if (resync) return null;
			
			// Get cached groups sorted by alias
			Cursor c = getContentResolver().query(Groups.CONTENT_URI, new String[] { Groups._ID, Groups.ALIAS }, null, null, Groups.ALIAS);
			
			try {
				int serverIndex = 0;
				int cachedIndex = 0;
				c.moveToNext();
				
				while(serverIndex < serverGroups.length || cachedIndex < c.getCount()) {
					if (resync) return null;
					
					int comparison;
					if (serverIndex >= serverGroups.length) {
						// Same as cached group not found
						comparison = 1;
					} else if (cachedIndex >= c.getCount()) {
						// Same as new group found
						comparison = -1;
					} else {
						String serverAlias = serverGroups[serverIndex].alias;
						String cachedAlias = c.getString(1);
						comparison = serverAlias.compareTo(cachedAlias);
					}
					
					if (comparison < 0) {
						// Found new group in server, must create it
						createGroup(
								serverGroups[serverIndex].alias, 
								serverGroups[serverIndex].name,
								serverGroups[serverIndex].lat,
								serverGroups[serverIndex].lng
								);
						serverIndex++;
					} else if (comparison > 0) {
						// Cached group not found, must delete it
						deleteGroup(c.getInt(0));
						cachedIndex++;
						c.moveToNext();
					} else {
						// Server and cached match, advance both
						serverIndex++;
						cachedIndex++;
						c.moveToNext();
					}
				}
				return serverGroups;
			} finally {
				c.close();
			}
		}
		
		private void syncUsers(Group[] groups) {
			Set<User> serverUsersSet = new TreeSet<User>();
			for(Group group : groups) {
				if (resync) return;
				
				int page = 1;
				while(true) {
					try {
						User[] users = api.getUsers(group.alias, page);
						if (resync) return;
						
						if (users.length == 0) {
							break;
						}
						for(User user : users) {
							serverUsersSet.add(user);
						}
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					page++;
				}
			}
			
			User[] serverUsers = serverUsersSet.toArray(new User[serverUsersSet.size()]);
			if (resync) return;
			
			// Get cached groups sorted by alias
			Cursor c = getContentResolver().query(Users.CONTENT_URI, new String[] { Users._ID, Users.LOGIN }, null, null, Users.LOGIN);
			
			try {
				int serverIndex = 0;
				int cachedIndex = 0;
				c.moveToNext();
				
				while(serverIndex < serverUsers.length || cachedIndex < c.getCount()) {
					if (resync) return;
					
					int comparison;
					if (serverIndex >= serverUsers.length) {
						// Same as cached group not found
						comparison = 1;
					} else if (cachedIndex >= c.getCount()) {
						// Same as new group found
						comparison = -1;
					} else {
						String serverLogin = serverUsers[serverIndex].login;
						String cachedLogin = c.getString(1);
						comparison = serverLogin.compareTo(cachedLogin);
					}
					
					if (comparison < 0) {
						// Found new user in server, must create it
						createUser(serverUsers[serverIndex].login, serverUsers[serverIndex].displayName);
						serverIndex++;
					} else if (comparison > 0) {
						// Cached user not found, must delete it
						deleteUser(c.getInt(0));
						cachedIndex++;
						c.moveToNext();
					} else {
						// Server and cached match, advance both
						serverIndex++;
						cachedIndex++;
						c.moveToNext();
					}
				}
			} finally {
				c.close();
			}
		}
		
		private void syncMessages(Group[] groups) {
			int newMessagesCount = 0;
			
			for(Group group : groups) {
				if (resync) return;
				
				// Get guid of last cached message
				Uri uri = Groups.CONTENT_URI;
				uri = Uri.withAppendedPath(uri, group.alias);
				uri = Uri.withAppendedPath(uri, "messages");
				uri = Uri.withAppendedPath(uri, "last");
				
				Cursor c = getContentResolver().query(uri, new String[] { Messages.GUID }, null, null, null);
				String lastGuid = c.moveToNext() ? c.getString(0) : null;
				
				// Get just one page of messages
				try {
					Message[] messages = api.getMessages(group.alias, 1);
					if (resync) return;
					
					for(Message message : messages) {
						if (resync) return;
						
						if (message.guid != null && message.guid.equals(lastGuid)) {
							break;
						}
						
						ContentValues values = new ContentValues();
						values.put(Messages.GUID, message.guid);
						values.put(Messages.FROM_USER, message.fromUser);
						values.put(Messages.TO_GROUP, message.toGroup);
						values.put(Messages.MESSAGE, message.message);
						values.put(Messages.LAT, message.lat);
						values.put(Messages.LNG, message.lng);
						values.put(Messages.LOCATION_NAME, locationResolver.getLocationName(message.lat, message.lng));
						values.put(Messages.CREATED_DATE, message.createdDate);
						
						getContentResolver().insert(Messages.CONTENT_URI, values);
						newMessagesCount++;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				} finally {
					c.close();
				}
			}
			
			if (newMessagesCount > 0) {
				notifyNewMessages(newMessagesCount);
			}
		}

		private void createGroup(String alias, String name, double lat, double lng) {
			ContentValues values = new ContentValues();
			values.put(Groups.ALIAS, alias);
			values.put(Groups.NAME, name);
			values.put(Groups.LAT, lat);
			values.put(Groups.LNG, lng);
			values.put(Groups.LOCATION_NAME, locationResolver.getLocationName(lat, lng));
			getContentResolver().insert(Groups.CONTENT_URI, values);
		}
		
		private void deleteGroup(int id) {
			getContentResolver().delete(Uri.withAppendedPath(Groups.CONTENT_URI, String.valueOf(id)), null, null);
		}
		
		private void createUser(String login, String displayName) {
			ContentValues values = new ContentValues();
			values.put(Users.LOGIN, login);
			values.put(Users.DISPLAY_NAME, displayName);
			getContentResolver().insert(Users.CONTENT_URI, values);
		}
		
		private void deleteUser(int id) {
			getContentResolver().delete(Uri.withAppendedPath(Users.CONTENT_URI, String.valueOf(id)), null, null);
		}
	}	

}
