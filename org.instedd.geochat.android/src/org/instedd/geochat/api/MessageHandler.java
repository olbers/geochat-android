package org.instedd.geochat.api;

import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MessageHandler extends DefaultHandler {
	
	private final static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	
	private Message[] NO_MESSAGES = {};
	
	private final static int NONE = 0;
	private final static int TITLE = 1;
	private final static int GUID = 2;
	private final static int PUB_DATE = 3;
	private final static int SENDER_ALIAS = 4;
	private final static int GROUP_ALIAS = 5;
	private final static int LAT = 6;
	private final static int LNG = 7;
	
	private boolean inItem;
	private int tagName;
	private Message[] messages;
	private int messagesCount;
	private Message message;
	
	public Message[] getMessages() {
		if (messages == null)
			return NO_MESSAGES;
		
		if (messagesCount == IGeoChatApi.MAX_PER_PAGE)
			return messages;
		
		Message[] finalMessages = new Message[messagesCount];
		System.arraycopy(messages, 0, finalMessages, 0, messagesCount);
		return finalMessages;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (!inItem) {
			if ("item".equals(localName)) {
				if (messages == null) {
					messages = new Message[IGeoChatApi.MAX_PER_PAGE];
				}
				message = new Message();
				inItem = true;
				tagName = NONE;
				return;
			}
			return;
		}
		
		if ("title".equals(localName)) {
			tagName = TITLE;
		} else if ("guid".equals(localName)) {
			tagName = GUID;
		} else if ("pubDate".equals(localName)) {
			tagName = PUB_DATE;
		} else if ("SenderAlias".equals(localName)) {
			tagName = SENDER_ALIAS;
		} else if ("GroupAlias".equals(localName)) {
			tagName = GROUP_ALIAS;
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
			message.message = new String(ch, start, length);
			break;
		case GUID:
			message.guid = new String(ch, start, length);
			break;
		case PUB_DATE:
			String date = new String(ch, start, length);
			try {
				try {
					// This is faster, but I don't know if it always works
					message.createdDate = format.parse(date).getTime();
				} catch (Exception e) {
					// This works, but it's slower
					message.createdDate = org.instedd.geochat.api.DateUtils.parseDate(date).getTime();
				}
			} catch (NullPointerException e) {
				
			}
			break;
		case SENDER_ALIAS:
			message.fromUser = new String(ch, start, length);
			break;
		case GROUP_ALIAS:
			message.toGroup = new String(ch, start, length);
			break;
		case LAT:
			message.lat = Double.parseDouble(new String(ch, start, length));
			if (!(-90 <= message.lat && message.lat <= 90)) {
				message.lat = 0;
			}
			break;
		case LNG:
			message.lng = Double.parseDouble(new String(ch, start, length));
			if (!(-180 <= message.lng && message.lng <= 180)) {
				message.lng = 0;
			}
			break;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (inItem && "item".equals(localName)) {
			messages[messagesCount] = message;
			messagesCount++;
			inItem = false;
		}
		tagName = NONE;
	}

}
