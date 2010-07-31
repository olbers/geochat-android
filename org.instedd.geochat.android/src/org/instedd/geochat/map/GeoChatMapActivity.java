package org.instedd.geochat.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.instedd.geochat.Menues;
import org.instedd.geochat.R;
import org.instedd.geochat.api.User;
import org.instedd.geochat.data.GeoChat.Users;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class GeoChatMapActivity extends MapActivity {
	
	private MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    new Thread() {
	    	public void run() {
	    		loadUsers();
	    	};
	    }.start();
	}
	
	private void loadUsers() {
		Map<String, Map<String, List<User>>> mapByLocation = new HashMap<String, Map<String, List<User>>>();  
        String[] PROJECTION = new String[] {
                Users._ID,
                Users.DISPLAY_NAME,
                Users.LAT,
                Users.LNG,
        };
        
        Cursor c = getContentResolver().query(Users.CONTENT_URI, PROJECTION, null, null, "lower(" + Users.DISPLAY_NAME + ")");
        while(c.moveToNext()) {
        	User user = new User();
        	user.displayName = c.getString(c.getColumnIndex(Users.DISPLAY_NAME));
        	user.lat = c.getDouble(c.getColumnIndex(Users.LAT));
        	user.lng = c.getDouble(c.getColumnIndex(Users.LNG));
        	if (user.lat == 0 && user.lng == 0)
        		continue;
        	
        	String groupingKey = (int)(user.lat * 10) + "," + (int)(user.lng * 10);
        	Map<String, List<User>> groupingMap = mapByLocation.get(groupingKey);
        	if (groupingMap == null) {
        		groupingMap = new HashMap<String, List<User>>();
        		mapByLocation.put(groupingKey, groupingMap);
        	}
        	
        	String key = user.lat + "," + user.lng;
        	List<User> users = groupingMap.get(key);
        	if (users == null) {
        		users = new ArrayList<User>();
        		groupingMap.put(key, users);
        	}
        	users.add(user);
        }
        c.close();
        
        for(Map.Entry<String, Map<String, List<User>>> entry : mapByLocation.entrySet()) {
        	mapView.getOverlays().add(new UserGroupsOverlay(entry.getValue()));	
        }
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.home(menu);
		Menues.compose(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, item.getItemId());
		return true;
	}

}
