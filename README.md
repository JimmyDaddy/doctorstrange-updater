# react-native-doctorstrange-updater  | 热更新客户端SDK
热更新客户端SDK, 提供jsbundle更新以及静态资源更新，并支持了**增量更新**,以及**版本回退**

This plugin not only supports update js code and static resources but also supports incremental update and revert update for react-native application.

这个SDK需要配合一个后端进行使用，服务端工程地址[doctorstrange](https://github.com/JimmyDaddy/doctorstrange "`github`")  (或者 [doctorstrange](http://gitlab.songxiaocai.org/ios/doctorstrange "`gitLab`"))

this is a client SDK, and there is a server project named [doctorstrange](https://github.com/JimmyDaddy/doctorstrange "`github`")  (or [doctorstrange](http://gitlab.songxiaocai.org/ios/doctorstrange "`gitLab`"))

## SDK config | SDK配置

首先需要配置jsbundle的版本信息，用于安装时初始化

At first you should add a json file under your project path, there is sample of it:
``` json
{
	"version": "1.6.9",
	"minContainerVersion": "2.3.4"
}
```
改配置文件含义如下(配置时请务必保持json文件可用):
Here's what the fields in the JSON mean:

* `version` — this is the version of the bundle file (in *major.minor.patch* format)

 指当前app打包时的js版本号，版本号请按照上述格式（数字.数字.数字）配置
* `minContainerVersion` — this is the minimum version of the container (native) app that is allowed to download this bundle (this is needed because adding a new React Native component to your app might result into changed native app, hence, going through the AppStore review process)

	当前版本js支持的最低原生App版本号，如果当前版本号过低会提示用户前去下载更新

updater needs know the location of this JSON file upon initialization.

sdk需要使用上述json文件进行初始化

## Installation | 安装

### IOS

1. `npm install react-native-doctorstrange-updater --save`
2. `react-native link react-native-doctorstrange-updater`
3. In the Xcode Project Navigator, click the root project, and in `build phases` tab, look for `Linked Frameworks and Libraries`. Click on the `+` button at the bottom and add `libDoctorstrangeUpdater.a` `libz.tbd` `libbz2.1.0.tbd` from the list.

	在Xcode中点击工程，点击到`Build Phases`，找到`Linked Frameworks and Libraries`. 点击底部的 `+`  然后添加 `libDoctorstrangeUpdater.a` `libz.tbd` `libz2.1.0.tbd` 到列表中.
4. Go to `Build Settings` tab and search for `Header Search Paths`. In the list, add `$(SRCROOT)/../node_modules/react-native-doctorstrange-updater` and select `recursive`.

	在`Build Settings`中搜索到`Header Search Paths`,添加`$(SRCROOT)/../node_modules/react-native-doctorstrange-updater`到列表中，并设置为`recursive`.


## Android
1. `npm install react-native-doctorstrange-updater --save`
2. `react-native link react-native-doctorstrange-updater`
3. add code as follows in your `settings.gradle` | 在 `setting.gradle`里添加如下代码 :
 > `include ':react-native-doctorstrange-updater'`

 > `
	project(':react-native-doctorstrange-updater').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-doctorstrange-updater/android')
`
4. add code as follows before or after `apply from: "../../node_modules/react-native/react.gradle"
` in your `build.gradle` under `android/app/` |  在`android/app/build.gradle`中  `apply from: "../../node_modules/react-native/react.gradle"
`之前或之后添加如下代码：
 > `apply from: "../../node_modules/react-native-doctorstrange-updater/android/doctor.gradle"
`

5. [set your ndk configure](https://developer.android.com/ndk/guides/setup.html)

## Usage | 使用

### IOS


In your `AppDelegate.m`
在 `AppDelegate.m`中引入头文件

``` objective-c
#import "DoctorStrangeUpdater.h"
```
然后进行如下操作
The code below essentially follows these steps.

1. Get an instance of `DoctorStrangeUpdater` | 获取`DoctorStrangeUpdater`实例
2. Initialize with `defaultJSCodeLocation` and `defaultMetadataFileLocation` | 初始化默认js代码地址以及默认版本信息文件地址

``` objective-c
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // defaultJSCodeLocation is needed at least for the first startup
  NSURL* defaultJSCodeLocation = [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];

  DoctorStrangeUpdater* updater = [DoctorStrangeUpdater sharedInstance];

  NSURL* defaultMetadataFileLocation = [[NSBundle mainBundle] URLForResource:@"metadata" withExtension:@"json"];

  [updater initializeWithUpdateMetadataUrl:defaultJSCodeLocation defaultMetadataFileLocation: defaultMetadataFileLocation];


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

### Android

in your `MainApplication.java` | 在 `MainApplication.java` 中引入包
```java
import com.sxc.doctorstrangeupdater.DoctorStrangeUpdaterPackage;

```
1.add code as follows in `getPackages()` | 在 `getPackages()`添加代码如下：
```java
@Override
protected List<ReactPackage> getPackages() {
	return Arrays.<ReactPackage>asList(
			new MainReactPackage(),
			new DoctorStrangeUpdaterPackage(getApplicationContext(), null),
	);
}
```

2. replace `getBundleAssetName()` with the method as follows | 替换 `getBundleAssetName()` 为下面的方法：
```java
    @Override
    protected String getJSBundleFile() {
        return DoctorStrangeUpdaterPackage.getJSBundleFile();
    }
```


### Common
3. Set other settiings on javascript | 在js中设置其他选项

```javascript
//first, import updater
import DoctorstrangeUpdater from 'react-native-doctorstrange-updater';
//then initial with options | 初始化
let updater = DoctorstrangeUpdater.getDoctorStrangeUpdater({
	//mode | 开发模式
	DEBUG: false,

	debugVersionHost: 'http://192.168.3.28:3002/update/version/selectlatest',

	debugDownloadHost: 'http://192.168.3.28:3002/update/download',
	debugErrorReportHost: 'http://192.168.0.156:3002/update/errorreport',
	//set the versionhost to get the latest version | 检查js版本的地址
	versionHost: `http://doctorstrange.songxiaocai.org/update/version/selectlatest`,
	//source download server | 下载资源文件的地址
	downloadHost: `http://doctorstrange.songxiaocai.org/update/download`,
	//api for reporting error | 错误报告api
	errorReportHost: `http://doctorstrange.songxiaocai.org/update/errorreport`,
	//allow the plugin to use cellular data for download data | 是否允许用户使用蜂窝数据，默认不允许
	allowCellularDataUse: true,
	//whether show run info on statusbar(default `false`) | 是否在状态栏上显示插件运行信息
	showInfo: true,

	downloadStart: () => {
		//TODO
	},
	downloadProgress: (progress) => {
		//show download progress
	},
	downloadEnd: () => {
		//TODO
	},
	//
	checkError: () => {
		//TODO
	},
	onError: () => {
		//TODO
	},
	//when the new data download completely and ready, ask user whether apply it immediately,
	//if you didn't set this option, once the new data ready, it will apply immediatly
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
	//
	alreadyUpdated: () => {
		updater.showMessageOnStatusBar('already Updated');
	},
	//
	needUpdateApp: () => {
		Alert.alert('Please update your application', 'update', [
			{text: 'cancel', onPress: ()=>{}},
			{text: 'comfirm', onPress: ()=>{
				Linking.openURL('http://appstoreurl');
			}},
		]);
	}
});
//finally, check update | 检查更新
updater.checkUpdate();
```

## API

| Name                    | exmaple  | Extra
| ----------------------- | :-------:| -------
| checkUpdate()     | ```let updater = DoctorstrangeUpdater.getDoctorStrangeUpdater() updater.checkUpdate()``` OR```DoctorstrangeUpdater.getDoctorStrangeUpdater().checkUpdate()```    |
| showMessageOnStatusBar(msg: String, color: string)| ```let updater = DoctorstrangeUpdater.getDoctorStrangeUpdater()		updater.showMessageOnStatusBar('already Updated', '#ffaa33');``` OR ``` DoctorstrangeUpdater.getDoctorStrangeUpdater().showMessageOnStatusBar('already Updated', '#ffaa33');```   |
