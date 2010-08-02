package org.instedd.geochat.api;

import java.io.InputStream;


public interface IGeoChatApi {
	
	int MAX_PER_PAGE = 10;

	boolean credentialsAreValid() throws GeoChatApiException;
	
	Group[] getGroups(int page) throws GeoChatApiException;
	
	User[] getUsers(String groupAlias, int page) throws GeoChatApiException;
	
	InputStream getUserIcon(String login, int size) throws GeoChatApiException;
	
	Message[] getMessages(String groupAlias, int page) throws GeoChatApiException;

	void sendMessage(String message) throws GeoChatApiException;

	void sendMessage(String groupAlias, String message) throws GeoChatApiException;

}
