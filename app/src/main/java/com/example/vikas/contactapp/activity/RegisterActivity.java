package com.example.vikas.contactapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import static com.example.vikas.contactapp.utils.Utils.FIREBASE_URL;

/**
 * Created by vikas on 20/1/16.
 */
public class RegisterActivity extends Activity {

    private Firebase mFirebase;
    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebase = new Firebase(FIREBASE_URL);
        mName = (EditText) findViewById(R.id.et_name);
        mEmail = (EditText) findViewById(R.id.et_email);
        mPassword = (EditText) findViewById(R.id.et_password);

        Button register = (Button) findViewById(R.id.btn_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = mName.getText().toString();
                final String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (name.length() < 3) {
                    mName.setError(getString(R.string.name_length_error));
                    return;
                }

                if (email.isEmpty()) {
                    mEmail.setError(getString(R.string.email_empty_error));
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError(getString(R.string.password_length_error));
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage(getString(R.string.registering));
                progressDialog.setCancelable(false);
                progressDialog.show();

                mFirebase.createUser(email, password, new Firebase.ValueResultHandler
                        <Map<String, Object>>() {

                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        progressDialog.hide();
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name);
                        map.put("email", email);
                        map.put("uid", result.get("uid"));
                        mFirebase.child("profiles").child(result.get("uid").toString())
                                .setValue(map);
                        Toast.makeText(RegisterActivity.this, R.string.registration_message_success,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        progressDialog.hide();
                        switch (firebaseError.getCode()) {
                            case FirebaseError.EMAIL_TAKEN:
                                Toast.makeText(RegisterActivity.this, R.string.email_exist_error,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.INVALID_EMAIL:
                                Toast.makeText(RegisterActivity.this, R.string.invalid_email_error,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.NETWORK_ERROR:
                                Toast.makeText(RegisterActivity.this, R.string.network_error,
                                        Toast.LENGTH_SHORT).show();
                            default:
                                Toast.makeText(RegisterActivity.this, R.string.unknown_error,
                                        Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }


}
