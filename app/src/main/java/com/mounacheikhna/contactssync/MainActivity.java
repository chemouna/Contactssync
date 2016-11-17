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
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import timber.log.Timber;

public class MainActivity extends Activity {

    public static final String AUTHORITY = ContactsContract.AUTHORITY;
    public static final String ACCOUNT_TYPE = "com.mounacheikhna.contactssync";
    public static final String ACCOUNT = "com.mounacheikhna.contactssync.account";
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    //private static final long SYNC_FREQUENCY = 60 * 60;  // 15 mn (in seconds)
    private static final long SYNC_FREQUENCY = 60 * 15;  // 1 mn - temp

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
                    Timber.tag("TEST").d("onChange");
                    query();
                }
            });
    }

    public static Account CreateSyncAccount(Context context) {
        Timber.tag("TEST").d("CreateSyncAccount");
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
            .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            ContentResolver.addPeriodicSync(
                account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
        }

        if (newAccount || !setupComplete) {
            triggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
        return account;
    }

    public static void triggerRefresh() {
        Timber.tag("TEST").d("triggerRefresh ");
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
            new Account(ACCOUNT, ACCOUNT_TYPE), // Sync account
            //"com.goblob.contacts.contactssyncadapter",      // Content authority
            AUTHORITY,      // Content authority
            b);                                             // Extras
    }

    private void query() {
        Timber.tag("TEST").d("query");
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
        Timber.tag("TEST").d("checkPermissions");
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
