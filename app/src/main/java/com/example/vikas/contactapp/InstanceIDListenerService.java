package com.example.vikas.contactapp;

import android.content.Intent;

/**
 * Created by vikas on 20/1/16.
 */
public class InstanceIDListenerService extends
        com.google.android.gms.iid.InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
