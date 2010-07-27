package org.instedd.geochat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.instedd.geochat.GeoChat.Locations;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;

public class LocationResolver {
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Locations._ID,
            Locations.LAT,
            Locations.LNG,
            Locations.NAME,
    };

    // lat,lng -> name
    private Map<String, String> locationsMap;
    private ContentResolver contentResolver;
	private Geocoder geocoder;
	
	public LocationResolver(Context context) {
		this.locationsMap = new HashMap<String, String>();
		this.geocoder = new Geocoder(context);
		this.contentResolver = context.getContentResolver();
		
		Cursor c = contentResolver.query(Locations.CONTENT_URI, PROJECTION, null, null, null);
		try {
			while(c.moveToNext()) {
				double lat = c.getDouble(c.getColumnIndex(Locations.LAT));
				double lng = c.getDouble(c.getColumnIndex(Locations.LNG));
				String name = c.getString(c.getColumnIndex(Locations.NAME));
				locationsMap.put(lat + "," + lng, name);
			}
		} finally {
			c.close();
		}
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
					
					ContentValues values = new ContentValues();
					values.put(Locations.LAT, lat);
					values.put(Locations.LNG, lng);
					values.put(Locations.NAME, name);
					contentResolver.insert(Locations.CONTENT_URI, values);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return name;
	}

}
