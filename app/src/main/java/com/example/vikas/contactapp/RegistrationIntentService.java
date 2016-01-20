package com.example.vikas.contactapp;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.Firebase;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import static com.example.vikas.contactapp.utils.Utils.GCM_URL;

/**
 * Created by vikas on 20/1/16.
 */
public class RegistrationIntentService extends IntentService {

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToServer(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRegistrationToServer(String token) {
        String uid = PrefUtils.getUid(this);
        if (!TextUtils.isEmpty(uid)) {
            Firebase gcmRef = new Firebase(GCM_URL + uid);
            gcmRef.setValue(token);
        }
    }


}
