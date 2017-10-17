# Rocket.Chat Android native application

[![CircleCI](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop.svg?style=shield)](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop) [![Build Status](https://travis-ci.org/RocketChat/Rocket.Chat.Android.svg?branch=develop)](https://travis-ci.org/RocketChat/Rocket.Chat.Android) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a81156a8682e4649994270d3670c3c83)](https://www.codacy.com/app/matheusjardimb/Rocket.Chat.Android)

# Get it from Google Play

[![Rocket.Chat on Google Play](https://user-images.githubusercontent.com/551004/29770692-a20975c6-8bc6-11e7-8ab0-1cde275496e0.png)](https://play.google.com/store/apps/details?id=chat.rocket.android)

## How to build

Retrolambda needs java8 to be installed on your system
```
$ export ANDROID_HOME=/path/to/android/sdk
$ git clone https://github.com/RocketChat/Rocket.Chat.Android.git
$ cd Rocket.Chat.Android
$ echo "sdk.dir="$ANDROID_HOME > local.properties
$ ./gradlew assembleDebug
(> gradlew assembleDebug on Windows)
```

### How to send APK to device

The following steps are only needed if running via command line. They are not needed if you are building via Android Studio.

Ensure that ADB recognizes your device with `$ adb devices`.

If a single device exists, install via `$ adb install /path/to/apk.apk`.

Assuming you used Gradle like earlier, the file will be called `module_name-debug.apk` in `project_name/module_name/build/outputs/apk/`.

Alternatively, you can simply run `$ ./gradlew installDebug` (`> gradlew installDebug` on Windows) to build, deploy, and debug all in a single command.

## Bug report & Feature request

Please report via [GitHub issue](https://github.com/RocketChat/Rocket.Chat.Android/issues) :)

## Coding Style

Please follow our [coding style](https://github.com/RocketChat/java-code-styles/blob/master/CODING_STYLE.md) when contributing.
