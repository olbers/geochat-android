package org.instedd.geochat.api;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UserHandler extends DefaultHandler {
	
	private final static User[] NO_USERS = {};
	
	private final static int NONE = 0;
	private final static int TITLE = 1;
	private final static int LOGIN = 2;
	
	private boolean inItem;
	private int tagName;
	private List<User> users;
	private User user;
	
	public User[] getUsers() {
		return users == null ? NO_USERS : users.toArray(new User[users.size()]);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if ("item".equals(localName)) {
			if (users == null) {
				users = new ArrayList<User>(10);
			}
			user = new User();
			inItem = true;
			tagName = NONE;
			return;
		}
		
		if (!inItem)
			return;
		
		if ("title".equals(localName)) {
			tagName = TITLE;
		} else if ("Login".equals(localName)) {
			tagName = LOGIN;
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
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("item".equals(localName)) {
			users.add(user);
			inItem = false;
		}
		tagName = NONE;
	}

}
