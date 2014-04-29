package com.attendease.ibeacons;

import android.util.Log;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.support.v4.content.LocalBroadcastManager;
import android.R;

public class AttendeaseBeaconAlertActivity extends Activity
{
    public static final String TAG = "AttendeaseBeaconAlertActivity";

    public BroadcastReceiver receiver;
    public AlertDialog mAlert;
    final Context thus = this;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // The activity is being created.

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(thus);
        builder.setTitle(title).setMessage(message);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                alertDialog.dismiss();
                AttendeaseBeaconAlertActivity.this.finish();
            }
        });

        alertDialog.setIcon(getIconValue(intent.getStringExtra("package"), "icon"));
        alertDialog.show();
    }

    private int getIconValue (String className, String iconName)
    {
        int icon = 0;

        try
        {
            Class<?> klass    = Class.forName(className + ".R$drawable");

            icon = (Integer) klass.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception e) {}

        return icon;
    }
}
