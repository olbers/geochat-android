package org.instedd.geochat.map;

import java.text.DecimalFormat;

public class LatLng {
	
	private final static DecimalFormat format = new DecimalFormat("#.######");
	
	public String lat;
	public String lng;
	
	public LatLng(double lat, double lng) {
		this.lat = format.format(lat);
		this.lng = format.format(lng);
	}

}
