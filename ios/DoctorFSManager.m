//
//  FSManager.m
//  DoctorStrange
//
//  Created by JimmyDaddy on 2017/4/9.
//  Copyright © 2017年 Air Computing Inc. All rights reserved.
//

#import "DoctorFSManager.h"
#import "SSZipArchive.h"



NSString* const UNZIP_FAIL_BUNDLE_NOT_EXIST = @"unzip fail, the bundle not exist";
NSString* const UNZIP_FAIL = @"unzip fail";
NSString* const ZIP_FILE_NOT_EXIST = @"zip file not exist";
NSString* const CREATE_DATA_DIR_FAIL = @"create data dir fail";


@implementation DoctorFSManager

RCT_EXPORT_MODULE()


- (NSArray<NSString *> *)supportedEvents
{
    return @[@"DownloadBegin", @"DownloadProgress"];
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"MainBundlePath": [[NSBundle mainBundle] bundlePath],
             @"CachesDirectoryPath": [self getPathForDirectory:NSCachesDirectory],
             @"DocumentDirectoryPath": [self getPathForDirectory:NSDocumentDirectory],
             @"ExternalDirectoryPath": [NSNull null],
             @"ExternalStorageDirectoryPath": [NSNull null],
             @"TemporaryDirectoryPath": NSTemporaryDirectory(),
             @"LibraryDirectoryPath": [self getPathForDirectory:NSLibraryDirectory],
             };
}


RCT_EXPORT_METHOD(readFile:(NSString *)filepath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:filepath];
    
    if (!fileExists) {
        return reject(@"ENOENT", [NSString stringWithFormat:@"ENOENT: no such file or directory, open '%@'", filepath], nil);
    }
    
    NSError *error = nil;
    
    NSDictionary *attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filepath error:&error];
    
    if (error) {
        return [self reject:reject withError:error];
    }
    
    if ([attributes objectForKey:NSFileType] == NSFileTypeDirectory) {
        return reject(@"EISDIR", @"EISDIR: illegal operation on a directory, read", nil);
    }
    
    NSData *content = [[NSFileManager defaultManager] contentsAtPath:filepath];
    NSString *base64Content = [content base64EncodedStringWithOptions:NSDataBase64EncodingEndLineWithLineFeed];
    
    resolve(base64Content);
}

RCT_EXPORT_METHOD(moveFile:(NSString *)filepath
                  destPath:(NSString *)destPath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSFileManager *manager = [NSFileManager defaultManager];
    
    NSError *error = nil;
    BOOL success = [manager moveItemAtPath:filepath toPath:destPath error:&error];
    
    if (!success) {
        return [self reject:reject withError:error];
    }
    
    resolve(nil);
}


RCT_EXPORT_METHOD(exists:(NSString *)filepath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(__unused RCTPromiseRejectBlock)reject)
{
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:filepath];
    
    resolve([NSNumber numberWithBool:fileExists]);
}

RCT_EXPORT_METHOD(writeFile:(NSString *)filepath
                  contents:(NSString *)base64Content
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSData *data = [[NSData alloc] initWithBase64EncodedString:base64Content options:NSDataBase64DecodingIgnoreUnknownCharacters];
    
    BOOL success = [[NSFileManager defaultManager] createFileAtPath:filepath contents:data attributes:nil];
    
    if (!success) {
        return reject(@"ENOENT", [NSString stringWithFormat:@"ENOENT: no such file or directory, open '%@'", filepath], nil);
    }
    
    return resolve(nil);
}

RCT_EXPORT_METHOD(copyFile:(NSString *)filepath
                  destPath:(NSString *)destPath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSFileManager *manager = [NSFileManager defaultManager];
    
    NSError *error = nil;
    BOOL success = [manager copyItemAtPath:filepath toPath:destPath error:&error];
    
    if (!success) {
        return [self reject:reject withError:error];
    }
    
    resolve(nil);
}

RCT_EXPORT_METHOD(stopDownload){
    
    if (self.downloader != nil) {
        [self.downloader stopDownload];
    }
}

RCT_EXPORT_METHOD(downloadFile:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    DownloadParams* params = [DownloadParams alloc];
    
    params.fromUrl = options[@"fromUrl"];
    params.toFile = options[@"toFile"];
    NSDictionary* headers = options[@"headers"];
    params.headers = headers;
    NSNumber* background = options[@"background"];
    params.background = [background boolValue];
    NSNumber* progressDivider = options[@"progressDivider"];
    params.progressDivider = progressDivider;
    
    params.completeCallback = ^(NSNumber* statusCode, NSNumber* bytesWritten) {
        NSMutableDictionary* result = [[NSMutableDictionary alloc] initWithDictionary: @{@"statusCode": statusCode}];
        if (bytesWritten) {
            [result setObject:bytesWritten forKey: @"bytesWritten"];
        }
        return resolve(result);
    };
    
    params.errorCallback = ^(NSError* error) {
        return [self reject:reject withError:error];
    };
    
    params.beginCallback = ^(NSNumber* statusCode, NSNumber* contentLength, NSDictionary* headers) {
        [self sendEventWithName: @"DownloadBegin"
                           body:@{@"statusCode": statusCode,
                                  @"contentLength": contentLength,
                                  @"headers": headers}];
    };
    
    params.progressCallback = ^(NSNumber* contentLength, NSNumber* bytesWritten) {
        [self sendEventWithName:@"DownloadProgress"
                           body:@{@"contentLength": contentLength,
                                  @"bytesWritten": bytesWritten}];
    };
    
    if (!self.downloader) self.downloader = [Downloader alloc];
    
    [self.downloader downloadFile:params];
    
}

RCT_EXPORT_METHOD(unlink:(NSString*)filepath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSFileManager *manager = [NSFileManager defaultManager];
    BOOL exists = [manager fileExistsAtPath:filepath isDirectory:false];
    
    if (!exists) {
        return reject(@"ENOENT", [NSString stringWithFormat:@"ENOENT: no such file or directory, open '%@'", filepath], nil);
    }
    
    NSError *error = nil;
    BOOL success = [manager removeItemAtPath:filepath error:&error];
    
    if (!success) {
        return [self reject:reject withError:error];
    }
    
    resolve(nil);
}

RCT_EXPORT_METHOD(mkdir:(NSString *)filepath
                  options:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSFileManager *manager = [NSFileManager defaultManager];
    
    NSError *error = nil;
    BOOL success = [manager createDirectoryAtPath:filepath withIntermediateDirectories:YES attributes:nil error:&error];
    
    if (!success) {
        return [self reject:reject withError:error];
    }
    
    NSURL *url = [NSURL fileURLWithPath:filepath];
    
    if ([[options allKeys] containsObject:@"NSURLIsExcludedFromBackupKey"]) {
        NSNumber *value = options[@"NSURLIsExcludedFromBackupKey"];
        success = [url setResourceValue: value forKey: NSURLIsExcludedFromBackupKey error: &error];
        
        if (!success) {
            return [self reject:reject withError:error];
        }
    }
    
    resolve(nil);
}

/**
 * 解压文件到指定文件夹，文件夹不存在则创建，否则返回bundle所在位置，***bundle名字约定为doctor.jsbundle***
 **/
RCT_EXPORT_METHOD(uzipFileAtPath:(NSString*)filePath
                  destinationPath:(NSString*)destinationPath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  ){
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    //判断需要解压的文件是否存在
    if ([fileManager fileExistsAtPath:filePath]) {
        //判断目标文件夹是否存在，不存在则创建
        if ([fileManager fileExistsAtPath:destinationPath] == NO) {
            NSError *error;
            if ([fileManager createDirectoryAtPath:destinationPath
                       withIntermediateDirectories:YES
                                        attributes:nil
                                             error:&error] == NO)
            {
                NSLog(@"Create directory error: %@", error);
                reject(CREATE_DATA_DIR_FAIL, @"create data dir fail", error);
                return;
            }
        }
        //进行解压
        if ([SSZipArchive unzipFileAtPath:filePath toDestination: destinationPath]) {
            NSString *bundlePath = [destinationPath stringByAppendingPathComponent:@"doctor.jsbundle"];
            //解压成功后判断文件是否存在，存在则返回文件路径
            if ([fileManager fileExistsAtPath: bundlePath]) {
                resolve(bundlePath);
                return;
            } else {
                reject(UNZIP_FAIL_BUNDLE_NOT_EXIST, @"bundle not exist", nil);
                return;
            }
        } else {
            reject(UNZIP_FAIL, @"unzip fail", nil);
            return;
        }
        
    } else {
        reject(ZIP_FILE_NOT_EXIST, @"zip not exist", nil);
    }
}

#pragma mark - private Method


- (NSString *)getPathForDirectory:(int)directory
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, YES);
    return [paths firstObject];
}


- (void)reject:(RCTPromiseRejectBlock)reject withError:(NSError *)error
{
    NSString *codeWithDomain = [NSString stringWithFormat:@"E%@%zd", error.domain.uppercaseString, error.code];
    reject(codeWithDomain, error.localizedDescription, error);
}


@end
