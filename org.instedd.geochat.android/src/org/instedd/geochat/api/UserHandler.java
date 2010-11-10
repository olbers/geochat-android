package org.instedd.geochat.api;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UserHandler extends DefaultHandler {
	
	private final static User[] NO_USERS = {};
	
	private final static int NONE = 0;
	private final static int TITLE = 1;
	private final static int LOGIN = 2;
	private final static int LAT = 3;
	private final static int LNG = 4;
	
	private boolean inItem;
	private int tagName;
	private int usersCount = 0;
	private User[] users;
	private User user;
	
	public User[] getUsers() {
		if (users == null)
			return NO_USERS;
		
		if (usersCount == IGeoChatApi.MAX_PER_PAGE)
			return users;
		
		User[] finalUsers = new User[usersCount];
		System.arraycopy(users, 0, finalUsers, 0, usersCount);
		return finalUsers;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (!inItem) {
			if ("item".equals(localName)) {
				if (users == null) {
					users = new User[10];
				}
				user = new User();
				inItem = true;
				tagName = NONE;
			}
			return;
		}
		
		if ("title".equals(localName)) {
			tagName = TITLE;
		} else if ("Login".equals(localName)) {
			tagName = LOGIN;
		} else if ("lat".equals(localName)) {
			tagName = LAT;
		} else if ("long".equals(localName)) {
			tagName = LNG;
		} else {
			tagName = NONE;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (!inItem)
			return;
		
		switch(tagName) {
		case TITLE:
			user.displayName = new String(ch, start, length);
			break;
		case LOGIN:
			user.login = new String(ch, start, length);
			break;
		case LAT:
			user.lat = Double.parseDouble(new String(ch, start, length));
			if (!(-90 <= user.lat && user.lat <= 90)) {
				user.lat = 0;
			}
			break;
		case LNG:
			user.lng = Double.parseDouble(new String(ch, start, length));
			if (!(-180 <= user.lng && user.lng <= 180)) {
				user.lng = 0;
			}
			break;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (inItem && "item".equals(localName)) {
			users[usersCount] = user;
			usersCount++;
			inItem = false;
		}
		tagName = NONE;
	}

}
