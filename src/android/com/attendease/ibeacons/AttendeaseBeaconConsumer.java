package com.attendease.ibeacons;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import java.util.Collection;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;


//https://github.com/RadiusNetworks/android-ibeacon-reference/blob/master/src/com/radiusnetworks/ibeaconreference/MonitoringActivity.java
import android.util.Log;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.content.LocalBroadcastManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.adobe.mobile.*;


public class AttendeaseBeaconConsumer extends Service implements IBeaconConsumer
{
    public static final String TAG = "AttendeaseBeaconConsumer";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    private JSONArray beaconUUIDs = new JSONArray();

    private static Hashtable beacons = new Hashtable();

    private static Hashtable beaconNotifications = new Hashtable();

    private static String notificationServer = "";
    private static Integer notificationInterval = 3600;
    private static String authToken = "";
    private static String attendeeName = "";
    private static String attendeeId = "";

    public static Hashtable getBeacons() {
      return beacons;
    }

    public static void setNotifyServer(String server, Integer interval) {
      Log.i(TAG, "AttendeaseBeaconConsumer.setNotifyServer");
      notificationServer = server;
      notificationInterval = interval;
    }

    public static void setNotifyServerAuthToken(String theAuthToken) {
      Log.i(TAG, "AttendeaseBeaconConsumer.setNotifyServerAuthToken");
      authToken = theAuthToken;
    }

    public static void setTheAttendeeName(String theAttendeeName) {
      Log.i(TAG, "AttendeaseBeaconConsumer.setTheAttendeeName");
      attendeeName = theAttendeeName;
    }

    public static void setTheAttendeeId(String theAttendeeId) {
      Log.i(TAG, "AttendeaseBeaconConsumer.setTheAttendeeId");
      attendeeId = theAttendeeId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            beaconUUIDs = new JSONArray(intent.getStringExtra("beaconUUIDs"));
        } catch (JSONException e) {
            Log.e(TAG, "onStartCommand: Got JSON Exception " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand: Got an Exception " + e.getMessage());
            e.printStackTrace();
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
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

        final Context thus = this;

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
          @Override
          public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
              if (iBeacons.size() > 0) {
                  Iterator<IBeacon> iterator = iBeacons.iterator();

                  Vector data = new Vector();

                  while (iterator.hasNext())
                  {
                      IBeacon beacon = iterator.next();
                      Log.i(TAG, region.getProximityUuid() + ": The iBeacon I see is about " + beacon.getAccuracy() + " meters away.");
                      data.addElement(beacon);

                      String identifier = beacon.getProximityUuid() + "," + beacon.getMajor() + "," + beacon.getMinor();

                      // Only notify the server/app if the beacon is near or in yo' face!
                      // Added CLProximityFar because walking into a room with the phone in your pocket seems to trigger this one first... and doesn't retrigger as you get closer.
                      if (beacon.getProximity() == IBeacon.PROXIMITY_FAR || beacon.getProximity() == IBeacon.PROXIMITY_NEAR || beacon.getProximity() == IBeacon.PROXIMITY_IMMEDIATE)
                      {

                          Date previousTime = (Date) beaconNotifications.get(identifier);

                          Boolean notify = true;

                          if (previousTime != null)
                          {
                              Date currentTime = new Date();


                              long seconds = (currentTime.getTime()-previousTime.getTime())/1000;

                              Log.v(TAG, "Seconds since last notified --------> " + seconds);

                              if (seconds < 60 || seconds < notificationInterval)
                              {
                                  notify = false;
                              }
                          }

                          if (notify)
                          {
                              Log.v(TAG, "NOTIFY about this beacon: " + identifier);

                              if (notificationServer != "" && authToken != "")
                              {
                                  beaconNotifications.put(identifier, new Date());
                              }

                              if (attendeeName != "" && attendeeId != "")
                              {
                                  // Build the input dictionary for Adobe Target
                                  Map<String, Object> targetParameters = new HashMap<String, Object>();
                                  targetParameters.put("uuid", beacon.getProximityUuid());
                                  targetParameters.put("major", Integer.toString(beacon.getMajor()));
                                  targetParameters.put("minor", Integer.toString(beacon.getMinor()));
                                  targetParameters.put("first_name", attendeeName);
                                  targetParameters.put("attendee_id", attendeeId);

                                  Log.v(TAG, "NOTIFY ADOBE about this beacon: " + identifier);

                                  //Analytics.BEACON_PROXIMITY proximity = beacon.getProximity();
                                  Analytics.trackBeacon(beacon.getProximityUuid(), Integer.toString(beacon.getMajor()), Integer.toString(beacon.getMinor()), Analytics.BEACON_PROXIMITY.PROXIMITY_NEAR, targetParameters);

                                  TargetLocationRequest beaconRequest1 = Target.createRequest("beaconMbox1", "abort", targetParameters);
                                  TargetLocationRequest beaconRequest2 = Target.createRequest("beaconMbox2", "abort", targetParameters);
                                  TargetLocationRequest beaconRequest3 = Target.createRequest("beaconMbox3", "abort", targetParameters);
                                  TargetLocationRequest beaconRequest4 = Target.createRequest("beaconMbox4", "abort", targetParameters);
                                  TargetLocationRequest beaconRequest5 = Target.createRequest("beaconMbox5", "abort", targetParameters);

                                  Target.loadRequest(beaconRequest1, new Target.TargetCallback<String>() {
                                      @Override
                                      public void call(String item) {
                                        targetResponse(item, thus);
                                      }
                                  });

                                  Target.loadRequest(beaconRequest2, new Target.TargetCallback<String>() {
                                      @Override
                                      public void call(String item) {
                                        targetResponse(item, thus);
                                      }
                                  });

                                  Target.loadRequest(beaconRequest3, new Target.TargetCallback<String>() {
                                      @Override
                                      public void call(String item) {
                                        targetResponse(item, thus);
                                      }
                                  });

                                  Target.loadRequest(beaconRequest4, new Target.TargetCallback<String>() {
                                      @Override
                                      public void call(String item) {
                                        targetResponse(item, thus);
                                      }
                                  });

                                  Target.loadRequest(beaconRequest5, new Target.TargetCallback<String>() {
                                      @Override
                                      public void call(String item) {
                                        targetResponse(item, thus);
                                      }
                                  });

                              }
                          }

                      }
                  }

                  beacons.put(region.getProximityUuid(), data);
              }
              else
              {
                beacons.put(region.getProximityUuid(), new Vector());
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

    public void targetResponse(String item, Context thus) {
      Log.v(TAG, "Adobe Target says: " + item);

      // TODO: If we already got a unique message from target don't show it again... (Like the iOS version)

      if (item != "abort")
      {
        try {
          JSONObject targetData = new JSONObject(item);


          Intent intent = new Intent(thus, AttendeaseBeaconAlertActivity.class); //this, "com.attendease.ibeacons.AttendeaseBeaconAlertService");
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          // You can also include some extra data.
          intent.putExtra("package", thus.getPackageName());
          intent.putExtra("title", targetData.get("title").toString());
          intent.putExtra("message", targetData.get("body").toString());
          startActivity(intent);

          NotificationCompat.Builder builder =
                  new NotificationCompat.Builder(thus)
                          .setSmallIcon(getIconValue(thus.getPackageName(), "icon"))
                          .setContentTitle(targetData.get("title").toString())
                          .setContentText(targetData.get("body").toString());
          int NOTIFICATION_ID = 424242;

          Intent targetIntent = new Intent(thus, AttendeaseBeacons.class);
          PendingIntent contentIntent = PendingIntent.getActivity(thus, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
          builder.setContentIntent(contentIntent);
          NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          nManager.notify(NOTIFICATION_ID, builder.build());

        } catch (JSONException e) {
          Log.e(TAG, "Target.loadRequest: Got JSON Exception " + e.getMessage());
        }
      }
    }

    /**
     * https://github.com/katzer/cordova-plugin-local-notifications/blob/master/src/android/Options.java
     * Returns numerical icon Value
     *
     * @param {String} className
     * @param {String} iconName
     */
    private int getIconValue (String className, String iconName) {
        int icon = 0;

        try {
            Class<?> klass  = Class.forName(className + ".R$drawable");

            icon = (Integer) klass.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception e) {}

        return icon;
    }
}