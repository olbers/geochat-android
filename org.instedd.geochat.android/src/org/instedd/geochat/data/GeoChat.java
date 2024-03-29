package org.instedd.geochat.data;

import java.util.TreeSet;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Provides access to the cached GeoChat database, that is,
 * data from the network - like groups, users and messages -
 * is cached locally for faster access.
 */
public class GeoChat {
	
	public static final String AUTHORITY = "org.instedd.geochat.provider.GeoChat";

    // This class cannot be instantiated
    private GeoChat() {}
    
    public static interface Locatable {
    	/**
         * The latitude
         * <P>Type: REAL</P>
         */
        String LAT = "lat";
        
        /**
         * The longitude
         * <P>Type: REAL</P>
         */
        String LNG = "lng";
        
        /**
         * The name of the location
         * <P>Type: TEXT</P>
         */
        String LOCATION_NAME = "location_name";
    }
    
    /**
     * The users table.
     */
    public final static class Users implements Locatable, BaseColumns {
    	// This class cannot be instantiated
        private Users() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/users");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of users.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geochat.user";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single user.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geochat.user";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "lower(display_name || login) ASC";
        
        /**
         * The login of the user
         * <P>Type: TEXT</P>
         */
        public static final String LOGIN = "login";
        
        /**
         * The display name of the user
         * <P>Type: TEXT</P>
         */
        public static final String DISPLAY_NAME = "display_name";
        
        /**
         * The groups this user belongs to
         * <P>Type: TEXT</P>
         */
        public static final String GROUPS = "groups";
        
        public static TreeSet<String> getGroups(String groups) {
        	String[] split = groups.split(",");
        	int splitLength = split.length;
        	TreeSet<String> tree = new TreeSet<String>();
        	for (int i = 0; i < splitLength; i++) {
				tree.add(split[i]);
			}
        	return tree;
        }
        
        public static String getGroups(TreeSet<String> groups) {
        	StringBuilder sb = new StringBuilder();
        	int i = 0;
        	for(String group : groups) {
        		if (i != 0)
        			sb.append(',');
				sb.append(group);
				i++;
			}
        	return sb.toString();
        }
    }
    
    /**
     * The groups table.
     */
    public final static class Groups implements Locatable, BaseColumns {
    	// This class cannot be instantiated
        private Groups() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/groups");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of groups.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geochat.group";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single group.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geochat.group";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "lower(name) ASC";
        
        /**
         * The name of the group
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";
        
        /**
         * The alias of the group
         * <P>Type: ALIAS</P>
         */
        public static final String ALIAS = "alias";
        
    }
    
    /**
     * The messages table.
     */
    public final static class Messages implements Locatable, BaseColumns {
    	// This class cannot be instantiated
        private Messages() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/messages");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of messages.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geochat.message";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single message.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geochat.message";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "created DESC";
        
        /**
         * The guid of the message
         * <P>Type: TEXT</P>
         */
        public static final String GUID = "guid";
        
        /**
         * The id of the user that sent the message
         * <P>Type: TEXT</P>
         */
        public static final String FROM_USER = "from_user";
        
        /**
         * The id of the group this message was sent to
         * <P>Type: TEXT</P>
         */
        public static final String TO_GROUP = "to_group";
        
        /**
         * The body of the message
         * <P>Type: TEXT</P>
         */
        public static final String MESSAGE = "message";
        
        /**
         * The timestamp for when the message was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";
    }

}
