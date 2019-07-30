
# react-native-yt-clickread

## Getting started

`$ npm install react-native-yt-clickread --save`

### Mostly automatic installation

`$ react-native link react-native-yt-clickread`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-yt-clickread` and add `RNYtClickread.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNYtClickread.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.yunti.clickread.RNYtClickreadPackage;` to the imports at the top of the file
  - Add `new RNYtClickreadPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-yt-clickread'
  	project(':react-native-yt-clickread').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-yt-clickread/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-yt-clickread')
  	```


## Usage
```javascript
import RNYtClickread from 'react-native-yt-clickread';

// TODO: What to do with the module?
RNYtClickread;
```
  