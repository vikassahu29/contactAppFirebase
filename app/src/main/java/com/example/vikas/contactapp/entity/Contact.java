package com.example.vikas.contactapp.entity;

import android.net.Uri;
import android.provider.BaseColumns;

import static com.example.vikas.contactapp.provider.CustomContentProvider.BASE_CONTENT_URI;

/**
 * Created by vikas on 20/1/16.
 */
public class Contact implements BaseColumns {

    public Integer _id;
    public String name;
    public String lookUpKey;
    public Boolean hasPhoneNumber;
    public String phoneNumber;
    public String emailAddress;

    public static final String TABLE = "contacts";

    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath("contacts").build();

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.contactapp.contacts";
    public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.contactapp.contacts";

    //DB Columns
    public static final String NAME = "name";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String EMAIL_ADDRESS = "email_address";
}
