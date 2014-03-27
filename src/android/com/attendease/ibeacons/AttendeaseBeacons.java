package com.attendease.ibeacons;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class AttendeaseBeacons extends CordovaPlugin
{
    public static final String TAG = "AttendeaseBeacons";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException
    {
        Log.v(TAG, "execute: data=" + data.toString());

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
            try {
              JSONObject dataObject = data.getJSONObject(0);
              this.getBeacons(dataObject, callbackContext);
              return true;
            } catch (JSONException e) {
              Log.e(TAG, action + " execute: Got JSON Exception " + e.getMessage());
              callbackContext.error(e.getMessage());
            }
        }

        if (action.equals("notifyServer"))
        {
            try {
              JSONObject dataObject = data.getJSONObject(0);
              this.notifyServer(dataObject, callbackContext);
              return true;
            } catch (JSONException e) {
              Log.e(TAG, action + " execute: Got JSON Exception " + e.getMessage());
              callbackContext.error(e.getMessage());
            }
        }

        if (action.equals("notifyServerAuthToken"))
        {
            try {
              JSONObject dataObject = data.getJSONObject(0);
              this.notifyServerAuthToken(dataObject, callbackContext);
              return true;
            } catch (JSONException e) {
              Log.e(TAG, action + " execute: Got JSON Exception " + e.getMessage());
              callbackContext.error(e.getMessage());
            }
        }

        return false;
    }

    private void monitor(JSONArray beaconUUIDs, CallbackContext callbackContext)
    {
        Log.v(TAG, "Hello monitor...");
        //callbackContext.error("This was a big failure...");
        callbackContext.success("This was a great success...");

        Intent beaconConsumer = new Intent(this.cordova.getActivity().getApplicationContext(), AttendeaseBeaconConsumer.class);

        AttendeaseBeaconConsumer.beaconUUIDs = beaconUUIDs;

        //this.cordova.getActivity().startActivity(beaconConsumer);
        this.cordova.getActivity().startService(beaconConsumer);
    }

    private void getBeacons(JSONObject dataObject, CallbackContext callbackContext)
    {
        Log.v(TAG, "Hello getBeacons...");

        //callbackContext.error("This was a big failure...");
        //callbackContext.success("This was a great success...");

        JSONObject beaconData = new JSONObject();
        //data.put("BEACON1data", "BEACON2data");

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, beaconData));
    }

    private void notifyServer(JSONObject dataObject, CallbackContext callbackContext)
    {
        Log.v(TAG, "Hello notifyServer...");
        //callbackContext.error("This was a big failure...");
        callbackContext.success("This was a great success...");
    }

    private void notifyServerAuthToken(JSONObject dataObject, CallbackContext callbackContext)
    {
        Log.v(TAG, "Hello notifyServerAuthToken...");
        //callbackContext.error("This was a big failure...");
        callbackContext.success("This was a great success...");
    }

}