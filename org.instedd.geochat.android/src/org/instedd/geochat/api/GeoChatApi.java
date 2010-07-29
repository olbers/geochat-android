package org.instedd.geochat.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.instedd.geochat.IRestClient;

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
	public boolean credentialsAreValid() throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/users/" + user + "/verify.rss?password=" + password);
		int b = is.read();
		return b == 't' || b == 'T';
	}

	@Override
	public Group[] getGroups(int page) throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/users/" + user + "/groups.rss?page=" + page);
		
		GroupHandler handler = new GroupHandler();
		Xml.parse(is, Encoding.UTF_8, handler);
		return handler.getGroups();
	}

	@Override
	public Message[] getMessages(String groupAlias, int page) throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/groups/" + groupAlias + "/messages.rss?page=" + page);
		
		MessageHandler handler = new MessageHandler();
		Xml.parse(is, Encoding.UTF_8, handler);
		return handler.getMessages();
	}

	@Override
	public User[] getUsers(String groupAlias, int page) throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/groups/" + groupAlias + "/members.rss?page=" + page);
		
		UserHandler handler = new UserHandler();
		Xml.parse(is, Encoding.UTF_8, handler);
		return handler.getUsers();
	}

	@Override
	public void sendMessage(String message) throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", message));
		this.client.post("http://geochat.instedd.org/api/messages", params);
	}

	@Override
	public void sendMessage(String groupAlias, String message) throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", message));
		this.client.post("http://geochat.instedd.org/api/groups/" + groupAlias + "/messages", params);
	}

}
