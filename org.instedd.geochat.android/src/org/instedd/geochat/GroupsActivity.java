package org.instedd.geochat;

import org.instedd.geochat.GeoChat.Groups;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class GroupsActivity extends ListActivity {
	
    private static final String[] PROJECTION = new String[] {
            Groups._ID,
            Groups.NAME,
            Groups.ALIAS,
            Groups.LOCATION_NAME,
    };
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Groups.CONTENT_URI);
        }
		
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
                Groups.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new GroupCursorAdapter(this, R.layout.group_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
	}
	
	private static class GroupCursorAdapter extends SimpleCursorAdapter {

		private Cursor c;
		private Context context;

		public GroupCursorAdapter(Context context, int layout, Cursor c,
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
				v = inflater.inflate(R.layout.group_item, null);
			}
			this.c.moveToPosition(pos);
			
			String location = c.getString(c.getColumnIndex(Groups.LOCATION_NAME));
			TextView uiLocation = (TextView) v.findViewById(R.id.location);
			
			if (TextUtils.isEmpty(location)) {
				uiLocation.setVisibility(View.GONE);
				uiLocation.setText(location);
			} else {
				uiLocation.setVisibility(View.VISIBLE);
				uiLocation.setText(location);
			}
			
			((TextView) v.findViewById(R.id.name)).setText(c.getString(c.getColumnIndex(Groups.NAME)));
			((TextView) v.findViewById(R.id.alias)).setText(c.getString(c.getColumnIndex(Groups.ALIAS)));
			
			return v;
		}

	}
}
