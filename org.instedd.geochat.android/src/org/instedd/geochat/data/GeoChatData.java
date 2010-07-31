package org.instedd.geochat.data;

import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.Message;
import org.instedd.geochat.api.User;
import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Messages;
import org.instedd.geochat.data.GeoChat.Users;
import org.instedd.geochat.map.LocationResolver;

import android.content.ContentValues;
import android.content.Context;
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
		context.getContentResolver().update(Uri.withAppendedPath(Groups.CONTENT_URI, String.valueOf(id)), values, null, null);
	}
	
	public void deleteGroup(int id) {
		context.getContentResolver().delete(Uri.withAppendedPath(Groups.CONTENT_URI, String.valueOf(id)), null, null);
	}
	
	public void createUser(User user) {
		context.getContentResolver().insert(Users.CONTENT_URI, getContentValues(user));
	}
	
	public void updateUser(int id, User user) {
		ContentValues values = getContentValues(user);
		values.put(Users._ID, id);
		context.getContentResolver().update(Uri.withAppendedPath(Users.CONTENT_URI, String.valueOf(id)), values, null, null);
	}
	
	public void deleteUser(int id) {
		context.getContentResolver().delete(Uri.withAppendedPath(Users.CONTENT_URI, String.valueOf(id)), null, null);
	}
	
	public void createMessage(Message message) {
		context.getContentResolver().insert(Messages.CONTENT_URI, getContentValues(message));
	}
	
	public void deleteOldMessages(String groupAlias) {
		//context.getContentResolver().delete(Uri.withAppendedPath(Messages.CONTENT_URI, "old"), null, null);
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
		values.put(Users.DISPLAY_NAME, user.displayName);
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

}
