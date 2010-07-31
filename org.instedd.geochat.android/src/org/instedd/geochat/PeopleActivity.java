package org.instedd.geochat;

import org.instedd.geochat.data.GeoChat.Users;

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

public class PeopleActivity extends ListActivity {
	
    private static final String[] PROJECTION = new String[] {
            Users._ID,
            Users.LOGIN,
            Users.DISPLAY_NAME,
            Users.LOCATION_NAME,
    };
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Users.CONTENT_URI);
        }
		
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
        		Users.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new PeopleCursorAdapter(this, R.layout.user_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
    }
	
	private static class PeopleCursorAdapter extends SimpleCursorAdapter {

		private Cursor c;
		private Context context;

		public PeopleCursorAdapter(Context context, int layout, Cursor c,
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
				v = inflater.inflate(R.layout.user_item, null);
			}
			this.c.moveToPosition(pos);
			
			String location = c.getString(c.getColumnIndex(Users.LOCATION_NAME));
			TextView uiLocation = (TextView) v.findViewById(R.id.location);
			
			if (TextUtils.isEmpty(location)) {
				uiLocation.setVisibility(View.GONE);
				uiLocation.setText(location);
			} else {
				uiLocation.setVisibility(View.VISIBLE);
				uiLocation.setText(location);
			}
			
			((TextView) v.findViewById(R.id.display_name)).setText(c.getString(c.getColumnIndex(Users.DISPLAY_NAME)));
			((TextView) v.findViewById(R.id.login)).setText(c.getString(c.getColumnIndex(Users.LOGIN)));
			
			return v;
		}

	}
}
