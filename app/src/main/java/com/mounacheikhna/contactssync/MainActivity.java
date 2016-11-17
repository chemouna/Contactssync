package com.mounacheikhna.contactssync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

public class MainActivity extends Activity {

    public static final String AUTHORITY = ContactsContract.AUTHORITY;
    public static final String ACCOUNT_TYPE = "com.mounacheikhna.contactssync";
    public static final String ACCOUNT = "myaccount";

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        account = CreateSyncAccount(this);

        //TODO: set contacts change here contacts_changes

        //TODO: first simple case just fetch data and add contentresolver observer

        query();
        getContentResolver().registerContentObserver(ContactsContract.Data.CONTENT_URI, true,
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    query();
                }
            });
    }

    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
            ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
            (AccountManager) context.getSystemService(
                ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
    }

    private void query() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[] {
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE
        };
        final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, projection,
            ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", null,
            ContactsContract.Data.CONTACT_ID + " ASC");

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contactId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                final String givenName = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                final String displayName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.d("TEST", "Data retrieved : contactId = "
                    + contactId
                    + " - givenName = %s"
                    + givenName
                    + " - displayName = "
                    + displayName
                    + ", phone = "
                    + number);
            }
        }
    }

    private void checkPermissions() {
        //TODO: display a message to the user explaining why these permission are needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.READ_SYNC_STATS,
                    android.Manifest.permission.WRITE_SYNC_SETTINGS
                }, 100);
            }
        }
    }
}
