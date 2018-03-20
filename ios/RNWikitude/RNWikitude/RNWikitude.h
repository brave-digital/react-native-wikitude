//
//  RNWikitude.h
//  RNWikitude
//
//  Created by Brave Digital Machine 7 on 2017/09/05.
//  Copyright © 2017 Brave Digital. All rights reserved.
//
@import UIKit;
// import RCTBridgeModule
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import "React/RCTBridgeModule.h"   // Required when used as a Pod in a Swift project
#endif

#import <AVFoundation/AVFoundation.h>
#import "ARViewController.h"


@interface RNWikitude : NSObject <RCTBridgeModule>

//@property (nonatomic, strong) NSDictionary *defaultOptions;
//@property (nonatomic, retain) NSMutableDictionary *options;
//@property (nonatomic, strong) RCTPromiseResolveBlock resolve;
@property (nonatomic, strong) RCTPromiseRejectBlock reject;

@end
