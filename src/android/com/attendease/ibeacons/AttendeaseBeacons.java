package com.attendease.ibeacons;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import java.util.Collection;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;

import com.radiusnetworks.ibeacon.IBeacon;

public class AttendeaseBeacons extends CordovaPlugin
{
    public static final String TAG = "AttendeaseBeacons";
    public Intent beaconConsumer;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        super.initialize(cordova, webView);
        beaconConsumer = new Intent(this.cordova.getActivity().getApplicationContext(), AttendeaseBeaconConsumer.class);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException
    {
        //Log.v(TAG, "execute: data=" + data.toString());

        if (action.equals("monitor"))
        {
            try {
              JSONArray beaconUUIDs = data.getJSONArray(0);
              this.monitor(beaconUUIDs, callbackContext);
              return true;
            } catch (JSONException e) {
              Log.e(TAG, action + " execute: Got JSON Exception " + e.getMessage());
              callbackContext.error(e.getMessage());
            }
        }

        if (action.equals("getBeacons"))
        {
            this.getBeacons(callbackContext);
            return true;
        }

        if (action.equals("notifyServer"))
        {
            this.notifyServer(data.getString(0), data.getInt(1), callbackContext);
            return true;
        }

        if (action.equals("notifyServerAuthToken"))
        {
            this.notifyServerAuthToken(data.getString(0), callbackContext);
            return true;
        }

        return false;
    }

    private void monitor(JSONArray beaconUUIDs, CallbackContext callbackContext)
    {
        beaconConsumer.putExtra("beaconUUIDs", beaconUUIDs.toString());

        this.cordova.getActivity().startService(beaconConsumer);

        callbackContext.success("This was a great success...");
    }

    private void getBeacons(CallbackContext callbackContext)
    {
        JSONArray beaconArray = new JSONArray();

        // Can't we get the data from the instance?
        //Bundle extras = beaconConsumer.getExtras();

        try {
            //Hashtable<String, Vector> beacons = (Hashtable) extras.get("beacons");

            // get beacons from static method
            Hashtable<String, Vector> beacons = AttendeaseBeaconConsumer.getBeacons();

            Enumeration<String> enumKey = beacons.keys();
            while(enumKey.hasMoreElements()) {
                String key = enumKey.nextElement();
                Vector data = beacons.get(key);

                Iterator i = data.iterator();
                while (i.hasNext()) {
                  IBeacon beacon = (IBeacon) i.next();
                  Log.v(TAG, "Hello getBeacons... " + key + " : " + beacon.toString());

                  String proximity = "Unknown";

                  if (beacon.getProximity() == IBeacon.PROXIMITY_FAR)
                  {
                    proximity = "Far";
                  }
                  else if (beacon.getProximity() == IBeacon.PROXIMITY_NEAR)
                  {
                    proximity = "Near";
                  }
                  else if (beacon.getProximity() == IBeacon.PROXIMITY_IMMEDIATE)
                  {
                    proximity = "Immediate";
                  }

                  JSONObject beaconData  = new JSONObject();
                  beaconData.put("uuid", beacon.getProximityUuid());
                  beaconData.put("major", beacon.getMajor());
                  beaconData.put("minor", beacon.getMinor());
                  beaconData.put("proximityString", proximity);
                  beaconData.put("accuracy", beacon.getAccuracy());

                  beaconArray.put(beaconData);
                }
            }

            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, beaconArray));
        } catch (Exception e) {
          Log.e(TAG, "getBeacons: Got Exception " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
    }

    private void notifyServer(String server, Integer interval, CallbackContext callbackContext)
    {
        AttendeaseBeaconConsumer.setNotifyServer(server, interval);

        callbackContext.success("This was a great success...");
    }

    private void notifyServerAuthToken(String authToken, CallbackContext callbackContext)
    {
        Log.v(TAG, "Hello notifyServerAuthToken... " + authToken);

        AttendeaseBeaconConsumer.setNotifyServerAuthToken(authToken);

        //callbackContext.error("This was a big failure...");
        callbackContext.success("This was a great success...");
    }

}