package org.instedd.geochat.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.instedd.geochat.IRestClient;

public class RestClient implements IRestClient {
	
	private DefaultHttpClient client;
	
	public RestClient() {
		this.client = new DefaultHttpClient();
	}

	@Override
	public InputStream get(String url) throws IOException {
		HttpGet get = new HttpGet(url);
		return this.client.execute(get).getEntity().getContent();
	}

	@Override
	public void setAuth(String user, String password) {
		this.client.getCredentialsProvider().setCredentials(
				new AuthScope(null, -1),
				new UsernamePasswordCredentials(user, password));
	}

}
