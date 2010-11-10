package org.instedd.geochat.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.instedd.geochat.Uris;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.Message;
import org.instedd.geochat.api.User;
import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Messages;
import org.instedd.geochat.data.GeoChat.Users;
import org.instedd.geochat.map.LocationResolver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class GeoChatData {
	
	private final Context context;
	private final LocationResolver locationResolver;

	public GeoChatData(Context context) {
		this.context = context;
		this.locationResolver = new LocationResolver(context);
	}
	
	public void createGroup(Group group) {
		context.getContentResolver().insert(Groups.CONTENT_URI, getContentValues(group));
	}
	
	public void updateGroup(int id, Group group) {
		ContentValues values = getContentValues(group);
		values.put(Groups._ID, id);
 		context.getContentResolver().update(Uris.groupId(id), values, null, null);
	}
	
	public void deleteGroup(int id) {
		context.getContentResolver().delete(Uris.groupId(id), null, null);
	}
	
	public String getGroupAlias(Uri uri) {
		if (uri == null)
			return null;
		
		int match = GeoChatProvider.URI_MATCHER.match(uri);
		switch(match) {
		case GeoChatProvider.GROUP_ALIAS:
			return uri.getLastPathSegment();
		case GeoChatProvider.GROUP_ID:
			String[] PROJECTION = new String[] {
	                Groups._ID,
	                Groups.ALIAS,
	        };
		    Cursor c = context.getContentResolver().query(uri, PROJECTION, null, null, null);
		    try {
			    if (c.moveToNext())
			    	return c.getString(1);
			    else
			    	return null;
		    } finally {
		    	c.close();
		    }
		default:
			return null;
		}
	}
	
	public void deleteGroups() {
		context.getContentResolver().delete(Groups.CONTENT_URI, null, null);
	}
	
	public void createUser(User user) {
		context.getContentResolver().insert(Users.CONTENT_URI, getContentValues(user));
	}
	
	public void updateUser(int id, User user) {
		ContentValues values = getContentValues(user);
		values.put(Users._ID, id);
		context.getContentResolver().update(Uris.userId(id), values, null, null);
	}
	
	public void saveUserIcon(String login, InputStream icon) {
		try {
			FileOutputStream out = context.openFileOutput(getUserIconFilename(login), Context.MODE_WORLD_WRITEABLE);
			BufferedOutputStream bout = new BufferedOutputStream(out);
			
			BufferedInputStream bin = new BufferedInputStream(icon);
			byte[] buffer = new byte[4096];
			int length;
			while((length = bin.read(buffer, 0, 4096)) > 0) {
				bout.write(buffer, 0, length);
			}
			
			bout.close();
			out.close();
			bin.close();
			icon.close();
			
			context.getContentResolver().notifyChange(Uris.userLogin(login), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasUserIconFor(String login) {
		return context.getFileStreamPath(getUserIconFilename(login)).exists();
	}
	
	public Uri getUserIconUri(String login) {
		return Uri.parse(context.getFileStreamPath(getUserIconFilename(login)).toString());
	}

	public void deleteUserIcon(String login) {
		if (hasUserIconFor(login)) {
			context.deleteFile(getUserIconFilename(login));
			context.getContentResolver().notifyChange(Uris.userLogin(login), null);
		}
	}
	
	public void deleteUser(int id) {
		context.getContentResolver().delete(Uris.userId(id), null, null);
	}
	
	public void deleteUsers() {
		context.getContentResolver().delete(Users.CONTENT_URI, null, null);
	}
	
	public void deleteUserIcons() {
		for(String file : context.fileList()) {
			context.deleteFile(file);
		}
	}
	
	public void createMessage(Message message) {
		context.getContentResolver().insert(Messages.CONTENT_URI, getContentValues(message));
	}
	
	public void deleteOldMessages(String groupAlias) {
		context.getContentResolver().delete(Uris.groupOldMessages(groupAlias), null, null);
	}
	
	public void deleteMessages() {
		context.getContentResolver().delete(Messages.CONTENT_URI, null, null);
	}
	
	private ContentValues getContentValues(Group group) {
		ContentValues values = new ContentValues();
		values.put(Groups.ALIAS, group.alias);
		values.put(Groups.NAME, group.name);
		values.put(Groups.LAT, group.lat);
		values.put(Groups.LNG, group.lng);
		values.put(Groups.LOCATION_NAME, locationResolver.getLocationName(group.lat, group.lng));
		return values;
	}
	
	private ContentValues getContentValues(User user) {
		ContentValues values = new ContentValues();
		values.put(Users.LOGIN, user.login);
		values.put(Users.DISPLAY_NAME, user.displayName == null ? "" : user.displayName);
		values.put(Users.LAT, user.lat);
		values.put(Users.LNG, user.lng);
		values.put(Users.LOCATION_NAME, locationResolver.getLocationName(user.lat, user.lng));
		values.put(Users.GROUPS, Users.getGroups(user.groups));
		return values;
	}
	
	private ContentValues getContentValues(Message message) {
		ContentValues values = new ContentValues();
		values.put(Messages.GUID, message.guid);
		values.put(Messages.FROM_USER, message.fromUser);
		values.put(Messages.TO_GROUP, message.toGroup);
		values.put(Messages.MESSAGE, message.message);
		values.put(Messages.LAT, message.lat);
		values.put(Messages.LNG, message.lng);
		values.put(Messages.LOCATION_NAME, locationResolver.getLocationName(message.lat, message.lng));
		values.put(Messages.CREATED_DATE, message.createdDate);
		return values;
	}
	
	private static String getUserIconFilename(String login) {
		return "user_" + login + ".png";
	}	

}
