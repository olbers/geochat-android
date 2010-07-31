package org.instedd.geochat;

import java.util.Set;
import java.util.TreeSet;

import org.instedd.geochat.GeoChat.Groups;
import org.instedd.geochat.GeoChat.Locations;
import org.instedd.geochat.GeoChat.Messages;
import org.instedd.geochat.GeoChat.Users;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.Message;
import org.instedd.geochat.api.User;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class Synchronizer {
	
	final Context context;
	IGeoChatApi api;
	final GeoChatData data;
	final LocationResolver locationResolver;
	final Notifier notifier;
	boolean running;
	boolean resync;
	boolean connectivityChanged;
	SyncThread syncThread;

	public Synchronizer(Context context) {
		this.context = context;
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
	
	public Group[] syncGroups() {
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
		Cursor c = context.getContentResolver().query(Groups.CONTENT_URI, new String[] { Groups._ID, Groups.ALIAS }, null, null, Groups.ALIAS);
		
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
					// Server and cached match, update and advance both
					data.updateGroup(c.getInt(0), serverGroups[serverIndex]);
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
	
	public void syncUsers(Group[] groups) {
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
		Cursor c = context.getContentResolver().query(Users.CONTENT_URI, new String[] { Users._ID, Users.LOGIN }, null, null, Users.LOGIN);
		
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
					data.createUser(serverUsers[serverIndex]);
					serverIndex++;
				} else if (comparison > 0) {
					// Cached user not found, must delete it
					data.deleteUser(c.getInt(0));
					cachedIndex++;
					c.moveToNext();
				} else {
					// Server and cached match, update and advance both
					data.updateUser(c.getInt(0), serverUsers[serverIndex]);
					serverIndex++;
					cachedIndex++;
					c.moveToNext();
				}
			}
		} finally {
			c.close();
		}
	}
	
	public int syncMessages(Group[] groups) {
		int newMessagesCount = 0;
		
		for(Group group : groups) {
			if (resync) return 0;
			
			// Get guid of last cached message
			Uri uri = Groups.CONTENT_URI;
			uri = Uri.withAppendedPath(uri, group.alias);
			uri = Uri.withAppendedPath(uri, "messages");
			uri = Uri.withAppendedPath(uri, "last");
			
			Cursor c = context.getContentResolver().query(uri, new String[] { Messages.GUID }, null, null, null);
			String lastGuid = c.moveToNext() ? c.getString(0) : null;
			
			// Get just one page of messages
			try {
				Message[] messages = api.getMessages(group.alias, 1);
				if (resync) return 0;
				
				for(Message message : messages) {
					if (resync) return 0;
					
					if (message.guid != null && message.guid.equals(lastGuid)) {
						break;
					}
					
					data.createMessage(message);
					newMessagesCount++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} finally {
				c.close();
			}
		}
		
		return newMessagesCount;
	}
	
	public void clearExistingData() {
		context.getContentResolver().delete(Groups.CONTENT_URI, null, null);
		context.getContentResolver().delete(Users.CONTENT_URI, null, null);
		context.getContentResolver().delete(Messages.CONTENT_URI, null, null);
		context.getContentResolver().delete(Locations.CONTENT_URI, null, null);
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
			while(running) {
				boolean hasConnectivity = hasConnectivity();
				boolean credentialsAreValid = true;
				if (hasConnectivity) {
					try {
						credentialsAreValid = api.credentialsAreValid();
					} catch (Exception e) {
						e.printStackTrace();
					}
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
				
				try {
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
					}
				} catch (Exception e) {
					e.printStackTrace();
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
		
	}

}
