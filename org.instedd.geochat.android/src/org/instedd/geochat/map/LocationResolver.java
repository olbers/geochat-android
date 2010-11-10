package org.instedd.geochat.map;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Locatable;
import org.instedd.geochat.data.GeoChat.Messages;
import org.instedd.geochat.data.GeoChat.Users;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;

public class LocationResolver {
	
    // lat,lng -> name
    private Map<String, String> locationsMap;
	private Geocoder geocoder;
	
	public LocationResolver(Context context) {
		this.locationsMap = new HashMap<String, String>();
		this.geocoder = new Geocoder(context);
		
		loadLocations(Groups.CONTENT_URI, context);
		loadLocations(Users.CONTENT_URI, context);
		loadLocations(Messages.CONTENT_URI, context);
	}

	public String getLocationName(double lat, double lng) {
		String key = lat + "," + lng;
		String name = locationsMap.get(key);
		if (name == null) {
			try {
				List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
				if (!addresses.isEmpty()) {
					Address address = addresses.get(0);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
						if (i != 0) {
							sb.append(", ");
						}
						sb.append(address.getAddressLine(i));
					}
					name = sb.toString();
					locationsMap.put(key, name);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return name;
	}
	
	private void loadLocations(Uri uri, Context context) {
		Cursor c = context.getContentResolver().query(uri, new String[] { Locatable.LAT, Groups.LNG, Groups.LOCATION_NAME }, null, null, null);
		final int latIndex = c.getColumnIndex(Locatable.LAT);
		final int lngIndex = c.getColumnIndex(Locatable.LNG);
		final int locationNameIndex = c.getColumnIndex(Locatable.LOCATION_NAME);
		
		try {
			while(c.moveToNext()) {
				double lat = c.getDouble(latIndex);
				double lng = c.getDouble(lngIndex);
				if (lat == 0 && lng == 0) {
					continue;
				}
				String locationName = c.getString(locationNameIndex);
				locationsMap.put(lat + "," + lng, locationName);
			}
		} finally {
			c.close();
		}
	}

}
