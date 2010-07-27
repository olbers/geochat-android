package org.instedd.geochat;

import org.instedd.geochat.GeoChat.Groups;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class GroupsActivity extends ListActivity {
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Groups._ID,
            Groups.NAME,
            Groups.ALIAS,
            Groups.LOCATION_NAME,
    };
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// If no data was given in the intent, then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Groups.CONTENT_URI);
        }
		
		// Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
                Groups.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.group_item, cursor,
                new String[] { Groups.NAME, Groups.ALIAS, Groups.LOCATION_NAME }, new int[] { R.id.name, R.id.alias, R.id.location });
        
        setListAdapter(adapter);
	}
}
