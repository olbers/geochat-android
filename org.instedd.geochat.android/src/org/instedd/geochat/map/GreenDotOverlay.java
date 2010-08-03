package org.instedd.geochat.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class GreenDotOverlay extends Overlay {
	
	private final static Paint greenPaint;
	private final static Paint darkGreenPaint;
	private final GeoPoint geoPoint;
	
	public GreenDotOverlay(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		
		Point point = projection.toPixels(geoPoint, null);
		drawCirlce(canvas, point);
	}
	
	private static void drawCirlce(Canvas canvas, Point point) {
		canvas.drawCircle(point.x, point.y, 3, darkGreenPaint);
		canvas.drawCircle(point.x, point.y, 2, greenPaint);
	}
	
	static {
		greenPaint = new Paint();
		greenPaint.setARGB(255, 0, 255, 0);
		greenPaint.setStyle(Style.FILL);
		
		darkGreenPaint= new Paint();
		darkGreenPaint.setARGB(255, 0, 64, 0);
	}

}
