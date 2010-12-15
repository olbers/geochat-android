package org.instedd.geochat.map;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public final class LocationTracker implements LocationListener {
	
	private static LocationTracker instance;
	
	private LocationManager manager;
	private LatLng latLng;
	
	private LocationTracker(LocationManager manager) {
		this.manager = manager;
		for(String provider : manager.getProviders(true)) {
			manager.requestLocationUpdates(provider, 10 * 60 * 1000, 1000, this);
		}
	}
	
	/**
	 * This method *must not* be called inside a Thread#run method.
	 */
	public static LocationTracker getInstance(Context context) {
		if (instance == null) {
			LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			instance = new LocationTracker(manager);
		}
		return instance;
	}
	
	public LatLng getLocation() {
		if (latLng != null) return latLng;
		
		List<String> providerNames = this.manager.getAllProviders();
		
		double bestTime = 0;
		double lat = 0;
		double lng = 0;
        
		// TODO this just checks the most recent location,
		// without taking into account the provider's accuracy
        for(String providerName : providerNames) {
        	Location location = this.manager.getLastKnownLocation(providerName);
        	if (location != null) {
        		double time = location.getTime();
        		if (time <= bestTime)
        			continue;
        		
        		bestTime = time;
	        	lat = location.getLatitude();
	        	lng = location.getLongitude();
        	}
        }
        
        if (bestTime != 0) {
        	latLng = new LatLng(lat, lng);
        }
        
        return latLng;
	}

	public void onLocationChanged(Location location) {
		latLng = new LatLng(location.getLatitude(), location.getLongitude());
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
