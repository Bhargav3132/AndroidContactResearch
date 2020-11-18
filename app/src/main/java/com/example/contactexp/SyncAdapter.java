package com.example.contactexp;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context, boolean should_auto_initialize) {
        super(context, should_auto_initialize);

        //noinspection ConstantConditions,PointlessBooleanExpression
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    Log.e("Uncaught sync exception", throwable.getMessage());
                }
            });
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult sync_result) {
        // TODO: implement sync
        Log.e("onPerformSync","onPerformSync");

    }
}