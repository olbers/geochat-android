package org.instedd.geochat.test;

import junit.framework.TestCase;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.Message;
import org.instedd.geochat.api.User;

public class GeoChatApiTest extends TestCase {
	
	public void testCredentialsFalse() throws Exception {
		MockRestClient restClient = new MockRestClient("false");
		GeoChatApi api = new GeoChatApi(restClient, "foo", "bar");
		assertFalse(api.credentialsAreValid());
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://geochat.instedd.org/api/users/foo/verify.rss?password=bar", restClient.getGetUrl());
	}
	
	public void testCredentialsTrue() throws Exception {
		MockRestClient restClient = new MockRestClient("true");
		GeoChatApi api = new GeoChatApi(restClient, "foo", "bar");
		assertTrue(api.credentialsAreValid());
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://geochat.instedd.org/api/users/foo/verify.rss?password=bar", restClient.getGetUrl());
	}
	
	public void testGroups() throws Exception {
		MockRestClient restClient = new MockRestClient("<?xml version=\"1.0\"?>\n" + 
				"<rss version=\"2.0\"\n" + 
				"     xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\n" + 
				"     xmlns:geochat=\"http://geochat.instedd.org/api/1.0\">\n" + 
				"  <channel>\n" + 
				"    <title>GeoChat - Groups</title>\n" + 
				"    <lastBuildDate>Tue, 10 Jun 2003 04:00:00 GMT</lastBuildDate>\n" + 
				"    <!-- TBD: link to next page should be added here -->\n" + 
				" \n" + 
				"    <item>\n" + 
				"      <title>Group 1</title>\n" + 
				"      <link>http://geochat.instedd.org/api/groups/{alias1}</link>\n" + 
				"      <description>{Group #1 Description}</description>\n" + 
				"      <pubDate>Tue, 10 Jan 2009 04:00:00 GMT</pubDate>\n" + 
				"      <guid>http://geochat.instedd.org/api/groups/{alias1}</guid>\n" + 
				"      <geochat:Alias>alias1</geochat:Alias>\n" + 
				"      <geochat:IsPublic>true</geochat:IsPublic>\n" + 
				"      <geochat:RequireApprovalToJoin>true</geochat:RequireApprovalToJoin>\n" + 
				"      <geochat:CreationDate>Mon, 09 Jan 2009 04:00:00 GMT</geochat:CreationDate>\n" + 
				"      <geochat:IsChatRoom>true</geochat:IsChatRoom>\n" + 
				"      <geochat:Owner>{Owner #1 Display Name}</geochat:Owner>\n" + 
				"      <geochat:Owner>{Owner #2 Display Name}</geochat:Owner>\n" + 
				"      <geochat:MembersCount>14</geochat:MembersCount>\n" + 
				"      <geo:lat>16.23</geo:lat>\n" + 
				"      <geo:long>-47.15</geo:long> \n" + 
				"    </item>\n" + 
				"\n" + 
				"    <item>\n" + 
				"      <title>Group 2</title>\n" + 
				"      <link>http://geochat.instedd.org/api/groups/{alias2}</link>\n" + 
				"      <description>{Group #2 Description}</description>\n" + 
				"      <pubDate>Tue, 10 Jan 2009 04:00:00 GMT</pubDate>\n" + 
				"      <guid>http://geochat.instedd.org/api/groups/{alias2}</guid>\n" + 
				"      <geochat:Alias>alias2</geochat:Alias>\n" + 
				"      <geochat:IsPublic>false</geochat:IsPublic>\n" + 
				"      <geochat:RequireApprovalToJoin>false</geochat:RequireApprovalToJoin>\n" + 
				"      <geochat:CreationDate>Mon, 09 Jan 2009 04:00:00 GMT</geochat:CreationDate>\n" + 
				"      <geochat:IsChatRoom>false</geochat:IsChatRoom>\n" + 
				"      <geochat:Owner>{Owner #1 Display Name}</geochat:Owner>\n" + 
				"      <geochat:Owner>{Owner #2 Display Name}</geochat:Owner>\n" + 
				"      <geochat:MembersCount>14</geochat:MembersCount>  \n" + 
				"      <geo:lat>16.23</geo:lat>\n" + 
				"      <geo:long>-47.15</geo:long> \n" + 
				"    </item>\n" + 
				"  </channel>\n" + 
				"</rss>");
		
		GeoChatApi api = new GeoChatApi(restClient, "foo", "bar");
		Group[] groups = api.getGroups(1);
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://geochat.instedd.org/api/users/foo/groups.rss?page=1", restClient.getGetUrl());
		assertEquals(2, groups.length);
		
		assertEquals("Group 1", groups[0].name);
		assertEquals("alias1", groups[0].alias);
		assertEquals(Double.valueOf("16.23"), groups[0].lat);
		assertEquals(Double.valueOf("-47.15"), groups[0].lng);
	}
	
	public void testUsers() throws Exception {
		MockRestClient restClient = new MockRestClient("<?xml version=\"1.0\"?>\n" + 
				"<rss version=\"2.0\" " +
				"  xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\n" + 
				"  xmlns:geochat=\"http://geochat.instedd.org/api/1.0\">\n" + 
				"  <channel>\n" + 
				"    <title>GeoChat - Group Members</title>\n" + 
				"    <lastBuildDate>Tue, 10 Jun 2003 04:00:00 GMT</lastBuildDate>\n" + 
				" \n" + 
				"    <item>\n" + 
				"      <title>Member 1</title>\n" + 
				"      <link>http://geochat.instedd.org/api/users/{login1}</link>\n" + 
				"      <pubDate>Tue, 10 Jan 2009 04:00:00 GMT</pubDate>\n" + 
				"      <guid>http://geochat.instedd.org/api/users/{login1}</guid>\n" + 
				"      <geochat:Login>login1</geochat:Login>\n" + 
				"      <geo:lat>16.23</geo:lat>\n" + 
				"      <geo:long>-47.15</geo:long> \n" + 
				"    </item>\n" + 
				"    <item>\n" + 
				"      <title>Member 2</title>\n" + 
				"      <link>http://geochat.instedd.org/api/users/{login2}</link>\n" + 
				"      <pubDate>Tue, 10 Jan 2009 04:00:00 GMT</pubDate>\n" + 
				"      <guid>http://geochat.instedd.org/api/users/{login2}</guid>\n" + 
				"      <geochat:Login>login2</geochat:Login>\n" + 
				"      <geo:lat>16.23</geo:lat>\n" + 
				"      <geo:long>-47.15</geo:long> \n" + 
				"    </item>\n" + 
				"  </channel>\n" + 
				"</rss>");
		
		GeoChatApi api = new GeoChatApi(restClient, "foo", "bar");
		User[] users = api.getUsers("group", 1);
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://geochat.instedd.org/api/groups/group/members.rss?page=1", restClient.getGetUrl());
		assertEquals(2, users.length);
		
		assertEquals("Member 1", users[0].displayName);
		assertEquals("login1", users[0].login);
		assertEquals(Double.valueOf("16.23"), users[0].lat);
		assertEquals(Double.valueOf("-47.15"), users[0].lng);
	}
	
	public void testMessages() throws Exception {
		MockRestClient restClient = new MockRestClient("<?xml version=\"1.0\"?>\n" + 
				"<rss version=\"2.0\"\n" + 
				"     xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\n" + 
				"     xmlns:geochat=\"http://geochat.instedd.org/api/1.0\">\n" + 
				"  <channel>\n" + 
				"    <title>GeoChat – Messages in Group {Group Name}</title>\n" + 
				"    <lastBuildDate>Tue, 10 Jun 2003 04:00:00 GMT</lastBuildDate>\n" + 
				"    <!-- TBD: link to next page should be added here -->\n" + 
				" \n" + 
				"    <item>\n" + 
				"      <title>{Message #1}</title>\n" + 
				"      <Authors>{Display Name}</Authors>\n" + 
				"      <Categories> {Message} </Categories>\n" + 
				"      <link>http://geochat.instedd.org/api/messages/{messageId}</link>\n" + 
				"      <description>{Message Description}</description>\n" + 
				"      <pubDate>Tue, 10 Jan 2009 04:00:00 GMT</pubDate>\n" + 
				"      <guid>http://geochat.instedd.org/api/messages/{messageId}</guid>\n" + 
				"      <geochat:ThreadId>{threadId}</geochat:ThreadId> \n" + 
				"      <geochat:ResponseOf>{messageId}</geochat:ResponseOf>\n" + 
				"      <geochat:SenderAlias>{Sender Alias Name}</geochat:SenderAlias>\n" + 
				"      <geochat:GroupAlias>{Group Alias}</geochat:GroupAlias>\n" + 
				"      <geochat:Route>{SMS}</geochat:Route>\n" + 
				"      <geochat:IsGroupBlast>true</geochat:IsGroupBlast>\n" + 
				"      <geo:lat>16.23</geo:lat>\n" + 
				"      <geo:long>-47.15</geo:long> \n" + 
				"    </item>\n" + 
				"   \n" + 
				"    <item>\n" + 
				"      <title>{Message #2}</title>\n" + 
				"      <Authors>{Display Name}</Authors>  \n" + 
				"      <Categories> {Message} </Categories>\n" + 
				"      <link>http://geochat.instedd.org/api/messages/{messageId}</link>\n" + 
				"      <description>{Message Description}</description>\n" + 
				"      <pubDate>Tue, 10 Jan 2009 04:00:00 GMT</pubDate>\n" + 
				"      <guid>http://geochat.instedd.org/api/messages/{messageId}</guid>\n" + 
				"      <geochat:ThreadId>{threadId}</geochat:ThreadId> \n" + 
				"      <geochat:ResponseOf>{messageId}</geochat:ResponseOf>\n" + 
				"      <geochat:Authors>{Display Name}</geochat:Authors>\n" + 
				"      <geochat:SenderAlias>{Sender Alias Name}</geochat:SenderAlias>\n" + 
				"      <geochat:GroupAlias>{Group Alias}</geochat:GroupAlias>\n" + 
				"      <geochat:Route>{SMS}</geochat:Route>\n" + 
				"      <geochat:IsGroupBlast>true</geochat:IsGroupBlast>\n" + 
				"      <geo:lat>16.23</geo:lat>\n" + 
				"      <geo:long>-47.15</geo:long> \n" + 
				"    </item>\n" + 
				"  </channel>\n" + 
				"</rss>");
		
		GeoChatApi api = new GeoChatApi(restClient, "foo", "bar");
		Message[] messages = api.getMessages("group", 1);
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://geochat.instedd.org/api/groups/group/messages.rss?page=1", restClient.getGetUrl());
		assertEquals(2, messages.length);
		
		assertEquals("http://geochat.instedd.org/api/messages/{messageId}", messages[0].guid);
//		assertEquals(DateUtils.parseDate("Tue, 10 Jan 2009 04:00:00 GMT").getTime(), messages[0].createdDate);
		assertEquals("{Sender Alias Name}", messages[0].fromUser);
		assertEquals("{Group Alias}", messages[0].toGroup);
		assertEquals("{Message #1}", messages[0].message);
		assertEquals(Double.valueOf("16.23"), messages[0].lat);
		assertEquals(Double.valueOf("-47.15"), messages[0].lng);
	}

}
