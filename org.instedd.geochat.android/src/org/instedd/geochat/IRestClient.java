package org.instedd.geochat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.NameValuePair;

public interface IRestClient {
	
	void setAuth(String user, String password);
	
	InputStream get(String url) throws IOException;
	
	void post(String url, List<NameValuePair> params) throws IOException;

}
