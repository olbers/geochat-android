package org.instedd.geochat;

import org.instedd.geochat.sync.GeoChatService;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	
	private final Handler handler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    getApplicationContext().startService(new Intent().setClass(this, GeoChatService.class));
	    
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(this, GroupsActivity.class);
	    spec = tabHost.newTabSpec("groups").setIndicator(res.getString(R.string.groups),
	                      res.getDrawable(R.drawable.ic_tab_groups))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, PeopleActivity.class);
	    spec = tabHost.newTabSpec("people").setIndicator(res.getString(R.string.people),
	                      res.getDrawable(R.drawable.ic_tab_users))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, MessagesActivity.class);
	    spec = tabHost.newTabSpec("messages").setIndicator(res.getString(R.string.messages),
	                      res.getDrawable(R.drawable.ic_tab_messages))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTab(2);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		getApplicationContext().startService(new Intent().setClass(this, GeoChatService.class));
	}
	
	@Override
	protected void onResume() {
		Intent intent = getIntent();
	    if (intent != null) {
	    	String action = intent.getAction();
	    	if (Actions.VIEW_MESSAGES.equals(action)) {
	    		// Clear new messages count, since we are viewing them
		        new GeoChatSettings(this).clearNewMessagesCount();
	    	}
	    }
	    
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.map(menu);
		Menues.compose(menu);
		Menues.refresh(menu);
		Menues.reportMyLocation(menu);
		Menues.settings(menu);
		Menues.logoff(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}
}