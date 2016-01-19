package com.example.vikas.contactapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

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
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mName = (EditText) findViewById(R.id.et_name);
        mEmail = (EditText) findViewById(R.id.et_email);
        mPassword = (EditText) findViewById(R.id.et_password);
        mRegister = (Button) findViewById(R.id.btn_register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (name.length() < 3) {
                    mName.setError("Name should have at least 3 characters");
                    return;
                }
                if (email.isEmpty()) {
                    mEmail.setError("Email can't be empty");
                    return;
                }
                if (password.length() < 6) {
                    mPassword.setError("Password should be at least 6 characters long");
                    return;
                }
                mFirebase.createUser(email, password, new Firebase.ValueResultHandler
                        <Map<String, Object>>() {

                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        mFirebase.child("profiles").child(result.get("uid").toString())
                                .setValue(name);
                        Toast.makeText(RegisterActivity.this, "You have been successfully" +
                                "registered. Please login to proceed", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(RegisterActivity.this, firebaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mFirebase = new Firebase(FIREBASE_URL);

    }


}
