package org.instedd.geochat.data;

import java.util.HashMap;

import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.data.GeoChat.Locations;
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
    private static final int DATABASE_VERSION = 1;
    
    private static final String USERS_TABLE_NAME = "users";
    private static final String GROUPS_TABLE_NAME = "groups";
    private static final String MESSAGES_TABLE_NAME = "messages";
    private static final String LOCATIONS_TABLE_NAME = "locations";
    
    private static HashMap<String, String> sUsersProjectionMap;
    private static HashMap<String, String> sGroupsProjectionMap;
    private static HashMap<String, String> sMessagesProjectionMap;
    private static HashMap<String, String> sLocationsProjectionMap;
    
    private final static int USERS = 1;
    private final static int GROUPS = 2;
    private final static int MESSAGES = 3;
    private final static int LOCATIONS = 4;
    private final static int LOCATION_LAT_LNG = 5;
    private final static int USER_MESSAGES = 6;
    private final static int GROUP_MESSAGES = 7;
    private final static int MESSAGE_ID = 8;
    private final static int GROUP_LAST_MESSAGE = 9;
    private final static int GROUP_ID = 10;
    private final static int USER_ID = 11;
    
    private static final UriMatcher sUriMatcher;
    
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
                    + Users.LOCATION_NAME + " TEXT"
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
            
            db.execSQL("CREATE TABLE " + LOCATIONS_TABLE_NAME + " ("
                    + Locations._ID + " INTEGER PRIMARY KEY,"
                    + Locations.LAT + " TEXT,"
                    + Locations.LNG + " TEXT,"
                    + Locations.NAME + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS groups");
            db.execSQL("DROP TABLE IF EXISTS messages");
            db.execSQL("DROP TABLE IF EXISTS locations");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case GROUPS:
        	count = db.delete(GROUPS_TABLE_NAME, where, whereArgs);
            break;
        case USERS:
        	count = db.delete(USERS_TABLE_NAME, where, whereArgs);
            break;
        case MESSAGES:
        	count = db.delete(MESSAGES_TABLE_NAME, where, whereArgs);
            break;
        case LOCATIONS:
        	count = db.delete(LOCATIONS_TABLE_NAME, where, whereArgs);
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
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
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
        case LOCATIONS:
        case LOCATION_LAT_LNG:
        	return Locations.CONTENT_TYPE;
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
		
		switch(sUriMatcher.match(uri)) {
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
		case LOCATIONS:
			rowId = db.insert(LOCATIONS_TABLE_NAME, Locations.NAME, values);
			if (rowId > 0) {
				Uri locationUri = ContentUris.withAppendedId(GeoChat.Locations.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(locationUri, null);
	            return locationUri;
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

        switch (sUriMatcher.match(uri)) {
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
        case LOCATIONS:
        	qb.setTables(LOCATIONS_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Locations.DEFAULT_SORT_ORDER;
            }
        	break;
        case LOCATION_LAT_LNG:
        	qb.setTables(LOCATIONS_TABLE_NAME);
        	qb.appendWhere(Locations.LAT + " = " + uri.getPathSegments().get(1));
        	qb.appendWhere(Locations.LNG + " = " + uri.getPathSegments().get(2));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = GeoChat.Locations.DEFAULT_SORT_ORDER;
            }
        	break;
        case USER_MESSAGES:
        	qb.setTables(MESSAGES_TABLE_NAME);
        	qb.appendWhere(Messages.FROM_USER + " = ");
        	qb.appendWhereEscapeString(uri.getPathSegments().get(1));        	if (TextUtils.isEmpty(sortOrder)) {
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
        	qb.appendWhere(Users._ID + " = ");
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
        switch (sUriMatcher.match(uri)) {
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
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "users", USERS);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "users/#", USER_ID);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "users/*/messages", USER_MESSAGES);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "groups", GROUPS);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "groups/#", GROUP_ID);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "groups/*/messages", GROUP_MESSAGES);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "groups/*/messages/last", GROUP_LAST_MESSAGE);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "messages", MESSAGES);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "messages/#", MESSAGE_ID);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "locations", LOCATIONS);
        sUriMatcher.addURI(GeoChat.AUTHORITY, "location/#/#", LOCATION_LAT_LNG);

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
        
        sLocationsProjectionMap = new HashMap<String, String>();
        sLocationsProjectionMap.put(Locations._ID, Locations._ID);
        sLocationsProjectionMap.put(Locations.LAT, Locations.LAT);
        sLocationsProjectionMap.put(Locations.LNG, Locations.LNG);
        sLocationsProjectionMap.put(Locations.NAME, Locations.NAME);
	}

}
