# Introduction 
This is a React Native bridge module for [Wikitude](https://www.wikitude.com/) which provides a simple integration to the Wikitude AR SDK.
At the moment, the module only supports loading AR experiences from Wikitude Studio URLs. These can be local or online locations.
You would normally export the files from Wikitude Studio and then host them in a directory somewhere yourself. Point your app to that URL and it should load your project.
 
## How to install

First install the module via npm and link it up:

```bash
npm install react-native-wikitude

react-native link
```
After that completes, you will need to do additional steps for each platform you are supporting:

#### Android

1. Unfortunately the gradle system does not seem to allow [sub-linking aar files](https://issuetracker.google.com/issues/36971586). To get around this you will have to install the `wikitudesdk` folder manually into each project you plan to use this module with. 

	Copy the `wikitudesdk` folder from the `node-modules/react-native-wikitude/android` folder into your project's `android` folder: 

	```bash
	cd YourReactNativeProject
	cp -R ./node_modules/react-native-wikitude/android/wikitudesdk ./android/wikitudesdk
	```

2. And then in your `android/settings.gradle` file, modify the existing `include ':react-native-wikitude'` line to also include the `wikitudesdk`:
	```gradle
	include ':wikitudesdk', ':react-native-wikitude'
	```

And thats it. The `react-native link` command should have taken care of installing the react-native-wikitude bridge into your project so you shouldnt need to modify your gradle files.

### iOS
At the moment, iOS integration is not documented. Please contribute to update me! 


## Usage

The module exposes just one function: 
```typescript
function startAR(architectWorldURL: string, hasGeolocation:boolean, hasImageRecognition:boolean, hasInstantTracking: boolean)
```
This function will open a new Wikitude view on top of your current view and open the specified URL.

```ecmascript 6
import Wikitude from 'react-native-wikitude';

const onButtonPress = () =>
{
	Wikitude.startAR('https://yourserver.com/yourwikitudestudioproject/', true, true, true)
};
```