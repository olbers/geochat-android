package org.instedd.geochat.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.instedd.geochat.IRestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		InputStreamReader isReader = new InputStreamReader(is);
		BufferedReader buffReader = new BufferedReader(isReader);
		String line = buffReader.readLine();
		return line != null && line.toLowerCase().equals("true");
	}

	@Override
	public Group[] getGroups(int page) throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/users/" + user + "/groups.rss?page=" + page);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList items = doc.getElementsByTagName("item");
		List<Group> groups = new ArrayList<Group>();
		if (items != null) {
			int itemsLength = items.getLength();
			for (int i = 0; i < itemsLength; i++) {
				Node item = items.item(i);
				NodeList itemTags = item.getChildNodes();
				if (itemTags != null) {
					Group group = new Group();
					int itemTagsLength = itemTags.getLength();
					for (int j = 0; j < itemTagsLength; j++) {
						Node itemTag = itemTags.item(j);
						String name = itemTag.getNodeName();
						String value = getNodeValue(itemTag);
						if ("title".equals(name)) {
							group.name = value;
						} else if (name.endsWith(":Alias")) {
							group.alias = value;
						} else if (name.endsWith(":lat")) {
							group.lat = Double.valueOf(value);
						} else if (name.endsWith(":long")) {
							group.lng = Double.valueOf(value);
						}
					}
					groups.add(group);
				}
			}
		}
		return groups.toArray(new Group[groups.size()]);
	}

	@Override
	public Message[] getMessages(String groupAlias, int page) throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/groups/" + groupAlias + "/messages.rss?page=" + page);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList items = doc.getElementsByTagName("item");
		List<Message> msgs = new ArrayList<Message>();
		if (items != null) {
			int itemsLength = items.getLength();
			for (int i = 0; i < itemsLength; i++) {
				Node item = items.item(i);
				NodeList itemTags = item.getChildNodes();
				if (itemTags != null) {
					Message msg = new Message();
					int itemTagsLength = itemTags.getLength();
					for (int j = 0; j < itemTagsLength; j++) {
						Node itemTag = itemTags.item(j);
						String name = itemTag.getNodeName();
						String value = getNodeValue(itemTag);
						if ("title".equals(name)) {
							msg.message = value;
						} else if ("guid".equals(name)) {
							msg.guid = value;
						} else if ("pubDate".equals(name)) {
							msg.createdDate = DateUtils.parseDate(value).getTime();
						} else if (name.endsWith(":SenderAlias")) {
							msg.fromUser = value;
						} else if (name.endsWith(":GroupAlias")) {
							msg.toGroup = value;
						} else if (name.endsWith(":lat")) {
							msg.lat = Double.valueOf(value);
						} else if (name.endsWith(":long")) {
							msg.lng = Double.valueOf(value);
						}
					}
					msgs.add(msg);
				}
			}
		}
		return msgs.toArray(new Message[msgs.size()]);
	}

	@Override
	public User[] getUsers(String groupAlias, int page) throws Exception {
		InputStream is = this.client.get("http://geochat.instedd.org/api/groups/" + groupAlias + "/members.rss?page=" + page);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList items = doc.getElementsByTagName("item");
		List<User> users = new ArrayList<User>();
		if (items != null) {
			int itemsLength = items.getLength();
			for (int i = 0; i < itemsLength; i++) {
				Node item = items.item(i);
				NodeList itemTags = item.getChildNodes();
				if (itemTags != null) {
					User user = new User();
					int itemTagsLength = itemTags.getLength();
					for (int j = 0; j < itemTagsLength; j++) {
						Node itemTag = itemTags.item(j);
						String name = itemTag.getNodeName();
						String value = getNodeValue(itemTag);
						if ("title".equals(name)) {
							user.displayName = value;
						} else if (name.endsWith(":Login")) {
							user.login = value;
						}
					}
					users.add(user);
				}
			}
		}
		return users.toArray(new User[users.size()]);
	}
	
	private String getNodeValue(Node node) {
		NodeList children = node.getChildNodes();
		if (children.getLength() > 0) {
			return children.item(0).getNodeValue();
		} else {
			return null;
		}
	}

}
