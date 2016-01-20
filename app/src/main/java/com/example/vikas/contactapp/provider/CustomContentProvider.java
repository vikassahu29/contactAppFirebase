package com.example.vikas.contactapp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.vikas.contactapp.entity.Contact;
import com.example.vikas.contactapp.entity.User;

/**
 * Created by vikas on 20/1/16.
 */
public class CustomContentProvider extends ContentProvider {

    private static final int USER = 100;

    private static final int CONTACTS = 200;
    private static final int CONTACTS_ID = 201;

    private static final int DELETE_EVERYTHING = 300;

    public static final String CONTENT_AUTHORITY = "com.example.vikas.contactapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri DELETE_EVERYTHING_URI = BASE_CONTENT_URI.buildUpon()
            .appendPath("deleteEverything").build();

    private static UriMatcher sUriMatcher = buildUriMatcher();
    private DBManager mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, "user", USER);
        matcher.addURI(CONTENT_AUTHORITY, "contacts", CONTACTS);
        matcher.addURI(CONTENT_AUTHORITY, "contacts/*", CONTACTS_ID);
        matcher.addURI(CONTENT_AUTHORITY, "deleteEverything", DELETE_EVERYTHING);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBManager(getContext());
        return true;
    }

    //Not Used
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER:
                return User.CONTENT_TYPE;
            case CONTACTS:
                return Contact.CONTENT_TYPE;
            case CONTACTS_ID:
                return Contact.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER: {
                long row = db.insertOrThrow(User.TABLE, null, values);
                return row == -1 ? null : ContentUris.withAppendedId(User.CONTENT_URI, row);
            }
            case CONTACTS: {
                long row = db.insertOrThrow(Contact.TABLE, null, values);
                return row == -1 ? null : ContentUris.withAppendedId(Contact.CONTENT_URI, row);
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case DELETE_EVERYTHING: {
                int count = db.delete(User.TABLE, null, null);
                count += db.delete(Contact.TABLE, null, null);
                return count;
            }
            default: {
                throw new UnsupportedOperationException("Unknown delete uri: " + uri);
            }
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case USER: {
                return db.update(User.TABLE, values, selection, selectionArgs);
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }
}
