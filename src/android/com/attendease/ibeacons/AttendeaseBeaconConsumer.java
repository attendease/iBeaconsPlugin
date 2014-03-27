package com.attendease.ibeacons;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import android.util.Log;

//https://github.com/RadiusNetworks/android-ibeacon-reference/blob/master/src/com/radiusnetworks/ibeaconreference/MonitoringActivity.java
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
//import android.app.AlertDialog;
//import android.content.DialogInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AttendeaseBeaconConsumer extends Service implements IBeaconConsumer
{
    public static final String TAG = "AttendeaseBeaconConsumer";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    public static JSONArray beaconUUIDs = new JSONArray();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AttendeaseBeaconConsumer.onCreate");
        iBeaconManager.bind(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "AttendeaseBeaconConsumer.onDestroy");
        iBeaconManager.unBind(this);
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
          @Override
          public void didEnterRegion(Region region) {
            Log.i(TAG, "I just saw an iBeacon: " + region.toString());
          }

          @Override
          public void didExitRegion(Region region) {
            Log.i(TAG, "I no longer see an iBeacon: " + region.toString() );
          }

          @Override
          public void didDetermineStateForRegion(int state, Region region) {
            if (state == 1)
            {
              Log.i(TAG, "I have just switched to SEEING iBeacons: " + region.toString());
            }
            else
            {
              Log.i(TAG, "I have just switched to NOT SEEING iBeacons: " + region.toString());
            }
          }
        });

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
          @Override
          public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
              if (iBeacons.size() > 0) {
                  Log.i(TAG, region.toString() + ": The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");
              }
          }
        });

        try {
          for (int i = 0; i < beaconUUIDs.length(); ++i) {
              try {
                String beaconUUID = beaconUUIDs.getString(i);

                Log.v(TAG, "iBeaconManager.startMonitoringBeaconsInRegion: " + beaconUUID);

                iBeaconManager.startMonitoringBeaconsInRegion(new Region(beaconUUID, beaconUUID, null, null));
                iBeaconManager.startRangingBeaconsInRegion(new Region(beaconUUID, beaconUUID, null, null));
              } catch (JSONException e) {
                Log.e(TAG, "onIBeaconServiceConnect: Got JSON Exception " + e.getMessage());
              }
          }



        } catch (RemoteException e) {   }
    }
}