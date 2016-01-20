package com.example.vikas.contactapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import static com.example.vikas.contactapp.utils.Utils.FIREBASE_URL;

/**
 * Created by vikas on 19/1/16.
 */
public class LoginActivity extends Activity implements View.OnClickListener {

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
                    mEmail.setError(getString(R.string.email_empty_error));
                    return;
                }
                if (password.length() == 0) {
                    mPassword.setError(getString(R.string.password_empty_error));
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.signing_in));
                progressDialog.setCancelable(false);
                progressDialog.show();

                mFirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        progressDialog.hide();
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
                        progressDialog.hide();
                        switch (firebaseError.getCode()) {
                            case FirebaseError.INVALID_CREDENTIALS:
                            case FirebaseError.INVALID_PASSWORD:
                            case FirebaseError.INVALID_EMAIL:
                            case FirebaseError.USER_DOES_NOT_EXIST:
                                Toast.makeText(LoginActivity.this,
                                        R.string.invalid_credentials_error, Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case FirebaseError.NETWORK_ERROR:
                                Toast.makeText(LoginActivity.this, R.string.network_error,
                                        Toast.LENGTH_SHORT).show();

                                break;
                            default:
                                Toast.makeText(LoginActivity.this, R.string.unknown_error,
                                        Toast.LENGTH_SHORT).show();
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
