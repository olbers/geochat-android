package org.instedd.geochat;

import org.instedd.geochat.GeoChat.Messages;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MessagesActivity extends ListActivity {
	
    private static final String[] PROJECTION = new String[] {
            Messages._ID,
            Messages.MESSAGE,
            Messages.FROM_USER,
            Messages.TO_GROUP,
            Messages.LOCATION_NAME,
            Messages.CREATED_DATE,
    };
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Clear new messages count, since we are viewing them
        Notifier.clearNewMessagesCount(this);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Messages.CONTENT_URI);
        }
		
		Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
        		Messages.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new MessageCursorAdapter(this, R.layout.message_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
    }
	
	private static class MessageCursorAdapter extends SimpleCursorAdapter {

		private Cursor c;
		private Context context;

		public MessageCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			this.c = c;
			this.context = context;
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
			
			((TextView) v.findViewById(R.id.message)).setText(c.getString(c.getColumnIndex(Messages.MESSAGE)));
			((TextView) v.findViewById(R.id.from)).setText(c.getString(c.getColumnIndex(Messages.FROM_USER)));
			((TextView) v.findViewById(R.id.group)).setText(c.getString(c.getColumnIndex(Messages.TO_GROUP)));
			
			CharSequence date = DateUtils.getRelativeDateTimeString(context, c.getLong(c.getColumnIndex(Messages.CREATED_DATE)), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);;
			((TextView) v.findViewById(R.id.date)).setText(date);
			
			return v;
		}

	}
}
