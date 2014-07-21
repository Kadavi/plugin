#import <Cordova/CDV.h>

@interface MBPCamera : CDVPlugin

- (void)takePicture:(CDVInvokedUrlCommand*)command;

@end