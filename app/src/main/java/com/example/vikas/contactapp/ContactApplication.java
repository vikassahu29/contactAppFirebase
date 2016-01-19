package com.example.vikas.contactapp;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by vikas on 19/1/16.
 */
public class ContactApplication extends Application{

    private static ContactApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        sInstance = this;
    }

    public static synchronized ContactApplication getInstance() {
        return sInstance;
    }
}
