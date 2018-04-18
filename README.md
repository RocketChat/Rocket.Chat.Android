![Rocket.Chat logo](https://raw.githubusercontent.com/RocketChat/Rocket.Chat.Artwork/master/Logos/logo-dark.svg?sanitize=true)

# Rocket.Chat Android native application

[![CircleCI](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop.svg?style=shield)](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop) [![Build Status](https://travis-ci.org/RocketChat/Rocket.Chat.Android.svg?branch=develop)](https://travis-ci.org/RocketChat/Rocket.Chat.Android) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a81156a8682e4649994270d3670c3c83)](https://www.codacy.com/app/matheusjardimb/Rocket.Chat.Android) 

## Description
Currently, the app is maintained in two branches, namely `v1+` and `v2+`. The `v1+` is maintained in the `develop` branch and the `v2+` is maintained in the `develop-2.x` branch. The older version is written partially in `java` and `kotlin`, but we intend to write the latest version completely in `kotlin`.

Clone the repository by running `git clone https://github.com/RocketChat/Rocket.Chat.Android.git` in your terminal. To build the v1.0+ of the app, run `git checkout develop` and to build the v2.0+, run `git checkout develop-2.x`. 
Since both the versions use `kotlin` for some or all of their classes, following are the common prerequisites for both versions:
## How to build
- Android Studio 3.0+ comes with built in kotlin support, so install the latest version (3.0+) of Android Studio (recommended). For older versions, you need to manually install kotlin plugin. Go to `File > Settings > Plugins` and search for `kotlin` and install it. You'll need to restart the IDE in order to see the changes.
- Make sure that you have the latest **gradle** and the **android plugin** versions installed. Go to `File > Project Structure > Project` and make sure that you have the latest versions installed. Refer [this](https://developer.android.com/studio/releases/gradle-plugin.html#updating-gradle) to see the compatible versions.
- Kotlin is already configured in the project. To check, go to `Tools > Kotlin > Configure Kotlin in project`. A message saying kotlin is already configured in the project pops up. You can update kotlin to the latest version by going to `Tools > Kotlin > Configure Kotlin updates` and download the latest version of kotlin.

### Instructions specific to version
#### v1.0+ 
- After checking out to `develop` branch as mentioned above, simply import the project in Android Studio.

#### v2+
- This version requires the [Kotlin SDK](https://github.com/RocketChat/Rocket.Chat.Kotlin.SDK) for Rocket.Chat. Clone the Kotlin SDK in by running `git clone https://github.com/RocketChat/Rocket.Chat.Kotlin.SDK.git`.
- First, a build is required for the SDK, so that required jar files are generated. Make sure that the android repository and the kotlin sdk have the same immediate parent directory. Change the current directory to `Rocket.Chat.Android/app` and run the `build-sdk.sh` which will result in creating of the required jar file `core*.jar` and `common*.jar` in `Rocket.Chat.Android/app/libs`,by the following steps in your terminal window:
```
cd Rocket.Chat.Android/app
./build-sdk.sh
```

## How to run
### Command Line
- Connect your physical device to your pc via USB or start an emulator. Run `adb devices` in terminal. You should see your device in the list of devices.
- In order to build the debug apk, run `./gradlew assembleDebug`. This would generate a debug apk which can be found under `Rocket.Chat.Android/app/build/outputs/apk/debug` folder with the name `app-debug.apk`.
- In order to build and install the apk directly to the connected device, run `./gradlew installDebug`.

### Android Studio
- After importing the project in android studio, go to `Run > Run app` and then select your device, or create a new virtual device by following the wizard.     

## Bug report & Feature request

Please report via [GitHub issue](https://github.com/RocketChat/Rocket.Chat.Android/issues) :)

## Coding Style

Please follow our [coding style](https://github.com/RocketChat/java-code-styles/blob/master/CODING_STYLE.md) when contributing.
