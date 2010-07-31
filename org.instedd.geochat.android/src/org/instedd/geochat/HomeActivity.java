package org.instedd.geochat;

import org.instedd.geochat.sync.GeoChatService;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    startService(new Intent().setClass(this, GeoChatService.class));
	    
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(this, GroupsActivity.class);
	    spec = tabHost.newTabSpec("groups").setIndicator(res.getString(R.string.groups),
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, PeopleActivity.class);
	    spec = tabHost.newTabSpec("people").setIndicator(res.getString(R.string.people),
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, MessagesActivity.class);
	    spec = tabHost.newTabSpec("messages").setIndicator(res.getString(R.string.messages),
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = getIntent();
	    if (intent != null) {
	    	String action = intent.getAction();
	    	if (Actions.VIEW_MESSAGES.equals(action)) {
	    		tabHost.setCurrentTab(2);
	    		return;
	    	}
	    }

	    tabHost.setCurrentTab(2);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.compose(menu);
		Menues.map(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, item.getItemId());
		return true;
	}
}