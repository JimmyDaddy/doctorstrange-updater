//
//  FSManager.h
//  DoctorStrange
//
//  Created by JimmyDaddy on 2017/4/9.
//  Copyright © 2017年 Air Computing Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import "Downloader.h"


@interface DoctorFSManager : RCTEventEmitter<RCTBridgeModule>

@property Downloader* downloader;


@end
