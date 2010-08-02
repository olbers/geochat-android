package org.instedd.geochat;

import java.util.ArrayList;
import java.util.List;

import org.instedd.geochat.data.GeoChatData;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class PeopleActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener {
	
	private Cursor cursor; 
	private int position;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Users.CONTENT_URI);
		}

        String[] PROJECTION = new String[] {
                Users._ID,
                Users.LOGIN,
                Users.DISPLAY_NAME,
                Users.LAT,
                Users.LNG,
                Users.LOCATION_NAME,
        };
        this.cursor = managedQuery(intent.getData(), PROJECTION, null, null,
        		Users.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new PeopleCursorAdapter(this, R.layout.user_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
        
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		viewMessages();
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
		this.position = position;
		showDialog(0);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final List<String> items = new ArrayList<String>(2);
		items.add(getResources().getString(R.string.view_messages));
		
		cursor.moveToPosition(position);
		String displayName = cursor.getString(cursor.getColumnIndex(Users.DISPLAY_NAME));
		double lat = cursor.getDouble(cursor.getColumnIndex(Users.LAT));
		double lng = cursor.getDouble(cursor.getColumnIndex(Users.LNG));
		if (lat != 0 || lng != 0) {
			items.add(getResources().getString(R.string.show_in_map));
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(displayName);
		builder.setItems(items.toArray(new String[items.size()]), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					viewMessages();
					break;
				case 1:
					showInMap();
					break;
				}
			}
		});
		return builder.create();
	}
	
	private void viewMessages() {
		cursor.moveToPosition(position);
		String login = cursor.getString(cursor.getColumnIndex(Users.LOGIN));
		Actions.viewUserMessages(this, login);
	}
	
	private void showInMap() {
		cursor.moveToPosition(position);
		String login = cursor.getString(cursor.getColumnIndex(Users.LOGIN));
		Actions.showUserInMap(this, login);
	}
	
	private class PeopleCursorAdapter extends SimpleCursorAdapter {

		private final Cursor c;
		private final Context context;
		private final GeoChatData data;
		
		public PeopleCursorAdapter(Context context, int layout, Cursor c,
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
			
			String login = c.getString(c.getColumnIndex(Users.LOGIN));
			
			((TextView) v.findViewById(R.id.display_name)).setText(c.getString(c.getColumnIndex(Users.DISPLAY_NAME)));
			((TextView) v.findViewById(R.id.login)).setText(login);
			
			if (data.hasUserIconFor(login)) {
				ImageView view = (ImageView) v.findViewById(R.id.icon);
				view.setImageURI(data.getUserIconUri(login));
			}
			
			return v;
		}

	}
}
