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
	
	public BorderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BorderImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		canvas.drawRect(0, 0, 48, 1, paints[0]);
		canvas.drawRect(0, 2, 1, 48, paints[1]);
		canvas.drawRect(48, 2, 52, 52, paints[2]);
		canvas.drawRect(0, 48, 52, 52, paints[3]);
		
		canvas.drawRect(0, 0, 1, 1, paints[4]);
		canvas.drawRect(49, 0, 52, 1, paints[4]);
		canvas.drawRect(0, 49, 1, 52, paints[4]);
		canvas.drawRect(49, 49, 52, 52, paints[4]);
	}

}
