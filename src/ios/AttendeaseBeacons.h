#import <CoreLocation/CoreLocation.h>
#import <Cordova/CDVPlugin.h>
#import "Foundation/Foundation.h"

@interface AttendeaseBeacons : CDVPlugin<CLLocationManagerDelegate>

@property (strong, nonatomic) CLLocationManager *locationManager;

@property NSMutableDictionary *beacons;
@property NSMutableDictionary *beaconNotifications;
@property NSString *notificationServer;
@property NSString *authToken;
@property NSNumber *notificationInterval;

@property (nonatomic, retain) NSDictionary *launchNotification;

@property (nonatomic) id delegate;

- (AttendeaseBeacons*)pluginInitialize;
- (void) monitor:(CDVInvokedUrlCommand*)command;
- (void) getBeacons:(CDVInvokedUrlCommand*)command;
- (void) notifyServer:(CDVInvokedUrlCommand*)command;
- (void) notifyServerAuthToken:(CDVInvokedUrlCommand*)command;

@end