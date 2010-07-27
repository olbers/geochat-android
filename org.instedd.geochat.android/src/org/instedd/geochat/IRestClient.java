package org.instedd.geochat;

import java.io.IOException;
import java.io.InputStream;

public interface IRestClient {
	
	void setAuth(String user, String password);
	
	InputStream get(String url) throws IOException;

}
