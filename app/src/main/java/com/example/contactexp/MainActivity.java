package com.example.contactexp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Account[] accounts = accountManager.getAccounts();
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

                addContact(getSampleContact("00 New Info"));

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

//        printAllIds(cursor);

//        Map<String, String> idAccountMap = getAllRawIdsWithAccountType(cursor);
    }

    public Map<String, String> getAllRawIdsWithAccountType(Cursor contactCursor) {
        Log.d(TAG, "Total contacts : " + contactCursor.getCount());
        Map<String, String> dataMap = new HashMap<>();
        ArrayList<String> otherContactList = new ArrayList<>();
        ArrayList<String> k2mContactList = new ArrayList<>();
        if (contactCursor != null && contactCursor.getCount() > 0) {

            while (contactCursor.moveToNext()) {
                String contactID = contactCursor.getString(contactCursor.getColumnIndex(_ID));
                String nameRawContactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));

                String selection = ContactsContract.RawContacts.CONTACT_ID + "= ?";
                ArrayList<String> selectionArgs = new ArrayList();
                selectionArgs.add(contactID);
                final String[] projection = null;

                Cursor cursor = this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection,
                        selectionArgs.toArray(new String[selectionArgs.size()]), null);

                boolean isK2MContact = false;
                while (cursor != null && cursor.moveToNext()) {
//                    String rawId = cursor.getString(cursor.getColumnIndex(_ID));
                    String accountType = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    if (accountType != null && accountType.equals("com.known2me")) {
                        isK2MContact = true;
                    }
                }

                if (isK2MContact) {
                    k2mContactList.add(contactID);
                } else {
                    otherContactList.add(contactID);
                }

                if (!cursor.isClosed()) cursor.close();

            }

//            if (!contactCursor.isClosed()) contactCursor.close();
        }

        // add new raw for other contacts with k2m account type
        addNewRawForContact(otherContactList);

        Log.d(TAG, "K2M contacts : " + k2mContactList.size());
        Log.d(TAG, "Other contacts : " + otherContactList.size());


        return dataMap;
    }

    private void addNewRawForContact(ArrayList<String> otherContactList) {
//        1329
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentProviderOperation.Builder op = ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.CONTACT_ID, "1329")
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.known2me")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "Known2Me");
        ops.add(op.build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }
    }

    private void printAllIds(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String contactID = cursor.getString(cursor.getColumnIndex(_ID));
                String nameRawContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                printRawInfo(contactID, nameRawContactId, displayName);
            }
        }
    }

    private void printRawInfo(String contactID, String nameRawContactId, String displayName) {
        String selection = ContactsContract.RawContacts.CONTACT_ID + "= ?";
        ArrayList<String> selectionArgs = new ArrayList();
        selectionArgs.add(contactID);
        final String[] projection = null;

        Cursor cursor = this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection,
                selectionArgs.toArray(new String[selectionArgs.size()]), null);

//        ArrayList<String> rawIds = new ArrayList<>();

        while (cursor != null && cursor.moveToNext()) {
//            rawIds.add(cursor.getString(cursor.getColumnIndex(_ID)));
            String rawId = cursor.getString(cursor.getColumnIndex(_ID));
            String accountType = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
            printDataInfo(contactID, nameRawContactId, displayName, rawId, accountType);
        }


        if (!cursor.isClosed()) cursor.close();
    }

    private void printDataInfo(String contactID, String nameRawContactId, String displayName, String rawId, String accountType) {
        String selection = ContactsContract.Data.RAW_CONTACT_ID + "= ?";
        ArrayList<String> selectionArgs = new ArrayList();
        selectionArgs.add(rawId);
        final String[] projection = null;

        Cursor cursor = this.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection,
                selectionArgs.toArray(new String[selectionArgs.size()]), null);

        String fullInfo =
                "Contact._Id" + "\t" + "Contact.name_raw_contact_id" + "\t" + "RawContacts._Id" + "\t" + "Data._Id" + "\t" + "accountType" +
                        "\t" + "displayName" + "\n";
        while (cursor != null && cursor.moveToNext()) {
            String dataId = cursor.getString(cursor.getColumnIndex(_ID));
//            System.out.println(contactID + "\t" + nameRawContactId + "\t" + rawId + "\t" + dataId + "\t" + displayName + "\t" + accountType);
            fullInfo += contactID + "\t" + nameRawContactId + "\t" + rawId + "\t" + dataId + "\t" + accountType + "\t" + displayName + "\n";

        }

        writeToFile(fullInfo);


        if (!cursor.isClosed()) cursor.close();
    }

    private void writeToFile(String data) {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, "/contact_info.txt");
//        if(!file.exists()){
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, true);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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
//1939r5499-1313192C2A48524246502A.3789r6004-1313192C2A48524246502A.3458i637bff7e0b71832b
//1939r5499-1313192C2A48524246502A.3458i637bff7e0b71832b.3789r7235-1313192C2A48524246502A

    /*private void updateContact(Contact contact){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentProviderOperation.Builder op = null;

        if (contact.identifier == null || contact.identifier.isEmpty()) {
            return ;
        }

        String rawContactId = getRawContactId(contact.identifier);

        Log.e(this.getClass().getSimpleName(), "rawContactId : " + rawContactId);

        if (rawContactId == null || rawContactId.isEmpty()) {
            Log.e(this.getClass().getSimpleName(), "Raw id is null for " + contact.identifier);
            return ;
        }

        // fetch current contact

        List<String> identifiers = new ArrayList<>();
        identifiers.add(contact.identifier);
        Cursor cursor = getCursorForContactIdentifiers(identifiers, true);

        String structureNameId = null;
        String organizationId = null;
        String nicknameId = null;
        String sipId = null;
        String noteId = null;
        Map<String, String> existingLabelIdMap = new HashMap<>();
        String birthdayId = null;
        while (cursor != null && cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

            if (mimeType.equals(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                structureNameId = id;
            } else if (mimeType.equals(CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
                organizationId = id;
            } else if (mimeType.equals(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)) {
                nicknameId = id;
            } else if (mimeType.equals(CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)) {
                sipId = id;
            } else if (mimeType.equals(CommonDataKinds.Note.CONTENT_ITEM_TYPE)) {
                noteId = id;
            } else if (mimeType.equals(CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)) {
                String groupId = cursor.getString(cursor.getColumnIndex(CommonDataKinds.GroupMembership.DATA1));
                existingLabelIdMap.put(groupId, id);
            } else if (mimeType.equals(CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
                int eventType = cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Event.TYPE));
                if (eventType == CommonDataKinds.Event.TYPE_BIRTHDAY) {
                    birthdayId = id;
                }
            }
        }

        ArrayList<Contact> contactList = getContactsFrom(cursor);

        if (contactList.size() == 0) {
            return ;
        }

        Contact currentContact = contactList.get(0);

        String queryCommon = BaseColumns._ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?";
        if (structureNameId == null) {
            Log.e(this.getClass().getSimpleName(), "Inserting structure name");
            // insert
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            op.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            op.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        } else {
            Log.e(this.getClass().getSimpleName(), "Updating structure id :" + structureNameId);
            // update
            if (equalsStructureName(contact, currentContact)) {
                op = null;
            } else {
                op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                String[] queryArg = new String[]{structureNameId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
                op.withSelection(queryCommon, queryArg);
            }
        }

        if (op != null) {
            // Update data (name)
            op.withValue(StructuredName.GIVEN_NAME, contact.givenName)
                    .withValue(StructuredName.MIDDLE_NAME, contact.middleName)
                    .withValue(StructuredName.FAMILY_NAME, contact.familyName)
                    .withValue(StructuredName.PREFIX, contact.prefix)
                    .withValue(StructuredName.SUFFIX, contact.suffix)
                    .withValue(StructuredName.PHONETIC_GIVEN_NAME, contact.phoneticGivenName)
                    .withValue(StructuredName.PHONETIC_MIDDLE_NAME, contact.phoneticMiddleName)
                    .withValue(StructuredName.PHONETIC_FAMILY_NAME, contact.phoneticFamilyName);
            ops.add(op.build());
        }
    }*/

    private Contact getSampleContact(String name) {
        Contact contact = new Contact(null);
        contact.givenName = name;
        contact.familyName = "Mono";
        contact.nickname = "Motorola";
        contact.company = "IPL";

        ArrayList<Item> emailList = new ArrayList<>();
        emailList.add(new Item(null, "Work", "rohit@bcci.com"));
        emailList.add(new Item(null, "Personal", "sharma@gmail.com"));
        contact.emails = emailList;

        ArrayList<Item> phoneList = new ArrayList<>();
        phoneList.add(new Item(null, "Work", "+91123456789"));
        phoneList.add(new Item(null, "Personal", "00987654321"));
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

    public void addK2MContactInfo(String contactId) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.known2me")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "Known2Me");
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, "1111")
//                .withValue(StructuredName.MIDDLE_NAME, contact.middleName)
                .withValue(StructuredName.FAMILY_NAME, "One");
//                .withValue(StructuredName.PREFIX, contact.prefix)
//                .withValue(StructuredName.SUFFIX, contact.suffix);
        ops.add(op.build());

        try {
            ContentProviderResult[] results = this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//            long contactId = 0;
            final String[] projection = new String[]{ContactsContract.RawContacts.CONTACT_ID};
            final Cursor cursor = getContentResolver().query(results[0].uri, null, null, null, null);

            String[] columnNames = cursor.getColumnNames();
            String message = "\n";

            String rawId = null;

            while (cursor != null && cursor.moveToNext()) {
                rawId = cursor.getString(cursor.getColumnIndex(_ID));
                message = "\n========New RAW=======\n";
                for (String columnName : columnNames) {
                    if (cursor.getType(cursor.getColumnIndex(columnName)) != FIELD_TYPE_NULL && cursor.getType(cursor.getColumnIndex(columnName)) != FIELD_TYPE_BLOB) {
                        String value = cursor.getString(cursor.getColumnIndex(columnName));
                        message += columnName + " = " + value + "\n";

                    }
                }
                Log.d(TAG, message);
            }

            if (rawId != null) {
                joinIntoExistingContact(Long.parseLong(contactId), Long.parseLong(rawId));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinIntoExistingContact(long existingContactId, long newRawContactId) {

        // get all existing raw-contact-ids that belong to the contact-id
        List<Long> existingRawIds = new ArrayList<>();
        Cursor cur = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts._ID}, ContactsContract.RawContacts.CONTACT_ID + "=" + existingContactId, null, null);
        while (cur.moveToNext()) {
            existingRawIds.add(cur.getLong(0));
        }
        cur.close();
        Log.i("Join", "Found " + existingRawIds.size() + " raw-contact-ids");

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // go over all existing raw ids, and join with our new one
        for (Long existingRawId : existingRawIds) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI);
            builder.withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER);
            builder.withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, newRawContactId);
            builder.withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, existingRawId);
            ops.add(builder.build());
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public void openForEdit(String contactId, String lookup){
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        /*
         * Sets the contact URI to edit, and the data type that the
         * Intent must match
         */
        Uri selectedContactUri = ContactsContract.Contacts.getLookupUri(Long.parseLong(contactId), lookup);
        editIntent.setDataAndType(selectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        // Sets the special extended data for navigation
        editIntent.putExtra("finishActivityOnSaveCompleted", true);
        // Sends the Intent
        startActivityForResult(editIntent,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG,"onActivityResult "+requestCode);
    }
}