package org.instedd.geochat.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
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
	public void post(String url, List<NameValuePair> params) throws IOException {
		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(params));
		post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		HttpResponse response = this.client.execute(post);
		response.getEntity().consumeContent();
	}

	@Override
	public void setAuth(String user, String password) {
		this.client.getCredentialsProvider().setCredentials(
				new AuthScope(null, -1),
				new UsernamePasswordCredentials(user, password));
	}

}
