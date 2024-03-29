package org.instedd.geochat;

import org.instedd.geochat.data.GeoChatData;
import org.instedd.geochat.data.GeoChatProvider;
import org.instedd.geochat.data.GeoChat.Messages;
import org.instedd.geochat.data.GeoChat.Users;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class MessagesActivity extends ListActivity implements OnItemLongClickListener {
	
	private final Handler handler = new Handler();
	private boolean mainActivity;
	private int position;
	private Cursor cursor;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Messages.CONTENT_URI);
		}

        String[] PROJECTION = new String[] {
                Messages._ID,
                Messages.MESSAGE,
                Messages.FROM_USER,
                Messages.TO_GROUP,
                Messages.LOCATION_NAME,
                Messages.CREATED_DATE,
        };
		this.cursor = managedQuery(intent.getData(), PROJECTION, null, null,
        		Messages.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new MessageCursorAdapter(this, R.layout.message_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
        
        getListView().setOnItemLongClickListener(this);
        
        if (GeoChatProvider.URI_MATCHER.match(intent.getData()) == GeoChatProvider.USER_MESSAGES) {
			String[] UPROJECTION = new String[] {
	                Users._ID,
	                Users.LOGIN,
	                Users.DISPLAY_NAME,
	        };
	        Cursor uCursor = getContentResolver().query(Uris.userLogin(intent.getData().getPathSegments().get(1)), UPROJECTION, null, null,
	        		Users.DEFAULT_SORT_ORDER);
	        if (uCursor.moveToNext()) {
	        	String displayName = uCursor.getString(2);
	        	if (displayName == null) {
	        		displayName = uCursor.getString(1);
	        	}
	        	String title = getResources().getString(R.string.app_name) + " - " + displayName; 
	        	if (cursor.getCount() == 0) {
	        		title += " (" + getResources().getString(R.string.no_messages) + ")";
	        	}
	        	setTitle(title);
	        }
	        uCursor.close();
	        mainActivity = true;
		} else {
			mainActivity = false;
		}
    }
    
    @Override
    protected void onResume() {
    	// Clear new messages count, since we are viewing them
        new GeoChatSettings(this).clearNewMessagesCount();
        
    	super.onResume();
    }
    
    public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
    	this.position = position;
		showDialog(0);
		return true;
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
    	final CharSequence[] items = {
    			getResources().getString(R.string.reply_to_group),
    			getResources().getString(R.string.view_user),
    			getResources().getString(R.string.view_group),
    			getResources().getString(R.string.show_in_map),
    			getResources().getString(R.string.copy_message_text)
    	};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.message));
		builder.setItems(items, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					replyToGroup();
					break;
				case 1:
					openUser();
					break;
				case 2:
					openGroup();
					break;
				case 3:
					showInMap();
					break;
				case 4:
					copyMessageText();
					break;
				}
				
			}			
		});
		return builder.create();
	}
    
    private void replyToGroup() {
    	cursor.moveToPosition(position);
    	String alias = cursor.getString(cursor.getColumnIndex(Messages.TO_GROUP));
    	Actions.compose(this, alias);
    }
    
    private void openUser() {
    	cursor.moveToPosition(position);
		String login = cursor.getString(cursor.getColumnIndex(Messages.FROM_USER));
		Actions.openUser(MessagesActivity.this, login);
    }
    
    private void openGroup() {
    	cursor.moveToPosition(position);
		String alias = cursor.getString(cursor.getColumnIndex(Messages.TO_GROUP));
		Actions.openGroup(MessagesActivity.this, alias);
    }
    
    private void showInMap() {
		cursor.moveToPosition(position);
		int id = cursor.getInt(cursor.getColumnIndex(Messages._ID));
		Actions.showMessageInMap(this, id);
	}
    
    private void copyMessageText() {
    	cursor.moveToPosition(position);
		String message = cursor.getString(cursor.getColumnIndex(Messages.MESSAGE));
		ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clip.setText(message);
	}
	
	private static class MessageCursorAdapter extends SimpleCursorAdapter {

		private final Cursor c;
		private final Context context;
		private final GeoChatData data;

		public MessageCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			this.c = c;
			this.context = context;
			this.data = new GeoChatData(context);
		}
		
		@Override
		public View getView(int pos, View inView, ViewGroup parent) {
			View v = inView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.message_item, null);
			}
			this.c.moveToPosition(pos);
			
			String location = c.getString(c.getColumnIndex(Messages.LOCATION_NAME));
			TextView uiLocation = (TextView) v.findViewById(R.id.location);
			
			if (TextUtils.isEmpty(location)) {
				uiLocation.setVisibility(View.GONE);
				uiLocation.setText(location);
			} else {
				uiLocation.setVisibility(View.VISIBLE);
				uiLocation.setText(location);
			}
			
			String login = c.getString(c.getColumnIndex(Messages.FROM_USER)); 
			
			((TextView) v.findViewById(R.id.message)).setText(c.getString(c.getColumnIndex(Messages.MESSAGE)));
			((TextView) v.findViewById(R.id.from)).setText(login);
			((TextView) v.findViewById(R.id.group)).setText(c.getString(c.getColumnIndex(Messages.TO_GROUP)));
			
			long longDate = c.getLong(c.getColumnIndex(Messages.CREATED_DATE));
			if (longDate > 0) {
				CharSequence date = DateUtils.getRelativeDateTimeString(context, longDate, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);;
				((TextView) v.findViewById(R.id.date)).setText(date);
			} else {
				((TextView) v.findViewById(R.id.date)).setText("");
			}
			
			ImageView view = (ImageView) v.findViewById(R.id.icon);
			if (data.hasUserIconFor(login)) {
				view.setImageURI(data.getUserIconUri(login));
			} else {
				view.setImageResource(R.drawable.default_user_icon);
			}
			
			return v;
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mainActivity) return super.onCreateOptionsMenu(menu);
		Menues.home(menu);
		Menues.map(menu);
		Menues.compose(menu);
		Menues.refresh(menu);
		Menues.reportMyLocation(menu);
		Menues.settings(menu);
		Menues.signout(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!mainActivity) return super.onOptionsItemSelected(item);
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}
}
