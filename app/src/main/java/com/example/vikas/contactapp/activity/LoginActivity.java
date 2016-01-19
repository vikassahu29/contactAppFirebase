package com.example.vikas.contactapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import static com.example.vikas.contactapp.utils.Utils.FIREBASE_URL;
/**
 * Created by vikas on 19/1/16.
 */
public class LoginActivity extends Activity implements View.OnClickListener{

    private EditText mEmail;
    private EditText mPassword;
    private Firebase mFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.et_email);
        mPassword = (EditText) findViewById(R.id.et_password);

        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
        mFirebase = new Firebase(FIREBASE_URL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (email.length() == 0) {
                    mEmail.setError("Email can't be empty");
                    return;
                }
                if (password.length() == 0) {
                    mPassword.setError("Password can't be empty");
                    return;
                }
                mFirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit()
                                .putString(PrefUtils.PREF_UID, authData.getUid())
                                .putBoolean(PrefUtils.PREF_IS_LOGGED_IN, true)
                                .commit();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        switch (firebaseError.getCode()) {
                            case FirebaseError.INVALID_CREDENTIALS:
                                Snackbar.make(new CoordinatorLayout(LoginActivity.this),
                                        "Invalid Credentials provided",
                                        Snackbar.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.NETWORK_ERROR:
                                Snackbar.make(new CoordinatorLayout(LoginActivity.this),
                                        "Network error. Please try after some time",
                                        Snackbar.LENGTH_SHORT).show();
                                break;
                            default:
                                Snackbar.make(new CoordinatorLayout(LoginActivity.this),
                                        "Unknown Error Occurred",
                                        Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.btn_register:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }
}
