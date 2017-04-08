//
/**
 * this project based on ReactNativeAutoUpdater
 */
//

#import <Foundation/Foundation.h>
#import "RCTRootView.h"


@interface DoctorStrangeUpdater : NSObject

/**
 *  Returns the singleton instance of DoctorStrangeUpdater
 *
 *  @return instance of DoctorStrangeUpdater
 */
+ (id)sharedInstance;

/**
 *  Initializes the singleton instance with the metadata URL and default JS code location
 *
 *  @param defaultJSCodeLocation        default JS code location in case there are no updates
 *  @param defaultMetadataFileLocation  location of the metadata file that described the JS code shipped with the app
 */
- (void)initializeWithUpdateMetadataUrl:(NSURL*)defaultJSCodeLocation defaultMetadataFileLocation:(NSURL*)metadataFileLocation;

/**
 *  Returns the location of the latest JS code that has been downloaded so far.
 *
 *  @return NSURL object of the JS code location
 */
- (NSURL*)latestJSCodeLocation;


- (BOOL)bsdiffPatch:(NSString *)patchPath
             origin:(NSString *)origin
        destination:(NSString *)destination;

@end
