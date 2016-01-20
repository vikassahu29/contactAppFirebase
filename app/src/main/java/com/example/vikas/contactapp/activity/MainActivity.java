package com.example.vikas.contactapp.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.RegistrationIntentService;
import com.example.vikas.contactapp.entity.User;
import com.example.vikas.contactapp.provider.CustomContentProvider;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.example.vikas.contactapp.utils.Utils.PROFILE_URL;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Firebase mProfileRef;
    private TextView mName;
    private TextView mEmail;
    private EditText mEtName;
    private Button mSave;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmail = (TextView) findViewById(R.id.tv_email);
        mName = (TextView) findViewById(R.id.tv_name);
        mEtName = (EditText) findViewById(R.id.et_name);
        mSave = (Button) findViewById(R.id.btn_save);

        mName.setOnClickListener(this);
        mSave.setOnClickListener(this);

        String uid = PrefUtils.getUid(this);

        if (!TextUtils.isEmpty(uid)) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            mProfileRef = new Firebase(PROFILE_URL + uid);

            mProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressDialog.hide();
                    mUser = dataSnapshot.getValue(User.class);
                    ContentValues values = new ContentValues();
                    values.put(User.NAME, mUser.name);
                    values.put(User.EMAIL, mUser.email);
                    values.put(User.UID, mUser.uid);
                    getContentResolver().insert(User.CONTENT_URI, values);

                    mEmail.setText(mUser.email);
                    mName.setText(mUser.name);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    progressDialog.hide();
                    switch (firebaseError.getCode()) {
                        case FirebaseError.NETWORK_ERROR:
                            Toast.makeText(MainActivity.this, R.string.network_error,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MainActivity.this, R.string.unknown_error,
                                    Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if (checkPlayServices()) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }


        findViewById(R.id.fab_add_contact).setOnClickListener(this);
        findViewById(R.id.fab_requests).setOnClickListener(this);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Toast.makeText(this, R.string.push_notification_warning, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logout) {
            mProfileRef.unauth();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().clear().apply();
            getContentResolver().delete(CustomContentProvider.DELETE_EVERYTHING_URI, null, null);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_contact: {
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.fab_requests: {
                Intent intent = new Intent(MainActivity.this, RequestsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_name: {
                mName.setVisibility(View.GONE);
                mEtName.setText(mName.getText());
                mEtName.setVisibility(View.VISIBLE);
                mSave.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.btn_save: {
                String name = mEtName.getText().toString();
                if (name.length() < 3) {
                    mEtName.setError(getString(R.string.name_length_error));
                    break;
                }
                mUser.name = name;
                mProfileRef.setValue(mUser);
                ContentValues values = new ContentValues();
                values.put(User.NAME, mUser.name);
                getContentResolver().update(User.CONTENT_URI, values, User.UID + " = ? ",
                        new String[]{mUser.uid});
                mName.setText(name);
                mEtName.setVisibility(View.GONE);
                mSave.setVisibility(View.GONE);
                mName.setVisibility(View.VISIBLE);
                break;
            }
        }
    }
}
