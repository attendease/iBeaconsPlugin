#import "AttendeaseBeacons.h"
#import "Cordova/CDV.h"
#import <objc/runtime.h>

static char launchNotificationKey;

@interface AttendeaseBeacons (Private)

- (void) didReceiveLocalNotification:(NSNotification*)localNotification;

@end

@implementation AttendeaseBeacons

#pragma mark - Plugin methods

- (AttendeaseBeacons*)pluginInitialize
{
    // Initialize location manager and set ourselves as the delegate
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    self.locationManager.delegate = self;

    self.beacons = [[NSMutableDictionary alloc] init];

    self.beaconNotifications = [[NSMutableDictionary alloc] init];

    self.notificationServer = @"";

    self.authToken = @"";

    self.notificationInterval = [NSNumber numberWithInteger: 3600];

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didReceiveLocalNotification:) name:CDVLocalNotification object:nil];


    return self;
}

- (void) monitor:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    // Check if beacon monitoring is available for this device
    if (![CLLocationManager isMonitoringAvailableForClass:[CLBeaconRegion class]])
    {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Monitoring not available"];
    }
    else
    {
        //NSLog(@"monitoring...");

        NSArray  *uuidsToMonitor = [command.arguments objectAtIndex:0];

        if (uuidsToMonitor != nil)
        {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        else
        {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"UUIDs array to monitor was null"];
        }

        // Initialize and monitor regions
        for (NSString *serviceUUID in uuidsToMonitor)
        {
            // Initialize region
            NSUUID *serviceUUIDObject = [[NSUUID alloc] initWithUUIDString:serviceUUID];

            //NSNumber *major = 42;
            //NSNumber *minor = 420;

            //NSString *identifier = [NSString stringWithFormat:@"Attendease Beacon: %@,%@,%@", serviceUUID, [major stringValue], [minor stringValue]];

            NSString *identifier = [NSString stringWithFormat:@"Attendease Beacon: %@", serviceUUID];

            // Create the beacon region to be monitored.
            // The identifier needs to be unique for each region
            CLBeaconRegion *beaconRegion = [[CLBeaconRegion alloc]
                                            initWithProximityUUID: serviceUUIDObject
                                            //major: major
                                            //minor: minor
                                            identifier: identifier];

            // Specify notifications
            beaconRegion.notifyEntryStateOnDisplay = YES;
            beaconRegion.notifyOnEntry = YES;
            beaconRegion.notifyOnExit = YES;

            // Begin monitoring region and ranging beacons
            [self.locationManager startMonitoringForRegion:beaconRegion];
            [self.locationManager startRangingBeaconsInRegion:beaconRegion];
        }
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)getBeacons:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSMutableArray* output = [NSMutableArray array];

        for (NSString *regionBeaconUUID in self.beacons)
        {
            NSArray *regionBeacons = [self.beacons objectForKey:regionBeaconUUID];

            if ([regionBeacons count] > 0)
            {
                for (CLBeacon *beacon in regionBeacons)
                {
                    [output addObject:[self beaconToDictionary:beacon]];
                }
            }
        }

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:output];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}


- (void)notifyServer:(CDVInvokedUrlCommand*)command
{
    self.notificationServer = [command.arguments objectAtIndex:0];
    self.notificationInterval =  [NSNumber numberWithInteger: [[command.arguments objectAtIndex:1] intValue]];

    //NSLog(@"Setting Server --------> %@", self.notificationServer);
    //NSLog(@"Setting Interval --------> %i", [self.notificationInterval intValue]);

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)notifyServerAuthToken:(CDVInvokedUrlCommand*)command
{
    self.authToken = [command.arguments objectAtIndex:0];

    //NSLog(@"Setting Auth Token --------> %@", self.authToken);

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


#pragma mark - locationManager biz

- (void) locationManager:(CLLocationManager *)manager didDetermineState:(CLRegionState)state forRegion:(CLRegion *)region
{
    /*
     if(state == CLRegionStateInside) {
     NSLog(@"locationManager didDetermineState INSIDE for %@", region.identifier);
     }
     else if(state == CLRegionStateOutside) {
     NSLog(@"locationManager didDetermineState OUTSIDE for %@", region.identifier);
     }
     else {
     NSLog(@"locationManager didDetermineState OTHER for %@", region.identifier);
     }
     */
}

- (void) locationManager:(CLLocationManager*)manager didEnterRegion:(CLRegion *)region
{
    //NSLog(@"You've entered the region of an active beacon...");
    //[self.locationManager startRangingBeaconsInRegion:self.theBeaconRegion];
}


-(void) locationManager:(CLLocationManager*)manager didExitRegion:(CLRegion *)region
{
    //NSLog(@"You are not in range of an active beacon.");
}

-(void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region
{
    //NSLog(@"didStartMonitoringForRegion!");
}


-(void) locationManager:(CLLocationManager*)manager
        didRangeBeacons:(NSArray*)beacons
               inRegion:(CLBeaconRegion*)region
{
    // For background beacon tracking check this out: http://developer.radiusnetworks.com/2013/11/13/ibeacon-monitoring-in-the-background-and-foreground.html

    //NSLog([@"didRangeBeacons: " stringByAppendingString:region.proximityUUID.UUIDString]);

    NSMutableArray* output = [NSMutableArray array];

    if([beacons count] > 0)
    {
        //convert list of beacons to a an array of simple property-value objects
        for (CLBeacon *beacon in beacons)
        {
            //NSLog(@"Adding beacon.");
            [output addObject:beacon];
        }
    }
    else
    {
        //NSLog(@"No beacons...");
    }

    [self.beacons setObject:output forKey:region.proximityUUID.UUIDString];

    for (CLBeacon *beacon in output)
    {
        // Only notify the server/app if the beacon is near or in yo' face!
        // Added CLProximityFar because walking into a room with the phone in your pocket seems to trigger this one first... and doesn't retrigger as you get closer.
        if (beacon.proximity == CLProximityFar || beacon.proximity == CLProximityNear || beacon.proximity == CLProximityImmediate)
        {
            NSString *identifier = [NSString stringWithFormat:@"%@,%@,%@", beacon.proximityUUID.UUIDString, beacon.major.stringValue, beacon.minor.stringValue];

            /*
            if (beacon.proximity == CLProximityFar) {
                NSLog([@"The beacon is far far away: " stringByAppendingString:identifier]);
            }
            else if (beacon.proximity == CLProximityNear) {
                NSLog([@"The beacon is very very close: " stringByAppendingString:identifier]);
            }
            else if (beacon.proximity == CLProximityImmediate) {
                NSLog([@"The beacon is very very, seriously very close: " stringByAppendingString:identifier]);
            }
            */

            NSDate *previousTime = [self.beaconNotifications objectForKey:identifier];

            BOOL notify = YES;

            if (previousTime != NULL)
            {
                NSDate *currentTime = [NSDate date];

                NSTimeInterval seconds = [currentTime timeIntervalSinceDate:previousTime];

                /*
                NSString *dateString = [NSDateFormatter localizedStringFromDate:previousTime
                                                                      dateStyle:NSDateFormatterShortStyle
                                                                      timeStyle:NSDateFormatterFullStyle];
                NSLog(@"PREVIOUS TIME: %@", dateString);

                NSLog(@"%@ - Seconds since last notified --------> %f", identifier, seconds);
                */

                // Beacons are ranged every second when app is in the foreground.
                // Don't notify every time they are ranged.
                if (([self.notificationInterval intValue] < 60) || (seconds < [self.notificationInterval intValue]))
                {
                    notify = NO;
                }
            }

            if (notify)
            {
                NSString* title = @"You found a beacon!";
                NSString* message = @"Have a nice day.";

                if (UIApplicationStateActive == [[UIApplication sharedApplication] applicationState])
                {
                    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:title message:message delegate:nil cancelButtonTitle:@"Close" otherButtonTitles:nil];
                    [alertView show];

                    /*
                     // Send data to cordova and show a modal there. Can this be done?
                     NSString* json = [NSString stringWithFormat:@"{\"title\":\"%@\",\"message\":\"%@\"}", title, message];

                     [self fireEvent:@"notification" id:@"What is this id" json:json];
                     */

                }
                else if (UIApplicationStateBackground == [[UIApplication sharedApplication] applicationState])
                {
                    UILocalNotification *localNotification = [[UILocalNotification alloc] init];
                    [localNotification setSoundName:UILocalNotificationDefaultSoundName];

                    [localNotification setAlertBody:title];


                    NSDictionary *params = @{
                                             @"message" : message,
                                             @"title" : title
                                             };

                    [localNotification setUserInfo:params];

                    [[UIApplication sharedApplication] presentLocalNotificationNow:localNotification];
                }

                // Send a notification to our server... because we are big brother and need to know everything you are doing! Just kidding!
                if (![self.notificationServer isEqual: @""] && ![self.authToken isEqual: @""] )
                {
                    NSDate *currentTime = [NSDate date];

                    [self.beaconNotifications setObject:currentTime forKey:identifier];

                    //NSLog(@"Server --------> %@", self.notificationServer);
                    //NSLog(@"Interval --------> %i", [self.notificationInterval intValue]);
                    //NSLog(@"Token --------> %@", self.authToken);

                    NSString *jsonRequest = [NSString stringWithFormat:@"{\"beacon\":\"%@\"}",identifier];

                    //NSLog(@"Request: %@", jsonRequest);

                    NSURL *url = [NSURL URLWithString:self.notificationServer];

                    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
                    NSData *requestData = [NSData dataWithBytes:[jsonRequest UTF8String] length:[jsonRequest length]];

                    [request setHTTPMethod:@"POST"];
                    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
                    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
                    [request setValue:self.authToken forHTTPHeaderField:@"X-Attendee-Token"];
                    [request setValue:[NSString stringWithFormat:@"%d", [requestData length]] forHTTPHeaderField:@"Content-Length"];
                    [request setHTTPBody: requestData];

                    NSURLConnection *connection = [NSURLConnection connectionWithRequest:request delegate:self];
                }
            }
        }

    }
}


#pragma mark - Helpers

- (NSDictionary*)beaconToDictionary:(CLBeacon*)beacon
{
    NSString *proximity;

    switch (beacon.proximity)
    {
        case CLProximityImmediate:
            proximity = @"Immediate";
            break;
        case CLProximityNear:
            proximity = @"Near";
            break;
        case CLProximityFar:
            proximity = @"Far";
            break;
        default:
            proximity = @"Unknown Proximity";
            break;
    }



    NSDictionary *targetParameters = @{
                                       @"uuid" : (NSString *) beacon.proximityUUID.UUIDString,
                                       @"major" : (NSNumber *) beacon.major,
                                       @"minor" : (NSNumber *) beacon.minor,
                                       @"rsi" : (NSNumber *) [NSNumber numberWithInt:beacon.rssi],
                                       @"proximity" : (NSNumber *) [NSNumber numberWithInt:beacon.proximity],
                                       @"proximityString" : (NSString *) proximity,
                                       @"accuracy" : (NSNumber *) [NSNumber numberWithDouble:beacon.accuracy]
                                       };

    return targetParameters;
}

- (void) didReceiveLocalNotification:(NSNotification*)localNotification
{
    //NSLog(@"didReceiveLocalNotification");
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    bool isActive            = state == UIApplicationStateActive;
    NSString* stateName      = isActive ? @"foreground" : @"background";

    UILocalNotification* notification = [localNotification object];
    NSString* message = [notification.userInfo objectForKey:@"message"];
    NSString* title = [notification.userInfo objectForKey:@"title"];

    NSString* json = [NSString stringWithFormat:@"{\"title\":\"%@\",\"message\":\"%@\"'}", title, message];

    /*
     NSDate* now                       = [NSDate date];
     NSTimeInterval fireDateDistance   = [now timeIntervalSinceDate:notification.fireDate];
     NSString* event                   = (fireDateDistance < 0.05) ? @"trigger" : @"click";
     */

    if ([stateName isEqual:@"background"])
    {
        // display alertView because fireEvent to show modal doesn't seem to work coming from the background. Ideas?
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:title message:message delegate:nil cancelButtonTitle:@"Close" otherButtonTitles:nil];
        [alertView show];

        //NSLog(@"Background... SAVE in mutable array for resume event...");

        //save it for later
        self.launchNotification = notification.userInfo;
    }
    else
    {
        // show a modal via javascript?
        // [self fireEvent:@"notification" id:@"What is this id" json:json];
    }
}

/**
 * Fires the given event.
 *
 * @param {String} event The Name of the event
 * @param {String} id    The ID of the notification
 * @param {String} json  A custom (JSON) string
 */
- (void) fireEvent:(NSString*) event id:(NSString*) id json:(NSString*) json
{
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    bool isActive            = state == UIApplicationStateActive;
    NSString* stateName      = isActive ? @"foreground" : @"background";

    NSString *escapedJson = [json stringByReplacingOccurrencesOfString:@"\r" withString:@""];
    escapedJson = [escapedJson stringByReplacingOccurrencesOfString:@"\n" withString:@"\\n"];

    //NSString* params = [NSString stringWithFormat:@"{\"%@\",\"%@\",\\'%@\\'}", id, stateName, json];
    NSString* js     = [NSString stringWithFormat:@"AttendeaseBeacons.fireEvent('%@',%@);", event, escapedJson];

    //NSLog(js);

    [self.commandDelegate evalJs:js];
}


// The accessors use an Associative Reference since you can't define a iVar in a category
// http://developer.apple.com/library/ios/#documentation/cocoa/conceptual/objectivec/Chapters/ocAssociativeReferences.html
- (NSMutableArray *)launchNotification
{
    return objc_getAssociatedObject(self, &launchNotificationKey);
}

- (void)setLaunchNotification:(NSDictionary *)aDictionary
{
    objc_setAssociatedObject(self, &launchNotificationKey, aDictionary, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (void)dealloc
{
    self.launchNotification = nil; // clear the association and release the object
}



@end