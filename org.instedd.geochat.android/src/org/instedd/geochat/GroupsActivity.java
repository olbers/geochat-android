package org.instedd.geochat;

import org.instedd.geochat.data.GeoChat.Groups;

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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class GroupsActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener {
	
	final Handler handler = new Handler();
	
	Cursor cursor;
	int position;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Groups.CONTENT_URI);
		}
		
        String[] PROJECTION = new String[] {
                Groups._ID,
                Groups.NAME,
                Groups.ALIAS,
                Groups.LOCATION_NAME,
        };
        this.cursor = managedQuery(intent.getData(), PROJECTION, null, null,
                Groups.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new GroupCursorAdapter(this, R.layout.group_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
        
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
	}
	
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		cursor.moveToPosition(position);
		int groupId = cursor.getInt(cursor.getColumnIndex(Groups._ID));
		Actions.openGroup(this, groupId);
	}
	
	public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
		this.position = position;
		showDialog(0);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final CharSequence[] items = { 
				getResources().getString(R.string.view_group),
				getResources().getString(R.string.compose),
				getResources().getString(R.string.refresh),
				};
		
		cursor.moveToPosition(position);
		String name = cursor.getString(cursor.getColumnIndex(Groups.NAME));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(name);
		builder.setItems(items, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int groupId = cursor.getInt(cursor.getColumnIndex(Groups._ID));
				String groupAlias = cursor.getString(cursor.getColumnIndex(Groups.ALIAS));
				switch(which) {
				case 0:
					Actions.openGroup(GroupsActivity.this, groupId);
					break;
				case 1:
					Actions.compose(GroupsActivity.this, groupId);
					break;
				case 2:
					Actions.refresh(GroupsActivity.this, Uris.groupAlias(groupAlias), handler);
					break;
				}
			}
		});
		return builder.create();
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		cursor.moveToPosition(position);
		String name = cursor.getString(cursor.getColumnIndex(Groups.NAME));
		dialog.setTitle(name);
	}
	
	private static class GroupCursorAdapter extends SimpleCursorAdapter {

		private final Cursor c;
		private final Context context;

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
