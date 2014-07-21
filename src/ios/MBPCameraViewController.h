#import <UIKit/UIKit.h>

@interface MBPCameraViewController : UIViewController

- (id)initWithCallback:(void(^)(UIImage*))callback titleName:(NSString*)title_ logoFilename:(NSString*)logoFilename_ description:(NSString*)description_;

@end