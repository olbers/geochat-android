package org.instedd.geochat;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.Menu;

public class Menues {
	
	public final static int HOME = 1;
	public final static int COMPOSE = 2;
	public final static int MAP = 3;
	public final static int REPORT_MY_LOCATION = 4;
	
	public static void executeAction(Context context, Handler handler, int menuItemId) {
		executeAction(context, handler, menuItemId, null);
	}
	
	public static void executeAction(Context context, Handler handler, int menuItemId, Uri data) {
		switch(menuItemId) {
		case Menues.HOME:
			Actions.home(context);
			break;
		case Menues.COMPOSE:
			Actions.compose(context, data);
			break;
		case Menues.MAP:
			Actions.map(context);
			break;
		case Menues.REPORT_MY_LOCATION:
			Actions.reportMyLocation(context, handler);
			break;
		}
	}
	
	public static void home(Menu menu) {
		menu.add(0, HOME, 0, R.string.home).setIcon(R.drawable.ic_menu_home);
	}
	
	public static void compose(Menu menu) {
		menu.add(0, COMPOSE, 0, R.string.compose).setIcon(R.drawable.ic_menu_compose);
	}
	
	public static void map(Menu menu) {
		menu.add(0, MAP, 0, R.string.map).setIcon(R.drawable.ic_menu_mapmode);
	}
	
	public static void reportMyLocation(Menu menu) {
		menu.add(0, REPORT_MY_LOCATION, 0, R.string.report_my_location).setIcon(R.drawable.ic_menu_mylocation);
	}

}
