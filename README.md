## Description
Drop the Mic!  If your phone hits something with enough force, you'll notify everyone that you've dropped the mic!

## Requirements
* Android 4.0.4+
* Android Studio 1.5.0
* Requires GCM enabled application. You can read more about GCM [here](https://developers.google.com/cloud-messaging/). You will have to enable this yourself for your checked out app.

## Installation
### Manual
* Follow the Android Studio installation instructions appropriate for your platform. http://developer.android.com/sdk/index.html (If you are on Ubuntu 15.04 or later, the only dependencies you need are lib32stdc++6 and lib32z1. Attempting to install some of the others will result in errors)
* You will need SDK version 22. To get this version, open Android Studio > Preferences > Appearance and Behavior > System Settings > Android SDK > SDK Platforms > Check-mark Android 5.1.1, API level 22 > Ok.  The SDK will be installed.
* Once you have Android Studio installed, you will need to create a local.properties file that points to your SDK root.
* On Mac, at the project root, you can try the following command:
```
echo sdk.dir = /Users/`whoami`/Library/Android/sdk > local.properties
```
* Your installed build tools version must match the version in build.gradle.  Because Android studio does not always you to install an older build tools version, you must update build.gradle to use the version that you have.  You can find your version here: Android Studio > Preferences > Appearance and Behavior > System Settings > Android SDK > SDK Tools > Uncheck Android Build Tools > Click Apply but DO NOT click ok to uninstall the tools > Note the version of the build tools in the pop-up (23.0.3 for example) > Click Cancel.
* Update buildToolsVersion to match your build tools version.  For example:
```
buildToolsVersion "23.0.3"
```
* Now you can run either your locally installed gradle or the Gradle Wrapper script (gradlew on Linux and OS X; gradlew.bat on Windows) in order to assemble the app: `./gradlew assemble`

If you get and error like the following, make sure you have SDK API level 22 installed:
```
FAILURE: Build failed with an exception.
* What went wrong:
A problem occurred configuring root project 'cogs-android-example-app'.
> failed to find target with hash string 'android-22' in: /Users/{YourUserName}/Library/Android/sdk
```
If you get an error like the following, update your build.gradle file as described above:
```
FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring root project 'cogs-android-example-app'.
> failed to find Build Tools revision 22.0.1
```
## License

Copyright 2016 Aviata Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
