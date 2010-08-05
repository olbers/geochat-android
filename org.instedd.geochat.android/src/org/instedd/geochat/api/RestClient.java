package org.instedd.geochat.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

public class RestClient implements IRestClient {
	
	private DefaultHttpClient client;
	private String auth;
	
	public RestClient() {
		this.client = new DefaultHttpClient();
	}

	public InputStream get(String url) throws IOException {
		HttpGet get = new HttpGet(url);
		auth(get);
		HttpResponse response = this.client.execute(get);
		if (response.getStatusLine().getStatusCode() == 404) {
			return null;
		}
		return response.getEntity().getContent();
	}
	
	public void post(String url, List<NameValuePair> params) throws IOException {
		HttpPost post = new HttpPost(url);
		auth(post);
		post.setEntity(new UrlEncodedFormEntity(params));
		post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		HttpResponse response = this.client.execute(post);
		response.getEntity().consumeContent();
	}

	public void setAuth(String user, String password) {
		this.auth = "Basic " + Base64.encodeBytes((user + ":" + password).getBytes());
	}
	
	private void auth(HttpRequestBase request) {
		if (auth != null) {
			request.addHeader("Authorization", auth);
		}
	}
	
	

}
