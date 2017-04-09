

#import "DoctorStrangeUpdater.h"
#import "StatusBarNotification.h"
#import "RCTBridge.h"
#include "bspatch.h"
#import "RCTLog.h"
#import "RCTConvert.h"

//最近一次检查更新日期
NSString* const DoctorStrangeUpdaterLastUpdateCheckDate = @"DoctorStrangeUpdater Last Update Check Date";
//当前版本信息
NSString* const DoctorStrangeUpdaterCurrentJSCodeMetadata = @"DoctorStrangeUpdater Current JS Code Metadata";
//上一次版本信息，保存用于回滚
NSString* const DoctorStrangeUpdaterPreviousJSCodeMetaData = @"DoctorStrangeUpdater previous JS Code Metadata";
//
NSString* const DoctorStrangeUpdaterAppVersionFirstOpen = @"DoctorStrangeUpdater app version first open_";

NSString* const PATCH_FILE_NOT_EXIST = @"patch file not exist";
NSString* const ORIGINAL_FILE_NOT_EXIST = @"origin file not exist";
NSString* const PATCH_FAIL = @"pacth fail";


@interface DoctorStrangeUpdater()<RCTBridgeModule>


@property NSURL* defaultJSCodeLocation;
@property NSURL* defaultMetadataFileLocation;
@property NSURL* _latestJSCodeLocation;
@property (nonatomic) BOOL showProgress;
@property (nonatomic) BOOL allowCellularDataUse;//是否允许使用蜂窝数据
@property BOOL initializationOK;


@end

@implementation DoctorStrangeUpdater {
    dispatch_queue_t _updateQueue;
}

@synthesize bridge = _bridge;


RCT_EXPORT_MODULE()

static DoctorStrangeUpdater *UPDATER_SINGLETON = nil;
static bool isFirstAccess = YES;

+ (id)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        isFirstAccess = NO;
        UPDATER_SINGLETON = [[super allocWithZone:NULL] init];
        [UPDATER_SINGLETON defaults];
    });

    return UPDATER_SINGLETON;
}

#pragma mark - Life Cycle

+ (id) allocWithZone:(NSZone *)zone {
    return [self sharedInstance];
}

+ (id)copyWithZone:(struct _NSZone *)zone {
    return [self sharedInstance];
}

+ (id)mutableCopyWithZone:(struct _NSZone *)zone {
    return [self sharedInstance];
}

- (id)copy {
    return [[DoctorStrangeUpdater alloc] init];
}

- (id)mutableCopy {
    return [[DoctorStrangeUpdater alloc] init];
}

- (id) init {
    if(UPDATER_SINGLETON){
        return UPDATER_SINGLETON;
    }
    if (isFirstAccess) {
        [self doesNotRecognizeSelector:_cmd];
    }
    self = [super init];
    if (self) {
        _updateQueue = dispatch_queue_create("doctorUpdate", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

- (void)defaults {
    self.showProgress = NO;
    self.allowCellularDataUse = YES;
}

#pragma mark - JS methods

/**
 * 使用js 获取版本号
 *
 */
- (NSDictionary *)constantsToExport {
    NSDictionary* metadata = [[NSUserDefaults standardUserDefaults] objectForKey:DoctorStrangeUpdaterCurrentJSCodeMetadata];
    NSDate* lastCheckDate = [[NSUserDefaults standardUserDefaults] objectForKey:DoctorStrangeUpdaterLastUpdateCheckDate];
    NSString* version = @"";
    NSString* description = @"";
    NSString* minContainerVersion = @"";
    NSString* minContainerBuidNumber = @"";
    if (metadata) {
        version = [metadata objectForKey:@"version"];
        description = [metadata objectForKey:@"description"];
        minContainerVersion = [metadata objectForKey:@"minContainerVersion"];
        minContainerBuidNumber = [metadata objectForKey:@"minContainerBuidNumber"];
    }
    
    NSString* lastCheckDateStr = nil;
    
    NSDateFormatter *dateFormatter =[[NSDateFormatter alloc] init];
    
    // 设置日期格式
    [dateFormatter setDateFormat:@"YYYY-mm-dd hh:mm:ss"];
    
    if (lastCheckDate) {
        lastCheckDateStr = [dateFormatter stringFromDate:lastCheckDate];
    } else {
        lastCheckDateStr = [dateFormatter stringFromDate:[NSDate date]];
    }
    UIDevice *currentDevice = [UIDevice currentDevice];
    
    return @{
             @"jsCodeVersion": version?: [NSNull null],
             @"bundleIdentifier": [[NSBundle mainBundle] bundleIdentifier],
             @"minContainerBuidNumber": minContainerBuidNumber?: [NSNull null],
             @"description": description?: [NSNull null],
             @"minContainerVersion": minContainerVersion?: [NSNull null],
             @"systemVersion": currentDevice.systemVersion,
             @"brand": @"Apple",
             @"buildNumber": [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"],
             @"appVersion": [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"],
             @"currentMetaDataKey": DoctorStrangeUpdaterCurrentJSCodeMetadata,
             @"previousMetaDataKey": DoctorStrangeUpdaterPreviousJSCodeMetaData,
             @"currentMetaData": metadata?: [NSNull null],
             @"lastCheckDateKey": DoctorStrangeUpdaterLastUpdateCheckDate,
             @"lastrCheckDate": lastCheckDateStr?: [NSNull null],
             };
}

/**
 * 对对应文件夹的文件进行patch操作
 */

RCT_EXPORT_METHOD(patch:(NSString *)patchPath
                  origin:(NSString *)origin
                  destination:(NSString *)destination
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  ){
    if (![[NSFileManager defaultManager] fileExistsAtPath:patchPath]) {
        reject(PATCH_FILE_NOT_EXIST, @"patch文件不存在.", nil);
        return;
    }
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:origin]) {
        reject(ORIGINAL_FILE_NOT_EXIST, @"originfile not exist.", nil);
        return;
    }
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:destination]) {
        [[NSFileManager defaultManager] removeItemAtPath:destination error:nil];
    }
    
    
    int err = beginPatch([origin UTF8String], [destination UTF8String], [patchPath UTF8String]);
    if (err) {
        reject(PATCH_FAIL, @"Patch error", nil);
        return;
    } else {
        resolve(destination);
        return;
    }
}

RCT_EXPORT_METHOD(reload:(NSString*)bundlePath){
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            [_bridge setValue:[NSURL fileURLWithPath:bundlePath] forKey:@"bundleURL"];
            [_bridge reload];
        } @catch (NSException *exception) {
            NSLog(@"eee%@", exception.reason);
        } @finally {
            
        }
        
    });
}


RCT_EXPORT_METHOD(setMetaData:(NSDictionary*) obj
                  key: (NSString*) key){
    @try {
        [[NSUserDefaults standardUserDefaults] setObject:obj forKey:key];
        [[NSUserDefaults standardUserDefaults] synchronize]; //立即持久化
    } @catch (NSException *exception) {
        NSLog(@"set nata data fail: %@", exception.reason);
    } @finally {
        
    }
}

RCT_EXPORT_METHOD(allowCellularDataUse: (BOOL)cellular){
    self.allowCellularDataUse = cellular;
}

RCT_EXPORT_METHOD(showProgress: (BOOL)showProgress){
    self.showProgress = showProgress;
}

RCT_EXPORT_METHOD(showMessageOnStatusBar:(NSString*) msg
                  color: (nullable NSString*) color){
    
    UIColor* uiColor = nil;

    if (color != nil) {
        uiColor = [self getColor:color];
    } else {
        uiColor = [StatusBarNotification successColor];
    }
    
    
    [StatusBarNotification showWithMessage:NSLocalizedString(msg, nil) backgroundColor:uiColor autoHide:YES];
}



#pragma mark - initialize Singleton

- (void)backToPreVersion
{
    NSLog(@"hkjhkjhkjhk");
}

- (void)initializeWithUpdateMetadataUrl:(NSURL*)defaultJSCodeLocation defaultMetadataFileLocation:(NSURL*)metadataFileLocation {
    self.defaultJSCodeLocation = defaultJSCodeLocation;
    self.defaultMetadataFileLocation = metadataFileLocation;
    //初始化app第一次打开
    [self initAppVersionFirstOpen];
    [self compareSavedMetadataAgainstContentsOfFile: self.defaultMetadataFileLocation];
}


- (NSURL*)latestJSCodeLocation {
    NSString* latestJSCodeURLString = [[[self libraryDirectory] stringByAppendingPathComponent:@"JSCode"] stringByAppendingPathComponent:@"doctor.jsbundle"];
    if ([[NSFileManager defaultManager] fileExistsAtPath:latestJSCodeURLString]) {
        self._latestJSCodeLocation = [NSURL fileURLWithPath: latestJSCodeURLString];
        return self._latestJSCodeLocation;
    } else {
        return self.defaultJSCodeLocation;
    }
}



#pragma mark - private


- (void)reject:(RCTPromiseRejectBlock)reject withError:(NSError *)error
{
    NSString *codeWithDomain = [NSString stringWithFormat:@"E%@%zd", error.domain.uppercaseString, error.code];
    reject(codeWithDomain, error.localizedDescription, error);
}

- (NSString *)getPathForDirectory:(int)directory
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, YES);
    return [paths firstObject];
}


- (UIColor *)getColor:(NSString *)hexColor {
    NSString *string = [hexColor substringFromIndex:1];//去掉#号
    unsigned int red,green,blue;
    NSRange range;
    range.length = 2;
    
    range.location = 0;
    /* 调用下面的方法处理字符串 */
    red = [self stringToInt:[string substringWithRange:range]];
    
    range.location = 2;
    green = [self stringToInt:[string substringWithRange:range]];
    range.location = 4;
    blue = [self stringToInt:[string substringWithRange:range]];
    
    return [UIColor colorWithRed:(float)(red/255.0f) green:(float)(green / 255.0f) blue:(float)(blue / 255.0f) alpha:1.0f];
}
- (int)stringToInt:(NSString *)string {
    
    unichar hex_char1 = [string characterAtIndex:0]; /* 两位16进制数中的第一位(高位*16) */
    int int_ch1;
    if (hex_char1 >= '0' && hex_char1 <= '9')
        int_ch1 = (hex_char1 - 48) * 16;   /* 0 的Ascll - 48 */
    else if (hex_char1 >= 'A' && hex_char1 <='F')
        int_ch1 = (hex_char1 - 55) * 16; /* A 的Ascll - 65 */
    else
        int_ch1 = (hex_char1 - 87) * 16; /* a 的Ascll - 97 */
    unichar hex_char2 = [string characterAtIndex:1]; /* 两位16进制数中的第二位(低位) */
    int int_ch2;
    if (hex_char2 >= '0' && hex_char2 <='9')
        int_ch2 = (hex_char2 - 48); /* 0 的Ascll - 48 */
    else if (hex_char1 >= 'A' && hex_char1 <= 'F')
        int_ch2 = hex_char2 - 55; /* A 的Ascll - 65 */
    else
        int_ch2 = hex_char2 - 87; /* a 的Ascll - 97 */
    return int_ch1+int_ch2;
}


/**
 *
 *初始化app当前原生版本是否第一次打开
 **/
- (void) initAppVersionFirstOpen{
    //获取App版本号
    NSString *version = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleVersionKey];
    NSString *flag = [DoctorStrangeUpdaterAppVersionFirstOpen stringByAppendingString: version];
    NSNumber *tmp =  [[NSUserDefaults standardUserDefaults] objectForKey:flag];
    if (nil == tmp) {
        //不存在标识，说明app是第一次打开，因此标记立即并持久化，说明是当前版本app第一次打开
        [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithBool:YES] forKey:flag];
        [[NSUserDefaults standardUserDefaults] synchronize]; //立即持久化 － 因为用户终止程序时不会保存
    }
}

/**
 * 判断当前版本app是否第一次打开
 **/
-(BOOL) currentVersionFirstOpen{
    NSString *version = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleVersionKey];
    NSString *flag = [DoctorStrangeUpdaterAppVersionFirstOpen stringByAppendingString: version];
    NSNumber *temp =  [[NSUserDefaults standardUserDefaults] objectForKey:flag];
    if (nil == temp || temp == [NSNumber numberWithBool:YES]) {
        return YES;
    } else {
        return NO;
    }
}
/**
 *初始化完成后代表不是第一次打开该app
 **/
-(void) afterFirstOpenInit{
    //获取App版本号
    NSString *version = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleVersionKey];
    NSString *flag = [DoctorStrangeUpdaterAppVersionFirstOpen stringByAppendingString: version];
    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithBool:NO] forKey:flag];
    [[NSUserDefaults standardUserDefaults] synchronize]; //立即持久化 － 因为用户终止程序时不会保存

}
/**
 *初始化文件资源信息
 **/
- (void)compareSavedMetadataAgainstContentsOfFile: (NSURL*)metadataFileLocation {
    //如果app是第一次打开，则进行资源初始化操作
    //创建资源文件夹
    [self createCodeDirectory];
    if ([self currentVersionFirstOpen]) {
        //删除上一版本的资源
        dispatch_sync(_updateQueue, ^{
            if ([self removeSources]) {
                if([self copyResource:metadataFileLocation]){
                    [self afterFirstOpenInit];
                }
            }
        });
    } else {
        //否则app在该版本就不是第一次打开，直接初始化完成
        self.initializationOK = YES;
    }
}
/**
 *删除资源文件
 **/
-(BOOL) removeSources{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString* assetsFolder = [[[self libraryDirectory] stringByAppendingPathComponent: @"JSCode"] stringByAppendingPathComponent:@"assets"];
    NSString* zipbundlePath =[[[self libraryDirectory] stringByAppendingPathComponent: @"JSCode"] stringByAppendingPathComponent:@"doctor.zip"];

    NSError *error;

    BOOL isDir = FALSE;
    BOOL isDirExist = [fileManager fileExistsAtPath: assetsFolder isDirectory: &isDir];
    if (isDir && isDirExist) {
        [fileManager removeItemAtPath:assetsFolder error:&error];
    }
    if (error) {
        return NO;
    } else {
        if([fileManager fileExistsAtPath:zipbundlePath]){
            [fileManager removeItemAtPath:zipbundlePath error:&error];
        }
        if (error) {
            return NO;
        }
        return YES;
    }
    
}

/**
 *拷贝必要资源
 **/
-(BOOL) copyResource: (NSURL*)metadataFileLocation{
    NSFileManager *fileManager = [NSFileManager defaultManager];

    NSString* assetsFolder = [[[self libraryDirectory] stringByAppendingPathComponent: @"JSCode"] stringByAppendingPathComponent:@"assets"];
    BOOL isDir = FALSE;
    //如果资源文件夹不存在则复制
    BOOL isDirExist = [fileManager fileExistsAtPath: assetsFolder isDirectory: &isDir];
    NSError *error = nil;
    if (!(isDir && isDirExist)) {
        NSString *bundleAssets = [[[NSBundle mainBundle]resourcePath]stringByAppendingPathComponent:@"assets"];
        if([fileManager copyItemAtPath:bundleAssets toPath:assetsFolder error:&error] == NO){
            if (self.showProgress) {
                [StatusBarNotification showWithMessage:NSLocalizedString(@"Copy static resources fail.", error) backgroundColor:[StatusBarNotification errorColor] autoHide:YES];
            }
            self.initializationOK = NO;
            return NO;
        };
    }


    NSData* data = [NSData dataWithContentsOfURL:self.defaultJSCodeLocation];
    NSString* filename = [NSString stringWithFormat:@"%@/%@", [self createCodeDirectory], @"doctor.jsbundle"];
    
    if ([data writeToFile:filename atomically:YES]) {
        //首先拿到打包文件中的本地版本信息文件，开发者务必要确定该文件的正确性
        NSData* fileMetadata = [NSData dataWithContentsOfURL: metadataFileLocation];
        if (!fileMetadata) {
            NSLog(@"[DoctorStrangeUpdater]: Make sure you initialize  with a metadata file.");
            if (self.showProgress) {
                [StatusBarNotification showWithMessage:NSLocalizedString(@"Error reading Metadata File.", nil) backgroundColor:[StatusBarNotification errorColor] autoHide:YES];
            }
            self.initializationOK = NO;
            return NO;
        }
        NSError *error;
        //将本地版本文件格式化为字典
        NSDictionary* localMetadata = [NSJSONSerialization JSONObjectWithData:fileMetadata options:NSJSONReadingAllowFragments error:&error];
        if (error) {
            NSLog(@"[DoctorStrangeUpdater]: Initialized with a WRONG metadata file.");
            if (self.showProgress) {
                [StatusBarNotification showWithMessage:NSLocalizedString(@"Error reading Metadata File.", error) backgroundColor:[StatusBarNotification errorColor] autoHide:YES];
            }
            self.initializationOK = NO;
            return NO;
        }

        [[NSUserDefaults standardUserDefaults] setObject:localMetadata forKey:DoctorStrangeUpdaterCurrentJSCodeMetadata];
        [[NSUserDefaults standardUserDefaults] synchronize]; //立即持久化
        return YES;
    } else {
        if (self.showProgress) {
            [StatusBarNotification showWithMessage:NSLocalizedString(@"Copy jsbundle fail.", nil) backgroundColor:[StatusBarNotification errorColor] autoHide:YES];
        }
        self.initializationOK = NO;
        return NO;

    }
    
}

/**
 * 根据差分包生成新的bundle
 * build new bundle from patch and origin bundle
 */
- (BOOL)bsdiffPatch:(NSString *)patchPath
             origin:(NSString *)origin
        destination:(NSString *)destination
{
    if (self.showProgress) {
        [StatusBarNotification showWithMessage:NSLocalizedString(@"初始化新数据中", nil)
                               backgroundColor:[StatusBarNotification errorColor]
                                      autoHide:YES];
    }

    if (![[NSFileManager defaultManager] fileExistsAtPath:patchPath]) {
        if (self.showProgress) {
            [StatusBarNotification showWithMessage:NSLocalizedString(@"patch文件不存在.", nil)
                                   backgroundColor:[StatusBarNotification errorColor]
                                          autoHide:YES];
        }
        return NO;
    }
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:origin]) {
        if (self.showProgress) {
            [StatusBarNotification showWithMessage:NSLocalizedString(@"originfile not exist.", nil)
                                   backgroundColor:[StatusBarNotification errorColor]
                                          autoHide:YES];
        }
        return NO;
    }
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:destination]) {
        [[NSFileManager defaultManager] removeItemAtPath:destination error:nil];
    }
    
    
    
    int err = beginPatch([origin UTF8String], [destination UTF8String], [patchPath UTF8String]);
    if (err) {
        return NO;
    }
    return YES;
}



- (NSString*)libraryDirectory {
    return [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) firstObject];
}

- (NSString*)createCodeDirectory {
    NSString* libraryDirectory = [self libraryDirectory];
    NSString *filePathAndDirectory = [libraryDirectory stringByAppendingPathComponent:@"JSCode"];
    NSError *error;

    NSFileManager* fileManager = [NSFileManager defaultManager];

    BOOL isDir;
    if ([fileManager fileExistsAtPath:filePathAndDirectory isDirectory:&isDir]) {
        if (isDir) {
            return filePathAndDirectory;
        }
    }

    if (![fileManager createDirectoryAtPath:filePathAndDirectory
                withIntermediateDirectories:YES
                                 attributes:nil
                                      error:&error])
    {
        NSLog(@"Create directory error: %@", error);
        if (self.showProgress) {
            [StatusBarNotification showWithMessage:NSLocalizedString(@"Create directory error: %@", error) backgroundColor:[StatusBarNotification errorColor] autoHide:YES];
        }

        return nil;
    }
    return filePathAndDirectory;
}
@end
