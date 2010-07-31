package org.instedd.geochat.api;


public interface IGeoChatApi {
	
	int MAX_PER_PAGE = 10;

	boolean credentialsAreValid() throws Exception;
	
	Group[] getGroups(int page) throws Exception;
	
	User[] getUsers(String groupAlias, int page) throws Exception;
	
	Message[] getMessages(String groupAlias, int page) throws Exception;

	void sendMessage(String message) throws Exception ;

	void sendMessage(String groupAlias, String message) throws Exception ;

}
