# React Native Wikitude Bridge

# Introduction
This is a React Native bridge module for [Wikitude](https://www.wikitude.com/) which provides a simple integration to the Wikitude AR SDK.
At the moment, the module only supports loading AR experiences from Wikitude Studio URLs. These can be local or online locations.
You would normally export the files from Wikitude Studio and then host them in a directory somewhere yourself. Point your app to that URL and it should load your project.

## How to install

First install the module via npm and link it up:

```bash
npm install react-native-wikitude

react-native link react-native-wikitude
```
After that completes, you will need to do additional steps for each platform you are supporting:

#### Android

1. Unfortunately the gradle system does not seem to allow [sub-linking aar files](https://issuetracker.google.com/issues/36971586). To get around this you will have to install the `wikitudesdk` folder manually into each project you plan to use this module with.

	Copy the `wikitudesdk` folder from the `node-modules/react-native-wikitude/android` folder into your project's `android` folder:

	On Mac / Linux:

	```bash
	cd YourReactNativeProject
	cp -R ./node_modules/react-native-wikitude/android/wikitudesdk ./android/wikitudesdk
	```

	or on Windows:

	```dos
	cd YourReactNativeProject
	xcopy node_modules\react-native-wikitude\android\wikitudesdk android\wikitudesdk /E
	```

2. And then in your `android/settings.gradle` file, modify the existing `include ':react-native-wikitude'` line to also include the `wikitudesdk`:
	```gradle
	include ':wikitudesdk', ':react-native-wikitude'
	```

3. In your `android/build.gradle` file, modify the minimum SDK version to at least version 19:
	```gradle
	android {
		defaultConfig {
			...
			minSdkVersion 19
			...
		}
	```
4. In your `android/app/src/main/AndroidManifest.xml` file, If you have it, remove the `android:allowBackup="false"` attribute from the `application` node. If you want to set allowBackup, follow the method [here](https://github.com/OfficeDev/msa-auth-for-android/issues/21).

5. Optionally: In your `android/build.gradle` file, define the versions of the standard libraries you'd like WikitudeBridge to use:
	```gradle
	...
	ext {
		// dependency versions
		compileSdkVersion = "<Your compile SDK version>" // default: 27
		buildToolsVersion = "<Your build tools version>" // default: "27.0.3"
		targetSdkVersion = "<Your target SDK version>" // default: 27
		constraintLayoutVersion = "<Your com.android.support.constraint:constraint-layout version>" //default "1.0.2"
	}
	...
	```

### iOS

React native linker should handle all the linking for you, or you can manually link the FrameWork if you want it in a different location as long as you change the Framework Search Path to the correct path in Xcode.

Also make sure the bridge is after React Native projects in your build order.

The JavaScript Framework Version 7.2 is included in this repository.

## Usage

The module exposes just one function:
```typescript
function startAR(architectWorldURL: string, hasGeolocation:boolean, hasImageRecognition:boolean, hasInstantTracking:boolean, wikitudeSDKKey:string)
```
This function will open a new Wikitude view on top of your current view and open the specified URL.

```ecmascript 6
import Wikitude from 'react-native-wikitude';

const onButtonPress = () =>
{
	Wikitude.startAR('https://yourserver.com/yourwikitudestudioproject/', true, true, true, "YourSDKKey")
};
```



## ChangeLog

- 2.0.4
	Minor Gradle file issue fixed.

- 2.0.3
	Keep screen on in Android

- 2.0.2
	Fixed errors, added SDK Key field

- 2.0.1
	Fixed android build process

- 2.0.0
	First Commit
