package com.example.vikas.contactapp.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.Firebase;

import static com.example.vikas.contactapp.utils.Utils.FIREBASE_URL;

/**
 * Created by vikas on 20/1/16.
 */
public class RequestsActivity extends BaseActivity {

    private Firebase mContactsRef;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        mContactsRef = new Firebase(FIREBASE_URL + "/contacts/" + PrefUtils.getUid(this));

    }
}
