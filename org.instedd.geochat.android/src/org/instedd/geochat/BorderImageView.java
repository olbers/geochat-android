package org.instedd.geochat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BorderImageView extends ImageView {
	
	private static Paint[] paints = new Paint[5];
	static {
		for (int i = 0; i < paints.length; i++) {
			paints[i] = new Paint();
		}
		paints[0].setARGB(60, 255, 255, 255);
		paints[1].setARGB(30, 255, 255, 255);
		paints[2].setARGB(30, 0, 0, 0);
		paints[3].setARGB(60, 0, 0, 0);
		paints[4].setARGB(128, 0, 0, 0);
	}
	
	private boolean showBorders = false;
	
	public BorderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BorderImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void showBorders() {
		showBorders = true;
	}
	
	public void hideBorders() {
		showBorders = false;
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		if (!showBorders) return;
		
		final float scale = getContext().getResources().getDisplayMetrics().density;
		
		canvas.drawRect(0, 0, 48*scale, 1*scale, paints[0]);
		canvas.drawRect(0, 2*scale, 1*scale, 48*scale, paints[1]);
		canvas.drawRect(48*scale, 2*scale, 52*scale, 52*scale, paints[2]);
		canvas.drawRect(0, 48*scale, 52*scale, 52*scale, paints[3]);
		
		canvas.drawRect(0, 0, 1*scale, 1*scale, paints[4]);
		canvas.drawRect(49*scale, 0, 52*scale, 1*scale, paints[4]);
		canvas.drawRect(0, 49*scale, 1*scale, 52*scale, paints[4]);
		canvas.drawRect(49*scale, 49*scale, 52*scale, 52*scale, paints[4]);
	}

}
