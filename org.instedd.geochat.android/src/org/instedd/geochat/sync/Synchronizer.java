package org.instedd.geochat.sync;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.instedd.geochat.GeoChatSettings;
import org.instedd.geochat.Notifier;
import org.instedd.geochat.R;
import org.instedd.geochat.Uris;
import org.instedd.geochat.api.GeoChatApiException;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.Message;
import org.instedd.geochat.api.User;
import org.instedd.geochat.data.GeoChatData;
import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Messages;
import org.instedd.geochat.data.GeoChat.Users;
import org.instedd.geochat.map.LocationResolver;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;

public class Synchronizer {
	
	private final static int ICON_SIZE = 50;
	
	final Handler handler;
	
	final Context context;
	IGeoChatApi api;
	final GeoChatData data;
	final LocationResolver locationResolver;
	final Notifier notifier;
	boolean running;
	boolean resync;
	boolean connectivityChanged;
	SyncThread syncThread;

	public Synchronizer(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		this.data = new GeoChatData(context);
		this.locationResolver = new LocationResolver(context);
		this.notifier = new Notifier(context);
		recreateApi();
	}
	
	public synchronized void start() {
		if (running) return;
		
		running = true;
		
		syncThread = new SyncThread();
		syncThread.start();
	}
	
	public synchronized void stop() {
		running = false;
	}
	
	public synchronized void resync() {
		if (!connectivityChanged) {
			recreateApi();
		}
		this.resync = true;
	}
	
	public synchronized void connectivityChanged() {
		this.connectivityChanged = true;
	}
	
	private synchronized void resyncFinished() {
		this.resync = false;
	}
	
	public Group[] syncGroups() throws GeoChatApiException {
		// Get groups from server and sort them by alias
		Set<Group> serverGroupsList = new TreeSet<Group>();
		int page = 1;
		while(true) {
			if (resync) return null;
			
			Group[] serverGroups = api.getGroups(page);
			if (serverGroups.length == 0) {
				break;
			}
			for(Group serverGroup : serverGroups) {
				serverGroupsList.add(serverGroup);
			}
			if (serverGroups.length != IGeoChatApi.MAX_PER_PAGE)
				break;
			page++;
		}
		if (resync) return null;
		
		Group[] serverGroups = serverGroupsList.toArray(new Group[serverGroupsList.size()]);
		if (resync) return null;
		
		// Get cached groups sorted by alias
		Cursor c = context.getContentResolver().query(Groups.CONTENT_URI, new String[] { Groups._ID, Groups.ALIAS, Groups.NAME, Groups.LAT, Groups.LNG }, null, null, "lower(" + Groups.ALIAS + ")");
		
		try {
			int serverIndex = 0;
			int cachedIndex = 0;
			c.moveToNext();
			
			int serverGroupsLength = serverGroups.length;
			int cachedGroupsLength = c.getCount();
			while(serverIndex < serverGroupsLength || cachedIndex < cachedGroupsLength) {
				if (resync) return null;
				
				int comparison;
				if (serverIndex >= serverGroupsLength) {
					// Same as cached group not found
					comparison = 1;
				} else if (cachedIndex >= cachedGroupsLength) {
					// Same as new group found
					comparison = -1;
				} else {
					String serverAlias = serverGroups[serverIndex].alias;
					String cachedAlias = c.getString(1);
					comparison = serverAlias.compareTo(cachedAlias);
				}
				
				if (comparison < 0) {
					// Found new group in server, must create it
					data.createGroup(serverGroups[serverIndex]);
					serverIndex++;
				} else if (comparison > 0) {
					// Cached group not found, must delete it
					data.deleteGroup(c.getInt(0));
					cachedIndex++;
					c.moveToNext();
				} else {
					// Server and cached match, update (if changed) and advance both
					Group serverGroup = serverGroups[serverIndex];
					if (!serverGroup.name.equals(c.getString(c.getColumnIndex(Groups.NAME))) ||
						serverGroup.lat != c.getDouble(c.getColumnIndex(Groups.LAT)) ||
						serverGroup.lng != c.getDouble(c.getColumnIndex(Groups.LNG))) {
							data.updateGroup(c.getInt(0), serverGroup);
					}
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
	
	public void syncUsers(Group[] groups) throws GeoChatApiException {
		Map<User, User> serverUsersMap = new TreeMap<User, User>();
		for(Group group : groups) {
			if (resync) return;
			
			int page = 1;
			while(true) {
				User[] users = api.getUsers(group.alias, page);
				if (resync) return;
				
				if (users.length == 0) {
					break;
				}
				for(User user : users) {
					User existing = serverUsersMap.get(user);
					if (existing == null) {
						existing = user;
						serverUsersMap.put(user, user);
					}
					if (existing.groups == null) {
						existing.groups = new TreeSet<String>();
					}
					existing.groups.add(group.alias);
				}
				
				if (users.length != IGeoChatApi.MAX_PER_PAGE)
					break;
				page++;
			}
		}
		
		User[] serverUsers = serverUsersMap.values().toArray(new User[serverUsersMap.size()]);
		if (resync) return;
		
		// Get cached groups sorted by alias
		Cursor c = context.getContentResolver().query(Users.CONTENT_URI, new String[] { Users._ID, Users.LOGIN, Users.DISPLAY_NAME, Users.LAT, Users.LNG, Users.GROUPS }, null, null, "lower(" + Users.LOGIN + ")");
		
		try {
			int serverIndex = 0;
			int cachedIndex = 0;
			c.moveToNext();
			
			int serverUsersLength = serverUsers.length; 
			int cachedUsersLength = c.getCount();
			while(serverIndex < serverUsersLength || cachedIndex < cachedUsersLength) {
				if (resync) return;
				
				int comparison;
				if (serverIndex >= serverUsersLength) {
					// Same as cached group not found
					comparison = 1;
				} else if (cachedIndex >= cachedUsersLength) {
					// Same as new group found
					comparison = -1;
				} else {
					String serverLogin = serverUsers[serverIndex].login;
					String cachedLogin = c.getString(1);
					comparison = serverLogin.compareTo(cachedLogin);
				}
				
				if (comparison < 0) {
					// Found new user in server, must create it
					User user = serverUsers[serverIndex];
					data.createUser(user);
					InputStream icon = api.getUserIcon(user.login, ICON_SIZE);
					if (icon != null) {
						data.saveUserIcon(user.login, icon);
					}
					serverIndex++;
				} else if (comparison > 0) {
					// Cached user not found, must delete it
					data.deleteUser(c.getInt(0));
					data.deleteUserIcon(c.getString(1));
					cachedIndex++;
					c.moveToNext();
				} else {
					// Server and cached match, update (if changed) and advance both
					User serverUser = serverUsers[serverIndex];
					TreeSet<String> existingUserGroups = Users.getGroups(c.getString(c.getColumnIndex(Users.GROUPS)));
					if (!serverUser.displayName.equals(c.getString(c.getColumnIndex(Users.DISPLAY_NAME))) ||
						serverUser.lat != c.getDouble(c.getColumnIndex(Users.LAT)) ||
						serverUser.lng != c.getDouble(c.getColumnIndex(Users.LNG)) ||
						!serverUser.groups.equals(existingUserGroups)) {
						data.updateUser(c.getInt(0), serverUser);
					}
					serverIndex++;
					cachedIndex++;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
	}
	
	public int syncMessages(Group[] groups) throws GeoChatApiException {
		int newMessagesCount = 0;
		
		for(Group group : groups) {
			if (resync) return newMessagesCount;
			
			// Get guid of last cached message
			Uri uri = Uris.groupLastMessage(group.alias);
			
			Cursor c = context.getContentResolver().query(uri, new String[] { Messages.GUID }, null, null, null);
			String lastGuid = c.moveToNext() ? c.getString(0) : null;
			
			// Get just one page of messages
			try {
				Message[] messages = api.getMessages(group.alias, 1);
				if (resync) return newMessagesCount;
				
				for(Message message : messages) {
					if (resync) return newMessagesCount;
					
					if (message.guid != null && message.guid.equals(lastGuid)) {
						break;
					}
					
					data.createMessage(message);
					newMessagesCount++;
				}
			} finally {
				c.close();
			}
		}
		
		if (resync) return newMessagesCount;
		
		return newMessagesCount;
	}
	
	public void deleteOldMessages(Group[] groups) {
		for(Group group : groups) {
			data.deleteOldMessages(group.alias);
		}
	}
	
	public void clearExistingData() {
		context.getContentResolver().delete(Groups.CONTENT_URI, null, null);
		context.getContentResolver().delete(Users.CONTENT_URI, null, null);
		context.getContentResolver().delete(Messages.CONTENT_URI, null, null);
		new GeoChatSettings(context).clearUserData();
	}
	
	private void recreateApi() {
		api = new GeoChatSettings(context).newApi();
	}
	
	private boolean hasConnectivity() {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conn.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	
	private class SyncThread extends Thread {
		@Override
		public void run() {
			try {
				while(running) {
					try {
						boolean hasConnectivity = hasConnectivity();
						boolean credentialsAreValid = true;
						if (hasConnectivity) {
							credentialsAreValid = api.credentialsAreValid();
						}
						
						if (resync) {
							if (!connectivityChanged) {
								if (!credentialsAreValid) {
									notifier.notifyWrongCredentials();
								}
							}
							connectivityChanged = false;
							
							resyncFinished();
						}
						
						if (hasConnectivity && credentialsAreValid) {
							Group[] groups = syncGroups();
							if (resync) continue;
							
							syncUsers(groups);
							if (resync) continue;
							
							int newMessagesCount = syncMessages(groups);
							if (newMessagesCount > 0) {
								notifier.notifyNewMessages(newMessagesCount);
							}
							if (resync) continue;
							
							deleteOldMessages(groups);
							if (resync) continue;
						}
					} catch (GeoChatApiException e) {
						// TODO handle this exception
						e.printStackTrace();
					}
						
					// Wait 15 minutes in intervals of 5 seconds,
					// so wait
					try {
						for(int i = 0; i < 3 * 60; i++) {
							sleep(5000);
							if (resync) break;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (final Exception e) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder
							.setTitle(context.getResources().getString(R.string.something_went_wrong))
							.setMessage(e.getMessage())
							.setNegativeButton(android.R.string.ok, null)
							.setCancelable(true)
							.create().show();
					}
				});
			}
		}		
		
	}

}
