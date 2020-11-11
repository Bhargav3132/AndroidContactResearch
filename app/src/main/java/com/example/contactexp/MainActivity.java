package com.example.contactexp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.provider.BaseColumns._ID;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String[] FROM_COLUMNS = {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY

            };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the CONTACT_KEY column
    private static final int CONTACT_KEY_INDEX = 1;

    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
    // Defines a variable for the search string
    private String searchString = "";
    // Defines the array to hold values that replace the ?
    private String[] selectionArgs = {searchString};

    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            android.R.id.text1
    };
    // Define global mutable variables
    // Define a ListView object
    ListView contactsList;
    // Define variables for the contact the user selects
    // The contact's _ID value
    long contactId;
    // The contact's LOOKUP_KEY
    String contactKey;
    // A content URI for the selected contact
    Uri contactUri;

    String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AccountManager accountManager = AccountManager.get(this); //this is Activity
        Account account = new Account("Known2Me", "com.known2me");
        boolean success = accountManager.addAccountExplicitly(account, "password", null);
        if (success) {
            Log.d(TAG, "Account created");
        } else {
            Log.d(TAG, "Account creation failed. Look at previous logs to investigate");
        }


        contactsList = findViewById(android.R.id.list);

        getLoaderManager().initLoader(0, null, MainActivity.this);

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent nextScreen = new Intent(MainActivity.this, OperationActivity.class);
                startActivity(nextScreen);*/

                addContact(getSampleContact("003 Bapu"));

            }
        });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        selectionArgs[0] = "%" + searchString + "%";
        // Starts the query
        return new CursorLoader(
                MainActivity.this,
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        );
//        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
// Put the result Cursor in the adapter for the ListView
//        cursorAdapter.swapCursor(cursor);
        contactsList.setAdapter(new CustomAdapter(MainActivity.this, cursor));
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
    }

    public void displayRawContact(String contactID) {


        String selection = ContactsContract.RawContacts.CONTACT_ID + "= ?";
        ArrayList<String> selectionArgs = new ArrayList();
        selectionArgs.add(contactID);
        final String[] projection = null;

        Cursor cursor = this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection,
                selectionArgs.toArray(new String[selectionArgs.size()]), null);
        String[] columnNames = cursor.getColumnNames();
        String message = "\n";

        ArrayList<String> rawIds = new ArrayList<>();

        while (cursor != null && cursor.moveToNext()) {
            rawIds.add(cursor.getString(cursor.getColumnIndex(_ID)));
            message += "\n========New RAW=======\n";
            for (String columnName : columnNames) {
                if (cursor.getType(cursor.getColumnIndex(columnName)) != FIELD_TYPE_NULL && cursor.getType(cursor.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
                    String value = cursor.getString(cursor.getColumnIndex(columnName));
                    message += columnName + " = " + value + "\n";

                }
            }
        }

        Log.i("Full RAW Contact:- ", message);

        displayData(rawIds);

        if (!cursor.isClosed()) cursor.close();


    }

    public void displayData(ArrayList<String> rawIds) {

        String ids = "(";
        for (int i = 0; i < rawIds.size(); i++) {
            if (i == rawIds.size() - 1) {
                ids += "?";
            } else {
                ids += "?,";
            }
        }
        ids += ")";

        String selection = ContactsContract.Data.RAW_CONTACT_ID + " IN " + ids;
        ArrayList<String> selectionArgs = rawIds;
        final String[] projection = null;

        Cursor cursor = this.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection,
                selectionArgs.toArray(new String[selectionArgs.size()]), null);
        String[] columnNames = cursor.getColumnNames();

        Log.w("Full Data Contact:- ", "");

        while (cursor != null && cursor.moveToNext()) {
            String message = "\n";
            Log.w("Full Data Contact:- ", "\n--------New DATA--------\n");
            for (String columnName : columnNames) {
                if (cursor.getType(cursor.getColumnIndex(columnName)) != FIELD_TYPE_NULL && cursor.getType(cursor.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
                    String value = cursor.getString(cursor.getColumnIndex(columnName));

                    message += columnName + "=" + value + "\n";
                }
            }
            Log.w("", message);
        }


        if (!cursor.isClosed()) cursor.close();


    }

    private Contact getSampleContact(String name) {
        Contact contact = new Contact(null);
        contact.givenName = name;
        contact.familyName = "Mota";
        contact.nickname = "Hitman";
        contact.company = "IPL";

        ArrayList<Item> emailList = new ArrayList<>();
        emailList.add(new Item(null, "Work", "rohit@bcci.com"));
        emailList.add(new Item(null, "Personal", "sharma@gmail.com"));
        contact.emails = emailList;

        ArrayList<Item> phoneList = new ArrayList<>();
        phoneList.add(new Item(null, "Work", "+919538715743"));
        phoneList.add(new Item(null, "Personal", "0091789463"));
        contact.phones = phoneList;

        ArrayList<Item> webList = new ArrayList<>();
        webList.add(new Item(null, "Work", "www.ipl.com"));
        webList.add(new Item(null, "Home", "www.test.com"));
        contact.websites = webList;

        ArrayList<PostalAddress> addressList = new ArrayList<>();
        addressList.add(new PostalAddress(null, "Home", "123-Park View", "Law Garden", "Ahmedabad", "382210", "West", "India", null));
        contact.postalAddresses = addressList;

        contact.note = "Winner of ipl 2020";

        return contact;
    }

    private boolean addContact(Contact contact) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.known2me")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "Known2Me");
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, contact.givenName)
                .withValue(StructuredName.MIDDLE_NAME, contact.middleName)
                .withValue(StructuredName.FAMILY_NAME, contact.familyName)
                .withValue(StructuredName.PREFIX, contact.prefix)
                .withValue(StructuredName.SUFFIX, contact.suffix);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(CommonDataKinds.Note.NOTE, contact.note);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(CommonDataKinds.Organization.COMPANY, contact.company)
                .withValue(CommonDataKinds.Organization.TITLE, contact.jobTitle);
        ops.add(op.build());

        //Photo
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                .withValue(CommonDataKinds.Photo.PHOTO, contact.avatar)
                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        ops.add(op.build());

        op.withYieldAllowed(true);

        //Phones
        for (Item phone : contact.phones) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, phone.value);

            if (Item.stringToPhoneType(phone.label) == CommonDataKinds.Phone.TYPE_CUSTOM) {
                op.withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.BaseTypes.TYPE_CUSTOM);
                op.withValue(CommonDataKinds.Phone.LABEL, phone.label);
            } else {
                op.withValue(CommonDataKinds.Phone.TYPE, Item.stringToPhoneType(phone.label));
            }

            ops.add(op.build());
        }

        //Emails
        for (Item email : contact.emails) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.ADDRESS, email.value)
                    .withValue(CommonDataKinds.Email.TYPE, Item.stringToEmailType(email.label));
            ops.add(op.build());
        }
        //Postal addresses
        for (PostalAddress address : contact.postalAddresses) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.StructuredPostal.TYPE, PostalAddress.stringToPostalAddressType(address.label))
                    .withValue(CommonDataKinds.StructuredPostal.LABEL, address.label)
                    .withValue(CommonDataKinds.StructuredPostal.STREET, address.street)
                    .withValue(CommonDataKinds.StructuredPostal.CITY, address.city)
                    .withValue(CommonDataKinds.StructuredPostal.REGION, address.region)
                    .withValue(CommonDataKinds.StructuredPostal.POSTCODE, address.postcode)
                    .withValue(CommonDataKinds.StructuredPostal.COUNTRY, address.country);
            ops.add(op.build());
        }

        // Birthday
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                .withValue(CommonDataKinds.Event.TYPE, CommonDataKinds.Event.TYPE_BIRTHDAY)
                .withValue(CommonDataKinds.Event.START_DATE, contact.birthday);
        ops.add(op.build());

        try {
            this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}