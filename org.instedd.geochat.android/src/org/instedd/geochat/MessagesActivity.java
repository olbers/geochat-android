package org.instedd.geochat;

import org.instedd.geochat.GeoChat.Messages;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MessagesActivity extends ListActivity {
	
	/**
     * The columns we are interested in from the database
     */
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

     // If no data was given in the intent, then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Messages.CONTENT_URI);
        }
		
		// Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
        		Messages.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
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
			
			((TextView) v.findViewById(R.id.message)).setText(c.getString(c.getColumnIndex(Messages.MESSAGE)));
			((TextView) v.findViewById(R.id.from)).setText(c.getString(c.getColumnIndex(Messages.FROM_USER)));
			((TextView) v.findViewById(R.id.group)).setText(c.getString(c.getColumnIndex(Messages.TO_GROUP)));
			((TextView) v.findViewById(R.id.location)).setText(c.getString(c.getColumnIndex(Messages.LOCATION_NAME)));
			
			CharSequence date = DateUtils.getRelativeDateTimeString(context, c.getLong(c.getColumnIndex(Messages.CREATED_DATE)), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);;
			((TextView) v.findViewById(R.id.date)).setText(date);
			
			return v;
		}

	}
}
