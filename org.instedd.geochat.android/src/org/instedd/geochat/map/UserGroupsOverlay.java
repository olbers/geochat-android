package org.instedd.geochat.map;

import java.util.List;
import java.util.Map;

import org.instedd.geochat.api.User;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class UserGroupsOverlay extends Overlay {	
	
	private final Map<String, List<User>> userGroups;
	private final static Paint redPaint;
	private final static Paint darkRedPaint;
	private final static Paint strokePaint;
	private final static Paint textPaint;

	public UserGroupsOverlay(Map<String, List<User>> userGroups) {
		this.userGroups = userGroups;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		
		if (userGroups.size() == 1) {
			List<User> users = userGroups.values().iterator().next();
			Point point = getPoint(users.get(0), projection);
			drawCirlce(canvas, point);
			int usersLength = users.size();
			for (int i = 0; i < usersLength; i++) {
				User user = users.get(i);
				String text = user.displayName;
				canvas.drawText(text, point.x, point.y + 4 - (i + 1) * 12, textPaint);	
			}	
		} else {
			int direction = 0;
			for(List<User> users : userGroups.values()) {
				Point point = getPoint(users.get(0), projection);
				float targetX;
				float targetY;
				int upOrDown;
				switch(direction) {
				case 0:
					targetX = point.x + 20;
					targetY = point.y - 60;
					upOrDown = 1;
					break;
				case 1:
					targetX = point.x - 60;
					targetY = point.y - 20;
					upOrDown = 1;
					break;
				case 2:
					targetX = point.x - 20;
					targetY = point.y + 60;
					upOrDown = -1;
					break;
				case 3:
					targetX = point.x + 60;
					targetY = point.y + 20;
					upOrDown = -1;
					break;
				default:
					targetX = point.x;
					targetY = point.y - 100;
					upOrDown = 1;
				}
				
				drawCirlce(canvas, point);
				canvas.drawLine(point.x, point.y, targetX, targetY, darkRedPaint);
				
				int start, end, increment;
				if (upOrDown == 1) {
					start = users.size() - 1;
					end = -1;
					increment = -1;
				} else {
					start = 0;
					end = users.size();
					increment = 1;
				}
				
				for (int i = start, j = 0; i != end; i += increment, j++) {
					User user = users.get(i);
					String text = user.displayName;
					canvas.drawText(text, targetX, targetY + upOrDown * (4 - (j + 1) * 14), textPaint);	
				}
				
				direction++;
			}
		}
		
	}
	
	private static Point getPoint(User user, Projection projection) {
		return projection.toPixels(new GeoPoint((int)(user.lat * 1E6), (int)(user.lng * 1E6)), null);
	}
	
	private static void drawCirlce(Canvas canvas, Point point) {
		canvas.drawCircle(point.x, point.y, 3, darkRedPaint);
		canvas.drawCircle(point.x, point.y, 2, redPaint);
	}
	
	static {
		redPaint = new Paint();
		redPaint.setARGB(255, 255, 0, 0);
		redPaint.setStyle(Style.FILL);
		
		darkRedPaint= new Paint();
		darkRedPaint.setARGB(255, 64, 0, 0);
		
		strokePaint = new Paint();
		strokePaint.setARGB(255, 255, 255, 255);
	    strokePaint.setTextAlign(Paint.Align.CENTER);
	    strokePaint.setTextSize(14);
	    strokePaint.setTypeface(Typeface.DEFAULT);
	    strokePaint.setStyle(Paint.Style.STROKE);
	    strokePaint.setStrokeWidth(2);
	    
	    textPaint = new Paint();
	    textPaint.setARGB(255, 255, 0, 0);	    
	    textPaint.setTextAlign(Paint.Align.CENTER);
	    textPaint.setTextSize(13);
	    textPaint.setTypeface(Typeface.DEFAULT_BOLD);
	    textPaint.setStrokeWidth(1);
	    textPaint.setShadowLayer(2, 0, 0, Color.WHITE);
	}

}
