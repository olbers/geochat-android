package org.instedd.geochat.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

public class MockHttpEntity implements HttpEntity {
	
	private final InputStream stream;

	public MockHttpEntity(InputStream stream) {
		this.stream = stream;
	}

	public void consumeContent() throws IOException {
		
	}

	public InputStream getContent() throws IOException, IllegalStateException {
		return stream;
	}

	public Header getContentEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Header getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isChunked() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRepeatable() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return false;
	}

	public void writeTo(OutputStream outstream) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
