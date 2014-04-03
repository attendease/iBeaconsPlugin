# Cordova iBeacons Plugin

This plugin enables iBeacon discovery for your Cordova (Phonegap) application.

## Supported Platforms

Only devices that support Bluetooth 4 (Bluetooth LE, Bluetooth Smart) are supported.

* iOS 7+
* Android 4.3+ (Work in progress...)

### General TODO:

- Better handling of local notifications.
- Handle modals in the cordova app.

### Android TODO:

Better integrate the Radius Networks Android iBeacon Library:

- [Android iBeacon Library](http://developer.radiusnetworks.com/ibeacon/android/)

# Installing

Install with Cordova cli

    $ cordova plugin add https://github.com/Attendease/iBeaconsPlugin.git

# Setup for Android

Run these two commands:

    $ android update project --path platforms/android/libs/AndroidIBeaconLibrary/
    $ android update project --target 2 --path platforms/android/ --library libs/AndroidIBeaconLibrary/

Finally add the following to the `src/android/libs/AndroidIBeaconLibrary/project.properties` file:

    manifestmerger.enabled=true


# Examples

There are no examples... yet!

# API

## Methods

- [AttendeaseBeacons.monitor](#monitor)
- [AttendeaseBeacons.getBeacons](#getBeacons)
- [AttendeaseBeacons.notifyServer](#notifyServer)
- [AttendeaseBeacons.notifyServerAuthToken](#notifyServerAuthToken)

## monitor

Monitor beacon UUIDs

    AttendeaseBeacons.connect(UUIDs, callback);

## getBeacons

After you [monitor](#getBeacons) the beacons you can start getting beacons that are found.

    AttendeaseBeacons.getBeacons(callback);

The callback returns an array of beacons it has found.

## notifyServer

When the app is in the background we can't talk to the Phonegap javascript so we send a beacon POST to the specified server.

    AttendeaseBeacons.notifyServer(theServerToPostTo, interval);

The theServerToPostTo to post to is... the server to post to! The interval is so we don't flood the server with beacons. If we find a beacon we wait that many seconds to send the same beacon again.

## notifyServerAuthToken

Our server needs credentials to link the beacon to an attendee. This is here for that reason.

    AttendeaseBeacons.notifyServerAuthToken(theAuthToken);

## Amazing Example

    var uuidsToMonitor;

    AttendeaseBeacons.notifyServer(theServerToPostTo, 900); // 900 seconds

    AttendeaseBeacons.notifyServerAuthToken(theAuthToken);

    uuidsToMonitor = ["E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", "B9407F30-F5F8-466E-AFF9-25556B57FE6D"];

    AttendeaseBeacons.monitor(uuidsToMonitor, function() {
      return setInterval((function() {
        return AttendeaseBeacons.getBeacons(function(beacons) {
          if (_.isEmpty(beacons)) {
            return console.log("No beacons found.");
          } else {
            return _.each(beacons, function(beacon) {
              return console.log("" + beacon.uuid + " (" + beacon.major + ", " + beacon.minor + ") " + beacon.proximityString + " (" + beacon.accuracy + " meters)");
            });
          }
        });
      }), 3000);
    });

## Feedback

Give the code a try. If you find any issues or missing features please let us know or create a pull request.