package com.example.vikas.contactapp.entity;

import android.net.Uri;
import android.provider.BaseColumns;

import static com.example.vikas.contactapp.provider.CustomContentProvider.BASE_CONTENT_URI;

/**
 * Created by vikas on 20/1/16.
 */
public class User implements BaseColumns {

    public int _id;
    public String uid;
    public String name;
    public String email;


    public static final String TABLE = "user";

    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath("user").build();

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.contactapp.users";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.contactapp.users";

    //DB Columns
    public static final String NAME = "name";
    public static final String UID = "uid";
    public static final String EMAIL = "email";


}
