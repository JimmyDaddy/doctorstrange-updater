//
//  DownloadManager.h
//  DoctorStrange
//
//  Created by JimmyDaddy on 2017/4/9.
//  Copyright © 2017年 Air Computing Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
typedef void (^DownloadCompleteCallback)(NSNumber*, NSNumber*);
typedef void (^ErrorCallback)(NSError*);
typedef void (^BeginCallback)(NSNumber*, NSNumber*, NSDictionary*);
typedef void (^ProgressCallback)(NSNumber*, NSNumber*);

@interface DownloadParams : NSObject

@property (copy) NSString* fromUrl;
@property (copy) NSString* toFile;
@property (copy) NSDictionary* headers;
@property (copy) DownloadCompleteCallback completeCallback;   // Download has finished (data written)
@property (copy) ErrorCallback errorCallback;                 // Something went wrong
@property (copy) BeginCallback beginCallback;                 // Download has started (headers received)
@property (copy) ProgressCallback progressCallback;           // Download is progressing
@property        bool background;                             // Whether to continue download when app is in background
@property (copy) NSNumber* progressDivider;


@end

@interface DownloadManager : NSObject <NSURLSessionDelegate, NSURLSessionDownloadDelegate>

- (void)downloadFile:(DownloadParams*)params;
- (void)stopDownload;


@end
