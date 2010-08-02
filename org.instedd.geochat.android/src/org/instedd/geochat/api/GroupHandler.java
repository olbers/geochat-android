package org.instedd.geochat.api;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GroupHandler extends DefaultHandler {
	
	private final static Group[] NO_GROUPS = {};
	
	private final static int NONE = 0;
	private final static int TITLE = 1;
	private final static int ALIAS = 2;
	private final static int LAT = 3;
	private final static int LNG = 4;
	
	private boolean inItem;
	private int tagName;
	private Group[] groups;
	private int groupsCount;
	private Group group;
	
	public Group[] getGroups() {
		if (groups == null)
			return NO_GROUPS;
		
		if (groupsCount == IGeoChatApi.MAX_PER_PAGE)
			return groups;
		
		Group[] finalGroups = new Group[groupsCount];
		System.arraycopy(groups, 0, finalGroups, 0, groupsCount);
		return finalGroups;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if ("item".equals(localName)) {
			if (groups == null) {
				groups = new Group[10];
			}
			group = new Group();
			inItem = true;
			tagName = NONE;
			return;
		}
		
		if (!inItem)
			return;
		
		if ("title".equals(localName)) {
			tagName = TITLE;
		} else if ("Alias".equals(localName)) {
			tagName = ALIAS;
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
			group.name = new String(ch, start, length);
			break;
		case ALIAS:
			group.alias = new String(ch, start, length);
			break;
		case LAT:
			group.lat = Double.parseDouble(new String(ch, start, length));
			if (!(-90 <= group.lat && group.lat <= 90)) {
				group.lat = 0;
			}
			break;
		case LNG:
			group.lng = Double.parseDouble(new String(ch, start, length));
			if (!(-180 <= group.lng && group.lng <= 180)) {
				group.lng = 0;
			}
			break;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("item".equals(localName)) {
			groups[groupsCount] = group;
			groupsCount++;
			inItem = false;
		}
		tagName = NONE;
	}

}
