package com.example.vikas.contactapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.vikas.contactapp.entity.Contact;
import com.example.vikas.contactapp.entity.User;

/**
 * Created by vikas on 20/1/16.
 */
public class DBManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contact_app.db";
    private static final int DATABASE_VERSION = 1;

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + User.TABLE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + User.NAME + " TEXT,"
                + User.EMAIL + " TEXT,"
                + User.UID + " TEXT,"
                + "UNIQUE (" + User.UID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Contact.TABLE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contact.NAME + " TEXT,"
                + Contact.EMAIL_ADDRESS + " TEXT,"
                + Contact.PHONE_NUMBER + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
