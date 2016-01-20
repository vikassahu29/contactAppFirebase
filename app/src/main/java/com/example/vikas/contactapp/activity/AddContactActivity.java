package com.example.vikas.contactapp.activity;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.entity.Contact;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.vikas.contactapp.utils.Utils.FIREBASE_URL;

/**
 * Created by vikas on 20/1/16.
 */
public class AddContactActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private SparseArray<Contact> mContactSparseArray;

    private RecyclerView mRecyclerView;
    private ContactAdapter mAdapter;
    private Firebase mContactRef;

    private static final int INSERTED = 1;
    private static final int REMOVED = 0;
    private static final int FULL = -1;

    private static final String[] PROJECTION_LIST = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
    };

    private static final String SORT_ORDER_LIST = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            + " ASC ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        mContactSparseArray = new SparseArray<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == getResources()
                        .getConfiguration().orientation ? LinearLayoutManager.VERTICAL :
                        LinearLayoutManager.HORIZONTAL), false));
        mAdapter = new ContactAdapter(new ArrayList<Contact>());
        mRecyclerView.setAdapter(mAdapter);
        mContactRef = new Firebase(FIREBASE_URL + "/contacts/" + PrefUtils.getUid(this));
        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_save) {
            int size = mContactSparseArray.size();
            for (int i = 0; i < size; i++) {
                Contact contact = mContactSparseArray.valueAt(i);
                Map<String, String> map = new HashMap<>();
                map.put("name", contact.name);
                if (contact.hasPhoneNumber) {
                    Cursor cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? ",
                            new String[]{contact.lookUpKey},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC LIMIT 1");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            contact.phoneNumber = cursor.getString(cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            map.put("phoneNumber", contact.phoneNumber);
                        }
                        cursor.close();
                    }
                } else {
                    Cursor cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? ",
                            new String[]{contact.lookUpKey},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC LIMIT 1");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            contact.emailAddress = cursor.getString(cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Email.ADDRESS));
                            map.put("emailAddress", contact.emailAddress);
                        }
                        cursor.close();
                    }
                }
                mContactRef.push().setValue(map);
            }
            Toast.makeText(this, "Contacts Saved", Toast.LENGTH_SHORT).show();
            finish();
        }

        return true;
    }

    public void startLoading() {
        getSupportLoaderManager().initLoader(123123, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
                PROJECTION_LIST, null, null, SORT_ORDER_LIST);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            ArrayList<Contact> contactArrayList = new ArrayList<>(data.getCount());
            int idColumnIndex = data.getColumnIndex(ContactsContract.Contacts._ID);
            int displayNameColumnIndex = data.getColumnIndex(ContactsContract.Contacts
                    .DISPLAY_NAME_PRIMARY);
            int lookUpKeyColumnIndex = data.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
            int hasPhoneNumberColumnIndex = data.getColumnIndex(ContactsContract.Contacts
                    .HAS_PHONE_NUMBER);
            do {
                Contact contact = new Contact();
                contact._id = data.getInt(idColumnIndex);
                contact.lookUpKey = data.getString(lookUpKeyColumnIndex);
                contact.name = data.getString(displayNameColumnIndex);
                contact.hasPhoneNumber = data.getInt(hasPhoneNumberColumnIndex) == 1;
                contactArrayList.add(contact);
            } while (data.moveToNext());
            mAdapter.changeDataSet(contactArrayList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private ArrayList<Contact> mDataSet;

        public ContactAdapter(ArrayList<Contact> dataList) {
            mDataSet = dataList;
            this.notifyDataSetChanged();
        }

        public void changeDataSet(ArrayList<Contact> dataList) {
            mDataSet = dataList;
            this.notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(AddContactActivity.this).inflate(
                    R.layout.item_contact_list, parent, false);
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

        private class ContactViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener{

            private TextView mName;
            private CheckBox mCheckBox;
            private Contact mContact;

            public ContactViewHolder(View itemView) {
                super(itemView);
                mName = (TextView) itemView.findViewById(R.id.tv_name);
                mCheckBox = (CheckBox) itemView.findViewById(R.id.check_box);
                itemView.setOnClickListener(this);
            }

            public void render(Contact contact) {
                mContact = contact;
                mName.setText(contact.name);
                if (isPresent(contact)) {
                    mCheckBox.setChecked(true);
                } else {
                    mCheckBox.setChecked(false);
                }
            }

            @Override
            public void onClick(View v) {
                switch (addRemoveContact(mContact)) {
                    case INSERTED:
                        mCheckBox.setChecked(true);
                        break;
                    case REMOVED:
                        mCheckBox.setChecked(false);
                        break;
                    case FULL:
                        Toast.makeText(v.getContext(), "Only 5 contacts can be selected",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    private int addRemoveContact(Contact contact) {
        Contact tempContact = mContactSparseArray.get(contact._id, null);
        if (tempContact == null) {
            if (mContactSparseArray.size() == 5) {
                return FULL;
            } else {
                mContactSparseArray.put(contact._id, contact);
                return INSERTED;
            }
        } else {
            mContactSparseArray.remove(contact._id);
            return REMOVED;
        }
    }

    private boolean isPresent(Contact contact) {
        return mContactSparseArray.get(contact._id, null) != null;
    }
}
