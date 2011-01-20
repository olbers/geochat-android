package org.instedd.geochat.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoChatApi implements IGeoChatApi {
	
	private final static String base = "http://geochat-stg.instedd.org/api";
	private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private final String user;
	private final String password;
	private final IRestClient client;

	public GeoChatApi(IRestClient restClient, String user, String password) {
		this.client = restClient;
		this.client.setAuth(user, password);
		this.user = user;
		this.password = password;
	}
	
	public boolean credentialsAreValid() throws GeoChatApiException {
		try {
			HttpResponse response = this.client.get(base + "/users/" + encode(user) + "/verify?password=" + encode(password));
			if (response == null) throw new GeoChatApiException("Status not HTTP_OK (200) on credentialsAreValid");
			
			InputStream content = response.getEntity().getContent();
			try {
				int b = content.read();
				return b == 't' || b == 'T';
			} finally {
				content.close();
			}
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		}
	}

	public Group[] getGroups() throws GeoChatApiException {
		try {
			HttpResponse response = this.client.get(base + "/users/" + encode(user) + "/groups.json");
			if (response == null) throw new GeoChatApiException("Status not HTTP_OK (200) on getGroups");
			
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			try {
				return toGroups(content, entity.getContentLength());
			} finally {
				content.close();
			}
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		} catch (JSONException e) {
			throw new GeoChatApiException(e);
		}
	}	

	public Message[] getMessages(String groupAlias, int page) throws GeoChatApiException {
		try {
			HttpResponse response = this.client.get(base + "/groups/" + encode(groupAlias) + "/messages.json?page=" + page);
			if (response == null) throw new GeoChatApiException("Status not HTTP_OK (200) on getMessages");
			
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			try {
				return toMessages(content, entity.getContentLength());
			} finally {
				content.close();
			}
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		} catch (JSONException e) {
			throw new GeoChatApiException(e);
		}
	}	

	public User[] getUsers(String groupAlias) throws GeoChatApiException {
		try {
			HttpResponse response = this.client.get(base + "/api/groups/" + encode(groupAlias) + "/members.json");
			if (response == null) throw new GeoChatApiException("Status not HTTP_OK (200) on getUsers");
			
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			try {
				return toUsers(content, entity.getContentLength());
			} finally {
				content.close();
			}
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		} catch (JSONException e) {
			throw new GeoChatApiException(e);
		}
	}

	public HttpResponse getUserIcon(String login, int size) throws GeoChatApiException {
		try {
			return this.client.get(base + "/users/" + encode(login) + "/icon?size=" + size);
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		}
	}

	public void sendMessage(String message) throws GeoChatApiException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", message));
		try {
			this.client.post(base + "/messages", params);
		} catch (IOException e) {
			throw new GeoChatApiException(e);
		}
	}

	public void sendMessage(String groupAlias, String message) throws GeoChatApiException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", message));
		try {
			this.client.post(base + "/groups/" + encode(groupAlias) + "/messages", params);
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
	
	private static Group[] toGroups(InputStream content, long length) throws IOException, JSONException {
		JSONArray array = new JSONArray(getString(content, length));
		
		int count = array.length();
		Group[] groups = new Group[count];
		for(int i = 0; i < count; i++) {
			JSONObject object = array.getJSONObject(i);
			Group group = new Group();
			group.alias = object.optString("alias");
			group.lat = object.optDouble("lat");
			group.lng = object.optDouble("long");
			group.name = object.optString("name");
			groups[i] = group;
		}
		return groups;
	}

	private static Message[] toMessages(InputStream content, long length) throws IOException, JSONException {
		JSONObject obj = new JSONObject(getString(content, length));
		JSONArray array = obj.getJSONArray("items");
		
		int count = array.length();
		Message[] messages = new Message[count];
		for(int i = 0; i < count; i++) {
			JSONObject object = array.getJSONObject(i);
			Message msg = new Message();
			try {
				// SimpleDateFormat doesn't understand Z, but it understands GMT :-(
				String created = object.optString("created");
				if (created.endsWith("Z"))
					created = created.substring(0, created.length() - 1) + "GMT";
				msg.createdDate = format.parse(created).getTime();
			} catch (ParseException e) {
			}
			msg.fromUser = object.optString("sender");
			msg.guid = object.optString("id");
			msg.lat = object.optDouble("lat");
			msg.lng = object.optDouble("long");
			msg.message = object.optString("text");
			msg.toGroup = object.optString("group");
			messages[i] = msg;
		}
		return messages;
	}
	
	private User[] toUsers(InputStream content, long length) throws IOException, JSONException {
		JSONArray array = new JSONArray(getString(content, length));
		
		int count = array.length();
		User[] users = new User[count];
		for(int i = 0; i < count; i++) {
			JSONObject object = array.getJSONObject(i);
			User user = new User();
			user.displayName = object.optString("displayName");
			user.lat = object.optDouble("lat");
			user.lng = object.optDouble("long");
			user.login = object.optString("login");
			users[i] = user;
		}
		return users;
	}
	
	private static String getString(InputStream content, long length) throws IOException, JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(content), 8192);
		StringBuilder sb = new StringBuilder(length < 0 ? 100 : (int)length);
		
		String line = null;
		while((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		
		return sb.toString();
	}

}
