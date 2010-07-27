package org.instedd.geochat;

import org.instedd.geochat.GeoChat.Users;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class PeopleActivity extends ListActivity {
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Users._ID, // 0
            Users.LOGIN, // 1
            Users.DISPLAY_NAME, // 2
    };
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     // If no data was given in the intent, then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Users.CONTENT_URI);
        }
		
		// Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
        		Users.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.user_item, cursor,
                new String[] { Users.DISPLAY_NAME, Users.LOGIN }, new int[] { R.id.display_name, R.id.login });
        
        setListAdapter(adapter);
    }
}
