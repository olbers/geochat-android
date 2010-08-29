package org.instedd.geochat;

import org.instedd.geochat.data.GeoChat.Groups;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class GroupActivity extends TabActivity {
	
	private final Handler handler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.group);
	    
	    Uri data = getIntent().getData();
	    
	    String[] PROJECTION = new String[] {
                Groups._ID,
                Groups.NAME,
                Groups.ALIAS,
        };
	    Cursor c = getContentResolver().query(data, PROJECTION, null, null, null);
	    if (!c.moveToNext()) {
	    	Actions.home(this);
	    	return;
	    }
	    
	    String alias = c.getString(c.getColumnIndex(Groups.ALIAS));
	    String name = c.getString(c.getColumnIndex(Groups.NAME));
	    c.close();
	    
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;
	    
	    setTitle(res.getString(R.string.app_name) + " - " + name);

	    intent = new Intent().setClass(this, PeopleActivity.class).setData(Uris.groupUsers(alias));
	    spec = tabHost.newTabSpec("people").setIndicator(res.getString(R.string.members),
	                      res.getDrawable(R.drawable.ic_tab_users))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, MessagesActivity.class).setData(Uris.groupMessages(alias));
	    spec = tabHost.newTabSpec("messages").setIndicator(res.getString(R.string.messages),
	                      res.getDrawable(R.drawable.ic_tab_messages))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.home(menu);
		Menues.map(menu);
		Menues.compose(menu);
		Menues.refresh(menu);
		Menues.reportMyLocation(menu);
		Menues.settings(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId(), getIntent().getData());
		return true;
	}

}
