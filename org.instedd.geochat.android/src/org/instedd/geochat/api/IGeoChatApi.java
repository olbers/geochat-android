package org.instedd.geochat.api;

import org.apache.http.HttpResponse;


public interface IGeoChatApi {
	
	int MAX_PER_PAGE = 50;

	boolean credentialsAreValid() throws GeoChatApiException;
	
	Group[] getGroups() throws GeoChatApiException;
	
	User[] getUsers(String groupAlias) throws GeoChatApiException;
	
	HttpResponse getUserIcon(String login, int size) throws GeoChatApiException;
	
	Message[] getMessages(String groupAlias, int page) throws GeoChatApiException;

	void sendMessage(String message) throws GeoChatApiException;

	void sendMessage(String groupAlias, String message) throws GeoChatApiException;

}
