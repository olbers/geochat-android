package org.instedd.geochat;

import org.instedd.geochat.api.Message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

public class Notifier {
	
	private final Context context;
	
	public final static int NEW_MESSAGES = 1;
	public final static int WRONG_CREDENTIALS = 2;
	public final static int SERVICE = 3;

	public Notifier(Context context) {
		this.context = context;
	}
	
	public void notifyWrongCredentials() {
		NotificationManager man = getNotificationManager();
		Notification notification = new Notification(R.drawable.stat_sys_warning, null, System.currentTimeMillis());
		setDefaults(notification);
		
		Resources r = context.getResources();
		String title = r.getString(R.string.disconnected_from_geochat);
		String content = r.getString(R.string.invalid_credentials);
		
		Intent intent = new Intent().setClass(context, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(LoginActivity.EXTRA_WRONG_CREDENTIALS, true);
		notification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, intent, 0));
		man.notify(WRONG_CREDENTIALS, notification);
	}
	
	public void notifyNewMessages(int count, Message lastMessage) {
		GeoChatSettings settings = new GeoChatSettings(context);
		count += settings.getNewMessagesCount();
		settings.setNewMessagesCount(count);
		
		NotificationManager man = getNotificationManager();
		Notification notification = new Notification(R.drawable.ic_stat_geochat, null, System.currentTimeMillis());
		setDefaults(notification);
		
		Resources r = context.getResources();
		String title;
		String content;
		if (count == 1) {
			title = r.getString(R.string.from_user_to_group, lastMessage.fromUser, lastMessage.toGroup);
			content = lastMessage.message;			
		} else {
			title = r.getString(R.string.new_geochat_messages);
			content = r.getString(R.string.d_new_messages, count);
		}
		
		notification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, getViewMessagesIntent(), 0));
		man.notify(NEW_MESSAGES, notification);
	}
	
	public void startSynchronizingGroups() {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat_sync, R.string.synchronizing_groups);
	}
	
	public void startSynchronizingUsers(String group) {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat_sync, R.string.synchronizing_group_users, group);
	}
	
	public void startSynchronizingMessages(String group) {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat_sync, R.string.synchronizing_group_messages, group);
	}
	
	public void stopSynchronizing() {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat, R.string.geochat_is_running);
	}
	
	private void setForegroundNotificationContent(int icon, int resource) {
		setForegroundNotificationContent(icon, resource, null);
	}
	
	private void setForegroundNotificationContent(int icon, int resource, String argument) {
		NotificationManager man = getNotificationManager();
		
		Resources r = context.getResources();
		String title = r.getString(R.string.app_name);
		String content = argument == null ? r.getString(resource) : r.getString(resource, argument);
		
		Notification notification = new Notification(icon, null, System.currentTimeMillis());
		notification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, getViewMessagesIntent(), 0));
		man.notify(SERVICE, notification);
	}
	
	private Intent getViewMessagesIntent() {
		return getViewMessagesIntent(context);
	}
	
	public static Intent getViewMessagesIntent(Context context) {
		Intent intent = new Intent().setClass(context, HomeActivity.class).setAction(Actions.VIEW_MESSAGES);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
	
	private NotificationManager getNotificationManager() {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private void setDefaults(Notification notification) {
		notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
	}

}
