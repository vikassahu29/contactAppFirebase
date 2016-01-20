package com.example.vikas.contactapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.example.vikas.contactapp.utils.Utils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Firebase mProfileRef;
    private TextView mName;
    private TextView mEmail;
    private EditText mEtName;
    private Button mSave;

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

        mProfileRef = new Firebase(Utils.FIREBASE_URL + "/profiles/" + PrefUtils.getUid(this));
        mProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName.setText((String) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(MainActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_contact);
        fab.setOnClickListener(this);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab_requests);
        fab1.setOnClickListener(this);
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
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().clear();
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
            case R.id.fab_add_contact:
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                startActivity(intent);
                break;
            case R.id.fab_requests:
                break;
            case R.id.tv_name:
                mName.setVisibility(View.GONE);
                mEtName.setText(mName.getText());
                mEtName.setVisibility(View.VISIBLE);
                mSave.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_save:
                String name = mEtName.getText().toString();
                if (name.length() < 3) {
                    mEtName.setError("Name should have at least 3 characters");
                    break;
                }
                mProfileRef.setValue(name);
                mName.setText(name);
                mEtName.setVisibility(View.GONE);
                mSave.setVisibility(View.GONE);
                mName.setVisibility(View.VISIBLE);
                break;
        }
    }
}
