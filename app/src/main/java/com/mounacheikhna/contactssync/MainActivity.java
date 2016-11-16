package com.mounacheikhna.contactssync;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: set contacts change here contacts_changes

        //TODO: first simple case just fetch data and add contentresolver observer

        query();
        getContentResolver().registerContentObserver(ContactsContract.Data.CONTENT_URI, true, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                query();
            }
        });
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
                Log.d("TEST", "Data retrieved : contactId = "+ contactId + " - givenName = %s" + givenName +
                        " - displayName = "+ displayName + ", phone = " + number);
            }
        }
    }
}
