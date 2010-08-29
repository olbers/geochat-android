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
	
	private final static int NEW_MESSAGES = 1;
	private final static int WRONG_CREDENTIALS = 2;

	public Notifier(Context context) {
		this.context = context;
	}
	
	public void notifyWrongCredentials() {
		NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
		
		NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
		
		Intent intent = new Intent().setClass(context, HomeActivity.class).setAction(Actions.VIEW_MESSAGES);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, intent, 0));
		man.notify(NEW_MESSAGES, notification);
	}
	
	private void setDefaults(Notification notification) {
		notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
	}

}
