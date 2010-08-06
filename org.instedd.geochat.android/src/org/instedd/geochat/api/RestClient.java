package org.instedd.geochat.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class RestClient implements IRestClient {
	
	private HttpClient client;
	private String auth;
	
	public RestClient() {
		this.client = initClient();
	}

	private HttpClient initClient() {
		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 100);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		           
		// Create and initialize scheme registry 
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		        
		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(cm, params);
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
