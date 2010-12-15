package org.instedd.geochat.test;

import junit.framework.TestCase;

import org.instedd.geochat.data.GeoChatData;

public class GeoChatDataTest extends TestCase {
	
	public void testGetUserIconFilename() {
		String filename = GeoChatData.getUserIconFilename("http://bact.blogspot.com");
		assertEquals("user_http___bact_blogspot_com.png", filename);
	}

}
