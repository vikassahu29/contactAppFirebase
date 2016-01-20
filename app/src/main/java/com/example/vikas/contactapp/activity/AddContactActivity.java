package com.example.vikas.contactapp.activity;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikas.contactapp.R;
import com.example.vikas.contactapp.entity.Contact;
import com.example.vikas.contactapp.provider.CustomContentProvider;
import com.example.vikas.contactapp.utils.PrefUtils;
import com.firebase.client.Firebase;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.vikas.contactapp.utils.Utils.CONTACT_URL;

/**
 * Created by vikas on 20/1/16.
 */
public class AddContactActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SparseArray<ContactView> mContactSparseArray;

    private ContactAdapter mAdapter;
    private Firebase mContactRef;
    private FlowLayout mFlowLayout;

    private static final int INSERTED = 1;
    private static final int REMOVED = 0;
    private static final int FULL = -1;

    private static final int READ_CONTACTS_PERMISSION = 1;

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
        } else {
            startLoading();
        }
        mContactSparseArray = new SparseArray<>();
        mFlowLayout = (FlowLayout) findViewById(R.id.flow_layout);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        mAdapter = new ContactAdapter(new ArrayList<Contact>());
        recyclerView.setAdapter(mAdapter);
        mContactRef = new Firebase(CONTACT_URL + PrefUtils.getUid(this));

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

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                ContentProviderOperation.Builder op = ContentProviderOperation
                        .newInsert(Contact.CONTENT_URI);
                Contact contact = mContactSparseArray.valueAt(i).contact;
                Map<String, String> map = new HashMap<>();
                map.put("name", contact.name);
                op.withValue(Contact.NAME, contact.name);
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
                            op.withValue(Contact.PHONE_NUMBER, contact.phoneNumber);
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
                            op.withValue(Contact.EMAIL_ADDRESS, contact.emailAddress);
                        }
                        cursor.close();
                    }
                }
                operations.add(op.build());
                mContactRef.push().setValue(map);
            }
            try {
                getContentResolver().applyBatch(CustomContentProvider.CONTENT_AUTHORITY,
                        operations);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
            if (size > 0) {
                Toast.makeText(this, R.string.contacts_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.nothing_to_save, Toast.LENGTH_SHORT).show();
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLoading();
                } else {
                    finish();
                }
            }
        }
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
                    R.layout.item_contact_add, parent, false);
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
                View.OnClickListener {

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
                        Toast.makeText(v.getContext(), R.string.max_contact_warning,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    private int addRemoveContact(Contact contact) {
        ContactView tempContactView = mContactSparseArray.get(contact._id, null);
        if (tempContactView == null) {
            int size = mContactSparseArray.size();
            if (size == 5) {
                return FULL;
            } else {
                TextView textView = new TextView(this);
                textView.setText(contact.name);
                FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(10, 20, 10, 20);
                textView.setLayoutParams(layoutParams);
                textView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                textView.setTextSize(14);
                textView.setPadding(5, 5, 5, 5);
                ContactView contactView = new ContactView();
                contactView.contact = contact;
                contactView.view = textView;
                mFlowLayout.addView(textView);
                mContactSparseArray.put(contact._id, contactView);
                return INSERTED;
            }
        } else {
            mFlowLayout.removeView(tempContactView.view);
            mContactSparseArray.remove(tempContactView.contact._id);
            return REMOVED;
        }
    }

    private boolean isPresent(Contact contact) {
        return mContactSparseArray.get(contact._id, null) != null;
    }

    public class ContactView {
        public Contact contact;
        public View view;
    }
}
