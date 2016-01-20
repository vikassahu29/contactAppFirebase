package com.example.vikas.contactapp.activity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.entity.Contact;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import static com.example.vikas.contactapp.utils.Utils.CONTACT_URL;

/**
 * Created by vikas on 20/1/16.
 */
public class RequestsActivity extends BaseActivity {

    private ContactViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        Firebase contactRef = new Firebase(CONTACT_URL + PrefUtils.getUid(this));
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new ContactViewAdapter(new ArrayList<Contact>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == getResources()
                        .getConfiguration().orientation ? LinearLayoutManager.VERTICAL :
                        LinearLayoutManager.HORIZONTAL), false));
        recyclerView.setAdapter(mAdapter);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);
        progressDialog.show();
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.hide();
                ArrayList<Contact> contactArrayList = new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Contact contact = dataSnapshot1.getValue(Contact.class);
                    contactArrayList.add(contact);
                }
                mAdapter.changeDataSet(contactArrayList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                progressDialog.hide();
                switch (firebaseError.getCode()) {
                    case FirebaseError.NETWORK_ERROR:
                        Toast.makeText(RequestsActivity.this, R.string.network_error,
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(RequestsActivity.this, R.string.unknown_error,
                                Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class ContactViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private ArrayList<Contact> mDataSet;

        public ContactViewAdapter(ArrayList<Contact> dataList) {
            mDataSet = dataList;
            this.notifyDataSetChanged();
        }

        public void changeDataSet(ArrayList<Contact> dataList) {
            mDataSet = dataList;
            this.notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(RequestsActivity.this).inflate(
                    R.layout.item_contact_view, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ContactViewHolder) holder).render(mDataSet.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        private class ContactViewHolder extends RecyclerView.ViewHolder {

            private TextView mName;
            private TextView mData;

            public ContactViewHolder(View itemView) {
                super(itemView);
                mName = (TextView) itemView.findViewById(R.id.tv_name);
                mData = (TextView) itemView.findViewById(R.id.tv_data);
            }

            public void render(Contact contact) {
                mName.setText(contact.name);
                if (TextUtils.isEmpty(contact.phoneNumber)) {
                    mData.setText(contact.emailAddress);
                } else {
                    mData.setText(contact.phoneNumber);
                }
            }
        }
    }
}
