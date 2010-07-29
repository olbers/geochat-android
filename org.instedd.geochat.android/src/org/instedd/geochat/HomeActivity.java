package org.instedd.geochat;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	
	private final static int MENU_COMPOSE = 1;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    startService(new Intent().setClass(this, GeoChatService.class));
	    
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, GroupsActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("groups").setIndicator("Groups",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, PeopleActivity.class);
	    spec = tabHost.newTabSpec("people").setIndicator("People",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, MessagesActivity.class);
	    spec = tabHost.newTabSpec("messages").setIndicator("Messages",
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
		menu.add(0, MENU_COMPOSE, 0, R.string.compose)
			.setIcon(R.drawable.ic_menu_compose);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_COMPOSE:
			startActivity(new Intent().setClass(this, ComposeActivity.class));
			break;
		}
		return true;
	}
}