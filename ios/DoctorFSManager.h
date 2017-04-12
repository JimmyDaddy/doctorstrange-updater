//
//  FSManager.h
//  DoctorStrange
//
//  Created by JimmyDaddy on 2017/4/9.
//  Copyright © 2017年 Air Computing Inc. All rights reserved.
//

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#endif
#import <Foundation/Foundation.h>
#import "Downloader.h"



@interface DoctorFSManager : RCTEventEmitter<RCTBridgeModule>

@property Downloader* downloader;


@end
