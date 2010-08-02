package org.instedd.geochat.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import android.util.Xml;
import android.util.Xml.Encoding;

public class GeoChatApi implements IGeoChatApi {
	
	private final String user;
	private final String password;
	private final IRestClient client;

	public GeoChatApi(IRestClient restClient, String user, String password) {
		this.client = restClient;
		this.client.setAuth(user, password);
		this.user = user;
		this.password = password;
	}
	
	@Override
	public boolean credentialsAreValid() throws GeoChatApiException {
		try {
			InputStream is = this.client.get("https://geochat.instedd.org/api/users/" + encode(user) + "/verify.rss?password=" + encode(password));
			int b = is.read();
			return b == 't' || b == 'T';
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		}
	}

	@Override
	public Group[] getGroups(int page) throws GeoChatApiException {
		try {
			InputStream is = this.client.get("https://geochat.instedd.org/api/users/" + encode(user) + "/groups.rss?page=" + page);
			
			GroupHandler handler = new GroupHandler();
			Xml.parse(is, Encoding.UTF_8, handler);
			return handler.getGroups();
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		} catch (SAXException e) {
			throw new GeoChatApiException(e);
		}
	}

	@Override
	public Message[] getMessages(String groupAlias, int page) throws GeoChatApiException {
		try {
			InputStream is = this.client.get("https://geochat.instedd.org/api/groups/" + encode(groupAlias) + "/messages.rss?page=" + page);
			
			MessageHandler handler = new MessageHandler();
			Xml.parse(is, Encoding.UTF_8, handler);
			return handler.getMessages();
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		} catch (SAXException e) {
			throw new GeoChatApiException(e);
		}
	}

	@Override
	public User[] getUsers(String groupAlias, int page) throws GeoChatApiException {
		try {
			InputStream is = this.client.get("https://geochat.instedd.org/api/groups/" + encode(groupAlias) + "/members.rss?page=" + page);
			
			UserHandler handler = new UserHandler();
			Xml.parse(is, Encoding.UTF_8, handler);
			return handler.getUsers();
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		} catch (SAXException e) {
			throw new GeoChatApiException(e);
		}
	}

	@Override
	public void sendMessage(String message) throws GeoChatApiException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", message));
		try {
			this.client.post("https://geochat.instedd.org/api/messages", params);
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		}
	}

	@Override
	public void sendMessage(String groupAlias, String message) throws GeoChatApiException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", message));
		try {
			this.client.post("https://geochat.instedd.org/api/groups/" + encode(groupAlias) + "/messages", params);
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		}
	}
	
	private String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

}
