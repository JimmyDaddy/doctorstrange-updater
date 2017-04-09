# doctorstrange-updater
This project supports update static resources and incremental update( just for IOS now, later I will make it support Android) for react-native application.

this is a client SDK, and there is a server project named [doctorstrange](https://github.com/JimmyDaddy/doctorstrange "`github`")  (or [doctorstrange](http://gitlab.songxiaocai.org/ios/doctorstrange "`gitLab`"))

### SDK config

At first you should add a json file under your project path, there is sample of it:
``` json
{
	"version": "1.6.9",
	"minContainerVersion": "2.3.4"
}
```
Here's what the fields in the JSON mean:

* `version` — this is the version of the bundle file (in *major.minor.patch* format)
* `minContainerVersion` — this is the minimum version of the container (native) app that is allowed to download this bundle (this is needed because adding a new React Native component to your app might result into changed native app, hence, going through the AppStore review process)

updater needs know the location of this JSON file upon initialization.

## Installation
1. `npm install doctorstrange-updater --save`
2. `react-native link doctorstrange-updater`
3. In the Xcode Project Navigator, click the root project, and in `General` tab, look for `Linked Frameworks and Libraries`. Click on the `+` button at the bottom and add `libDoctorstrangeUpdater.a` `libz.tbd` `libz2.1.0.tbd` from the list.
4. Go to `Build Settings` tab and search for `Header Search Paths`. In the list, add `$(SRCROOT)/../node_modules/doctorstrange-updater` and select `recursive`.



## Usage

### IOS


In your `AppDelegate.m` (make sure you complete step #5 from installation above, otherwise Xcode will not find the header file)

``` objective-c
#import "DoctorStrangeUpdater.h"
```

The code below essentially follows these steps.

1. Get an instance of `DoctorStrangeUpdater`
2. Initialize with `defaultJSCodeLocation` and `defaultMetadataFileLocation`

``` objective-c
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // defaultJSCodeLocation is needed at least for the first startup
  NSURL* defaultJSCodeLocation = [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];

  DoctorStrangeUpdater* updater = [DoctorStrangeUpdater sharedInstance];

  NSURL* defaultMetadataFileLocation = [[NSBundle mainBundle] URLForResource:@"metadata" withExtension:@"json"];
  [updater initializeWithUpdateMetadataUrl:[NSURL URLWithString:JS_CODE_METADATA_URL]
                     defaultJSCodeLocation:defaultJSCodeLocation
               defaultMetadataFileLocation:defaultMetadataFileLocation ];

  NSURL* latestJSCodeLocation = [updater latestJSCodeLocation];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  UIViewController *rootViewController = [UIViewController new];
  self.window.rootViewController = rootViewController;
  RCTBridge* bridge = [[RCTBridge alloc] initWithBundleURL:latestJSCodeLocation moduleProvider:nil launchOptions:nil];
    RCTRootView* rootView = [[RCTRootView alloc] initWithBridge:bridge moduleName:@"UrAPP" initialProperties:nil];
    self.window.rootViewController.view = rootView;
  [self.window makeKeyAndVisible];
  return YES;
}
```

3. Set other settiings on javascript

```javascript
//first, import updater
import DoctorstrangeUpdater from 'doctorstrange-updater';
//then initial with options
let updater = DoctorstrangeUpdater.getDoctorStrangeUpdater({
	//
	DEBUG: false,

	debugVersionHost: 'http://192.168.3.28:3002/update/version/selectlatest',

	debugDownloadHost: 'http://192.168.3.28:3002/update/download',
	//set the versionhost to get the latest version
	versionHost: `http://doctorstrange.songxiaocai.org/update/version/selectlatest`,
	//source download server
	downloadHost: `http://doctorstrange.songxiaocai.org/update/download`,
	//
	errorReportHost: `http://doctorstrange.songxiaocai.org/update/errorreport`,
	//allow the plugin to use cellular data for download data
	allowCellularDataUse: true,
	showProgress: true,
	downloadStart: () => {
		this.changeState({
			showDownLoad: false
		})
	},
	downloadProgress: (progress) => {
		this.changeState({
			downloadProgress: progress,
		})
	},
	downloadEnd: () => {
		this.changeState({
			showDownLoad: false
		})
	},
	checkError: () => {
		Alert.alert(
		  'check update error',
		  'error',
		);
	},
	onError: () => {
		Alert.alert(
		  'error',
		  'error',
		);
	},
	//when the new data download completely and ready, ask user whether apply it immediately,
	//if you didn't set this option, once the new data ready, it will apply immediately
	askReload: (reload) => {
		Alert.alert(
		  'new version is ready, apply it immediately？',
		  null,
		  [
			{text: 'cancle', onPress: () => {}},
			{text: 'apply', onPress: () => reload(true)}
		  ]
		);
	},
});
//finally, check update
updater.checkUpdate();
```

### Android

To be Continue
