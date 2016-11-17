package com.mounacheikhna.contactssync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import timber.log.Timber;

public class ContactsSyncAdapter extends AbstractThreadedSyncAdapter {

    public ContactsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public ContactsSyncAdapter(Context context, boolean autoInitialize,
        boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
        ContentProviderClient contentProviderClient, SyncResult syncResult) {
        //TODO: here is where we do sync with a backend if there's one
        Timber.tag("TEST").d("onPerformSync ");
    }

}
