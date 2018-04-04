//
//  ARViewController.h
//  RNWikitude
//
//  Created by Brave Digital Machine 7 on 2017/09/06.
//  Copyright © 2017 Brave Digital. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <WikitudeSDK/WTArchitectView.h>

@protocol ARViewControllerDelegate <NSObject>
@required
- (void)dataFromController:(NSString *)data;
@end

@interface ARViewController : UIViewController

@property (nonatomic, strong) WTArchitectView               *architectView;
@property (nonatomic, weak) WTNavigation                    *architectWorldNavigation;
@property (nonatomic, retain) NSString                        *url;
@property (nonatomic, retain) NSString                        *sdkkey;
@property (nonatomic, weak) id<ARViewControllerDelegate> delegate;

@end
