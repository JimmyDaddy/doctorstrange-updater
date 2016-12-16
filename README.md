# doctorstrange-updater
This project is based on `react-native-auto-updater` which only support update `jsbundle` for RN application,

and I upgraded it to support update static resources and incremental update( just for IOS now, later I will make it support Android).

this is a client SDK, and there is a server project named [doctorstrange](https://github.com/JimmyDaddy/doctorstrange "`github`")  (or [doctorstrange](http://gitlab.songxiaocai.org/ios/doctorstrange "`gitLab`"))

### SDK config

At first you should add a json file under your project path, there is sample of it:
``` json
{
	"version": "1.6.9",
	"minContainerVersion": "2.3.4",
    "serverUrl":"doctorstrange.songxiaocai.org/update/download",
    "description": "nothing",
    "url": "doctorstrange.songxiaocai.org/update/download"
}
```
Here's what the fields in the JSON mean:

* `version` — this is the version of the bundle file (in *major.minor.patch* format)
* `minContainerVersion` — this is the minimum version of the container (native) app that is allowed to download this bundle (this is needed because adding a new React Native component to your app might result into changed native app, hence, going through the AppStore review process)
* `url` — this is where updater will download the JS bundle and asserts from
* `description` — remains for future
updater needs know the location of this JSON file upon initialization.

## Installation
1. add `    "doctorstrange-updater":"git+https://github.com/JimmyDaddy/doctorstrange-updater.git"` of `    "doctorstrange-updater": "git+git@121.40.208.242:ios/doctorstrange-updater.git"` in your rn project's `package.json`
2. In the Xcode's "Project navigator", right click on your project's Libraries folder ➜ Add Files to _"Your Project Name"_
3. Go to `node_modules` ➜ `doctorstrange-updater` ➜ `iOS` ➜ select `DoctorstrangeUpdater.xcodeproj`
4. In the Xcode Project Navigator, click the root project, and in `General` tab, look for `Linked Frameworks and Libraries`. Click on the `+` button at the bottom and add `libDoctorstrangeUpdater.a` `libz.tbd` `libz2.1.0.tbd` from the list.
5. Go to `Build Settings` tab and search for `Header Search Paths`. In the list, add `$(SRCROOT)/../node_modules/doctorstrange-updater` and select `recursive`.



## Usage


In your `AppDelegate.m` (make sure you complete step #5 from installation above, otherwise Xcode will not find the header file)

``` objective-c
#import "DoctorStrangeUpdater.h"
```

The code below essentially follows these steps.

1. Get an instance of `DoctorStrangeUpdater`
2. Set `self` as a `delegate`
3. Initialize with `updateMetadataUrl` , `defaultJSCodeLocation` and `defaultMetadataFileLocation`
4. Make a call to `checkUpdate`, `checkUpdateDaily` or `checkUpdateWeekly`
5. Don't forget to implement the delegate methods (optional)

``` objective-c
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // defaultJSCodeLocation is needed at least for the first startup
  NSURL* defaultJSCodeLocation = [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];

  DoctorStrangeUpdater* updater = [DoctorStrangeUpdater sharedInstance];
  [updater setDelegate:self];

  // We set the location of the metadata file that has information about the JS Code that is shipped with the app.
  // This metadata is used to compare the shipped code against the updates.

  NSURL* defaultMetadataFileLocation = [[NSBundle mainBundle] URLForResource:@"metadata" withExtension:@"json"];
  [updater initializeWithUpdateMetadataUrl:[NSURL URLWithString:JS_CODE_METADATA_URL]
                     defaultJSCodeLocation:defaultJSCodeLocation
               defaultMetadataFileLocation:defaultMetadataFileLocation ];
  [updater setHostnameForRelativeDownloadURLs:@"http://www.JimmyDaddy.com"];
  [updater checkUpdate];

  NSURL* latestJSCodeLocation = [updater latestJSCodeLocation];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  UIViewController *rootViewController = [UIViewController new];
  self.window.rootViewController = rootViewController;
  RCTBridge* bridge = [[RCTBridge alloc] initWithBundleURL:url moduleProvider:nil launchOptions:nil];
    RCTRootView* rootView = [[RCTRootView alloc] initWithBridge:bridge moduleName:@"UrAPP" initialProperties:nil];
    self.window.rootViewController.view = rootView;
  [self.window makeKeyAndVisible];
  return YES;
}
```

If you want, you can ask the user to apply the update, right after an update is downloaded. To do that, implement the delegate methods. Check the Example app to see a working sample.

`doctorstrange-updater` is highly configurable. Here are the options you can configure

``` objective-c
DoctorStrangeUpdater *updater = [DoctorStrangeUpdater sharedInstance];
/* Show progress during the udpate
 * default value - YES
 */
[updater showProgress: NO];

/* Allow use of cellular data to download the update
 * default value - NO
 */
[updater allowCellularDataUse: YES];

/* Decide what type of updates to download
 * Available options -
 *	DoctorStrangeUpdaterMajorUpdate - will download only if major version number changes
 *	DoctorStrangeUpdaterMinorUpdate - will download if major or minor version number changes
 *	DoctorStrangeUpdaterPatchUpdate - will download for any version change
 * default value - DoctorStrangeUpdaterMinorUpdate
 */
[updater downloadUpdatesForType: DoctorStrangeUpdaterMajorUpdate];

/* Check update right now
*/
[updater checkUpdate];

/* Check update daily - Only check update once per day
*/
[updater checkUpdateDaily];

/* Check update weekly - Only check updates once per week
*/
[updater checkUpdatesWeekly];

/*  When the JSON file has a relative URL for downloading the JS Bundle,
 *  set the hostname for relative downloads
 */
[updater setHostnameForRelativeDownloadURLs:@"some.com"];

```
