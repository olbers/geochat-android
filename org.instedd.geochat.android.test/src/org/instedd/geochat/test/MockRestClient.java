package org.instedd.geochat.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.instedd.geochat.IRestClient;

public class MockRestClient implements IRestClient {
	
	private String user;
	private String password;
	private String response;
	private String getUrl;
	
	public MockRestClient(String response) {
		this.response = response;
	}

	@Override
	public InputStream get(String url) {
		this.getUrl = url;
		return new ByteArrayInputStream(response.getBytes());
	}

	@Override
	public void setAuth(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getGetUrl() {
		return getUrl;
	}

}
