package org.instedd.geochat.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.instedd.geochat.Connectivity;
import org.instedd.geochat.GeoChatSettings;
import org.instedd.geochat.Notifier;
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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class Synchronizer {
	
	private final static int ICON_SIZE = 50;
	
	final Handler handler;
	
	final Context context;
	IGeoChatApi api;
	String currentUser;
	final GeoChatData data;
	final LocationResolver locationResolver;
	final Notifier notifier;
	boolean running;
	boolean resync;
	boolean resyncOnlyMessages;
	String resyncGroupAlias;
	boolean connectivityChanged;
	ExecutorService genericExecutor;
	SyncThread syncThread;
	Object lock;

	public Synchronizer(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		this.data = new GeoChatData(context);
		this.locationResolver = new LocationResolver(context);
		this.notifier = new Notifier(context);
		this.genericExecutor = Executors.newCachedThreadPool();
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
		this.resyncStart();
	}
	
	public synchronized void resync() {
		if (!connectivityChanged) {
			recreateApi();
		}
		this.resyncOnlyMessages = false;
		this.resyncStart();
	}
	
	public synchronized void resyncMessages() {
		this.resyncOnlyMessages = true;
		this.resyncStart();
	}
	
	public synchronized void resyncMessages(String groupAlias) {
		this.resyncOnlyMessages = true;
		this.resyncGroupAlias = groupAlias;
		this.resyncStart();
	}
	
	public synchronized void connectivityChanged() {
		this.connectivityChanged = true;
	}
	
	private synchronized void resyncStart() {
		if (lock != null) {
			synchronized(lock) {
				lock.notify();
			}
		}
		this.resync = true;
	}
	
	private synchronized void resyncFinished() {
		this.resync = false;
	}
	
	public Group[] syncGroups() throws GeoChatApiException {
		if (running)
			notifier.startSynchronizingGroups();
		
		try {
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
			
			final Group[] serverGroups = serverGroupsList.toArray(new Group[serverGroupsList.size()]);
			
			genericExecutor.execute(new Runnable() {
				public void run() {
					// Get cached groups sorted by alias
					Cursor c = context.getContentResolver().query(Groups.CONTENT_URI, new String[] { Groups._ID, Groups.ALIAS, Groups.NAME, Groups.LAT, Groups.LNG }, null, null, "lower(" + Groups.ALIAS + ")");
					
					try {
						int serverIndex = 0;
						int cachedIndex = 0;
						c.moveToNext();
						
						int serverGroupsLength = serverGroups.length;
						int cachedGroupsLength = c.getCount();
						while(serverIndex < serverGroupsLength || cachedIndex < cachedGroupsLength) {
							if (resync) return;
							
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
					} finally {
						c.close();
					}
				}
			});
			
			return serverGroups;
		} finally {
			if (running)
				notifier.stopSynchronizing();
		}
	}
	
	public void syncUsers(String[] groupAliases, final boolean fetchIcons) throws GeoChatApiException {
		try {
			final Map<User, User> serverUsersMap = Collections.synchronizedMap(new TreeMap<User, User>());
			final GeoChatApiException[] exception = { null };
			
			for(final String group : groupAliases) {
				if (resync)
					return;
				
				if (running)
					notifier.startSynchronizingUsers(group);
				
				int page = 1;
				while(true) {
					if (resync) return;
					if (exception[0] != null) return;
					
					User[] users;
					try {
						users = api.getUsers(group, page);
					} catch (GeoChatApiException e) {
						exception[0] = e;
						return;
					}
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
						existing.groups.add(group);
					}
					
					if (users.length != IGeoChatApi.MAX_PER_PAGE)
						break;
					page++;
				}
			}
			
			final User[] serverUsers = serverUsersMap.values().toArray(new User[serverUsersMap.size()]);
			
			genericExecutor.execute(new Runnable() {
				public void run() {
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
								if (fetchIcons) {
									downloadUserIcon(user.login);
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
								if (!Synchronizer.equals(serverUser.displayName, c.getString(c.getColumnIndex(Users.DISPLAY_NAME))) ||
									serverUser.lat != c.getDouble(c.getColumnIndex(Users.LAT)) ||
									serverUser.lng != c.getDouble(c.getColumnIndex(Users.LNG)) ||
									!serverUser.groups.equals(existingUserGroups)) {
									data.updateUser(c.getInt(0), serverUser);
								}
								if (fetchIcons) {
									downloadUserIcon(serverUser.login);
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
			});
		} finally {
			if (running)
				notifier.stopSynchronizing();
		}
	}
	
	private String[] getGroupAliases(Group[] groups) {
		String[] aliases = new String[groups.length];
		for (int i = 0; i < groups.length; i++) {
			aliases[i] = groups[i].alias;
		}
		return aliases;
	}
	
	public SyncMessagesResult syncMessages(String[] groupsAliases) throws GeoChatApiException {
		try {
			int newMessagesCount = 0;
			Message lastMessage = null;
	
		loop:
			for (final String group : groupsAliases) {
				if (resync)
					break loop;
				
				if (running)
					notifier.startSynchronizingMessages(group);
	
				// Get guid of last cached message
				Uri uri = Uris.groupLastMessage(group);
	
				Cursor c = context.getContentResolver().query(uri,
						new String[] { Messages.GUID }, null, null, null);
				String lastGuid = c.moveToNext() ? c.getString(0) : null;
	
				// Get just one page of messages
				try {
					Message[] messages = api.getMessages(group, 1);
					if (resync)
						break loop;
	
					for (Message message : messages) {
						if (resync)
							break loop;
	
						if (message.guid != null && message.guid.equals(lastGuid)) {
							break;
						}
	
						data.createMessage(message);
						if (!equals(message.fromUser, currentUser)) {
							newMessagesCount++;
						}
						lastMessage = message;
					}
				} finally {
					c.close();
				}
			}
	
			return new SyncMessagesResult(newMessagesCount, lastMessage);
		} finally {
			if (running)
				notifier.stopSynchronizing();
		}
	}
	
	public void deleteOldMessages(String[] groupAliases) {
		for(String group : groupAliases) {
			data.deleteOldMessages(group);
		}
	}
	
	public void clearExistingData() {
		data.deleteGroups();
		data.deleteUsers();
		data.deleteUserIcons();
		data.deleteMessages();
		new GeoChatSettings(context).clearUserData();
	}
	
	private void recreateApi() {
		GeoChatSettings settings = new GeoChatSettings(context);
		api = settings.newApi();
		currentUser = settings.getUser();
	}
	
	private void downloadUserIcon(String login) {
		try {
			HttpResponse response = api.getUserIcon(login, ICON_SIZE);
			if (response == null) {
				data.deleteUserIcon(login);
			} else {
				try {
					InputStream content = response.getEntity().getContent();
					try {
						data.saveUserIcon(login, content);
					} finally {
						content.close();
					}
				} catch (IOException e) {
					
				}
			}
		} catch (GeoChatApiException e) {
			
		}
	}
	
	private static boolean equals(String s1, String s2) {
		if ((s1 == null) != (s2 == null))
			return false;
		
		if (s1 == null)
			return true;
		
		return s1.equals(s2);
	}
	
	private class SyncThread extends Thread {
		
		// Fetch icons the first time
		private boolean fetchIcons = true;
		
		@Override
		public void run() {
			while(running) {
				try {
					notifier.startSynchronizing();
					
					boolean hasConnectivity = Connectivity.hasConnectivity(context);
					boolean credentialsAreValid = true;
					if (hasConnectivity) {
						credentialsAreValid = api.credentialsAreValid();
					}
					
					if (resync) {
						if (!connectivityChanged) {
							if (!credentialsAreValid) {
								if (running)
									notifier.notifyWrongCredentials();
							}
						}
						connectivityChanged = false;
						
						resyncFinished();
					}
					
					if (hasConnectivity && credentialsAreValid) {
						String[] groupAliases;
						
						if (!resyncOnlyMessages) {
							Group[] groups = syncGroups();
							if (resync) continue;
							
							groupAliases = getGroupAliases(groups);
							
							syncUsers(groupAliases, fetchIcons);
							if (resync) continue;
							
							// Don't fetch icons the next times
							fetchIcons = false;
						} else {
							resyncOnlyMessages = false;
							
							if (resyncGroupAlias == null) {
								Cursor c = context.getContentResolver().query(Groups.CONTENT_URI, new String[] { Groups._ID, Groups.ALIAS }, null, null, "lower(" + Groups.ALIAS + ")");
								groupAliases = new String[c.getCount()];
								try {
									for(int i = 0; c.moveToNext(); i++) {
										groupAliases[i] = c.getString(1);
									}
								} finally {
									c.close();
								}
							} else {
								groupAliases = new String[] { resyncGroupAlias };
								resyncGroupAlias = null;
							}
						}
						
						SyncMessagesResult result = syncMessages(groupAliases);
						if (result.count > 0) {
							notifier.notifyNewMessages(result.count, result.lastMessage);
						}
						
						if (resync) continue;
						
						deleteOldMessages(groupAliases);
						if (resync) continue;
					}
				} catch (GeoChatApiException e) {
					// TODO handle this exception
					e.printStackTrace();
				}
				
				// Wait 15 minutes
				lock = new Object();
				synchronized (lock) {
					try {
						lock.wait(1000 * 60 * 15);
					} catch (InterruptedException e) {
					}
				}
				lock = null;
			}
		}
		
	}
	
	class SyncMessagesResult {
		int count;
		Message lastMessage;
		public SyncMessagesResult(int count, Message lastMessage) {
			this.count = count;
			this.lastMessage = lastMessage;
		}
	}

}
