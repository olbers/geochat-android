package org.instedd.geochat.data;

import java.util.HashMap;

import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Messages;
import org.instedd.geochat.data.GeoChat.Users;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to the cached geochat database.
 */
public class GeoChatProvider extends ContentProvider {
	
	private static final String TAG = "GeoChatProvider";
	
	private static final String DATABASE_NAME = "geochat.db";
    private static final int DATABASE_VERSION = 2;
    
    private static final String USERS_TABLE_NAME = "users";
    private static final String GROUPS_TABLE_NAME = "groups";
    private static final String MESSAGES_TABLE_NAME = "messages";
    
    private static HashMap<String, String> sUsersProjectionMap;
    private static HashMap<String, String> sGroupsProjectionMap;
    private static HashMap<String, String> sMessagesProjectionMap;
    
    public final static int USERS = 1;
    public final static int GROUPS = 2;
    public final static int MESSAGES = 3;
    public final static int USER_MESSAGES = 4;
    public final static int GROUP_MESSAGES = 5;
    public final static int MESSAGE_ID = 6;
    public final static int GROUP_LAST_MESSAGE = 7;
    public final static int GROUP_ID = 8;
    public final static int USER_ID = 9;
    public final static int GROUP_MESSAGES_OLD = 10;
    public final static int GROUP_USERS = 11;
    public final static int USER_LOGIN = 12;
    
    private final static int MAX_MESSAGES_COUNT = 10;
    private final static String MAX_MESSAGES_COUNT_PLUS_ONE_STRING = String.valueOf(MAX_MESSAGES_COUNT + 1);
    
    public static final UriMatcher URI_MATCHER;
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + USERS_TABLE_NAME + " ("
                    + Users._ID + " INTEGER PRIMARY KEY,"
                    + Users.LOGIN + " TEXT,"
                    + Users.DISPLAY_NAME + " TEXT,"
                    + Users.LAT + " REAL,"
                    + Users.LNG + " REAL,"
                    + Users.LOCATION_NAME + " TEXT,"
                    + Users.GROUPS + " TEXT"
                    + ");");
            
            db.execSQL("CREATE TABLE " + GROUPS_TABLE_NAME + " ("
                    + Groups._ID + " INTEGER PRIMARY KEY,"
                    + Groups.NAME + " TEXT,"
                    + Groups.ALIAS + " TEXT,"
                    + Users.LAT + " REAL,"
                    + Users.LNG + " REAL,"
                    + Users.LOCATION_NAME + " TEXT"
                    + ");");
            
            db.execSQL("CREATE TABLE " + MESSAGES_TABLE_NAME + " ("
                    + Messages._ID + " INTEGER PRIMARY KEY,"
                    + Messages.GUID + " TEXT,"
                    + Messages.FROM_USER + " INTEGER,"
                    + Messages.TO_GROUP + " INTEGER,"
                    + Messages.MESSAGE + " TEXT,"
                    + Messages.LAT + " TEXT,"
                    + Messages.LNG + " TEXT,"
                    + Users.LOCATION_NAME + " TEXT,"
                    + Messages.CREATED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS groups");
            db.execSQL("DROP TABLE IF EXISTS messages");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
        case GROUPS:
        	count = db.delete(GROUPS_TABLE_NAME, where, whereArgs);
            break;
        case USERS:
        	count = db.delete(USERS_TABLE_NAME, where, whereArgs);
            break;
        case MESSAGES:
        	count = db.delete(MESSAGES_TABLE_NAME, where, whereArgs);
            break;
        case GROUP_ID:
            String groupId = uri.getPathSegments().get(1);
            count = db.delete(GROUPS_TABLE_NAME, Groups._ID + "=" + groupId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        case USER_ID:
            String userId = uri.getPathSegments().get(1);
            count = db.delete(USERS_TABLE_NAME, Groups._ID + "=" + userId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        case GROUP_MESSAGES_OLD:
        	String groupAlias = uri.getPathSegments().get(1);
        	Cursor c = db.query(MESSAGES_TABLE_NAME, new String[] { Messages._ID }, Messages.TO_GROUP + " = '" + groupAlias + "'", null, null, null, Messages.CREATED_DATE + " DESC", MAX_MESSAGES_COUNT_PLUS_ONE_STRING);
        	if (c.getCount() > MAX_MESSAGES_COUNT) {
        		c.moveToPosition(MAX_MESSAGES_COUNT - 1);
        		int id = c.getInt(0);
        		count = db.delete(MESSAGES_TABLE_NAME, Messages.TO_GROUP + " = '" + groupAlias + "' AND " + Messages._ID + " <= " + id, null);
        	} else {
        		count = 0;
        	}
        	c.close();
        	break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        if (count > 0) {
        	getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
        case USERS:
        case USER_ID:
            return Users.CONTENT_TYPE;
        case GROUPS:
        case GROUP_ID:
        	return Groups.CONTENT_TYPE;
        case MESSAGES:
        case MESSAGE_ID:
        case GROUP_LAST_MESSAGE:
        	return Messages.CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId;
		
		switch(URI_MATCHER.match(uri)) {
		case USERS:
			rowId = db.insert(USERS_TABLE_NAME, Users.LOGIN, values);
			if (rowId > 0) {
				Uri userUri = ContentUris.withAppendedId(GeoChat.Users.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(userUri, null);
	            return userUri;
			}
			break;
		case GROUPS:
			rowId = db.insert(GROUPS_TABLE_NAME, Groups.ALIAS, values);
			if (rowId > 0) {
				Uri groupUri = ContentUris.withAppendedId(GeoChat.Groups.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(groupUri, null);
	            return groupUri;
			}
			break;
		case MESSAGES:        
			rowId = db.insert(MESSAGES_TABLE_NAME, Messages.MESSAGE, values);
			if (rowId > 0) {
				Uri messageUri = ContentUris.withAppendedId(GeoChat.Messages.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(messageUri, null);
	            return messageUri;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = sortOrder;
		boolean onlyOne = false;

        switch (URI_MATCHER.match(uri)) {
        case USERS:
        	qb.setTables(USERS_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Users.DEFAULT_SORT_ORDER;
            }
        	break;
        case GROUPS:
        	qb.setTables(GROUPS_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Groups.DEFAULT_SORT_ORDER;
            }
        	break;
        case MESSAGES:
        	qb.setTables(MESSAGES_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case USER_MESSAGES:
        	qb.setTables(MESSAGES_TABLE_NAME);
        	qb.appendWhere(Messages.FROM_USER + " = ");
        	qb.appendWhereEscapeString(uri.getPathSegments().get(1));        	
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case GROUP_MESSAGES:
        	qb.setTables(MESSAGES_TABLE_NAME);
        	qb.appendWhere(Messages.TO_GROUP + " = ");
        	qb.appendWhereEscapeString(uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case GROUP_USERS:
        	qb.setTables(USERS_TABLE_NAME);
        	qb.appendWhere(Users.GROUPS + " LIKE '%" + uri.getPathSegments().get(1) + "%'");
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Users.DEFAULT_SORT_ORDER;
            }
        	break;
        case MESSAGE_ID:
        	qb.setTables(MESSAGES_TABLE_NAME);
        	qb.appendWhere(Messages._ID + " = " + uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case GROUP_LAST_MESSAGE:
        	qb.setTables(MESSAGES_TABLE_NAME);
        	qb.appendWhere(Messages.TO_GROUP + " = ");
        	qb.appendWhereEscapeString(uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Messages.DEFAULT_SORT_ORDER;
            }
        	onlyOne = true;
        	break;
        case GROUP_ID:
        	qb.setTables(GROUPS_TABLE_NAME);
        	qb.appendWhere(Groups._ID + " = " + uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Groups.DEFAULT_SORT_ORDER;
            }
        	break;
        case USER_ID:
        	qb.setTables(USERS_TABLE_NAME);
        	qb.appendWhere(Users._ID + " = " + uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Users.DEFAULT_SORT_ORDER;
            }
        	break;
        case USER_LOGIN:
        	qb.setTables(USERS_TABLE_NAME);
        	qb.appendWhere(Users.LOGIN + " = ");
        	qb.appendWhereEscapeString(uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Users.DEFAULT_SORT_ORDER;
            }
        	break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c;

        if (onlyOne) {
        	c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy, "1");
        } else {
        	c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);        	
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
        case USER_ID:
            String userId = uri.getPathSegments().get(1);
            count = db.update(USERS_TABLE_NAME, values, Users._ID + "=" + userId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        case GROUP_ID:
            String groupId = uri.getPathSegments().get(1);
            count = db.update(GROUPS_TABLE_NAME, values, Groups._ID + "=" + groupId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "users", USERS);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "users/#", USER_ID);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "users/*", USER_LOGIN);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "users/*/messages", USER_MESSAGES);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "groups", GROUPS);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "groups/#", GROUP_ID);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "groups/*/messages", GROUP_MESSAGES);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "groups/*/users", GROUP_USERS);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "groups/*/messages/last", GROUP_LAST_MESSAGE);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "groups/*/messages/old", GROUP_MESSAGES_OLD);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "messages", MESSAGES);
        URI_MATCHER.addURI(GeoChat.AUTHORITY, "messages/#", MESSAGE_ID);

        sUsersProjectionMap = new HashMap<String, String>();
        sUsersProjectionMap.put(Users._ID, Users._ID);
        sUsersProjectionMap.put(Users.LOGIN, Users.LOGIN);
        sUsersProjectionMap.put(Users.DISPLAY_NAME, Users.DISPLAY_NAME);
        sUsersProjectionMap.put(Users.LAT, Users.LAT);
        sUsersProjectionMap.put(Users.LNG, Users.LNG);
        sUsersProjectionMap.put(Users.LOCATION_NAME, Users.LOCATION_NAME);
        
        sGroupsProjectionMap = new HashMap<String, String>();
        sGroupsProjectionMap.put(Groups._ID, Groups._ID);
        sGroupsProjectionMap.put(Groups.NAME, Groups.NAME);
        sGroupsProjectionMap.put(Groups.ALIAS, Groups.ALIAS);
        sGroupsProjectionMap.put(Groups.LAT, Groups.LAT);
        sGroupsProjectionMap.put(Groups.LNG, Groups.LNG);
        sGroupsProjectionMap.put(Groups.LOCATION_NAME, Groups.LOCATION_NAME);
        
        sMessagesProjectionMap = new HashMap<String, String>();
        sMessagesProjectionMap.put(Messages._ID, Messages._ID);
        sMessagesProjectionMap.put(Messages.GUID, Messages.GUID);
        sMessagesProjectionMap.put(Messages.FROM_USER, Messages.FROM_USER);
        sMessagesProjectionMap.put(Messages.TO_GROUP, Messages.TO_GROUP);
        sMessagesProjectionMap.put(Messages.MESSAGE, Messages.MESSAGE);
        sMessagesProjectionMap.put(Messages.LAT, Messages.LAT);
        sMessagesProjectionMap.put(Messages.LNG, Messages.LNG);
        sMessagesProjectionMap.put(Messages.LOCATION_NAME, Messages.LOCATION_NAME);
        sMessagesProjectionMap.put(Messages.CREATED_DATE, Messages.CREATED_DATE);
	}

}
