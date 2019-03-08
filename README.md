![Rocket.Chat logo](https://raw.githubusercontent.com/RocketChat/Rocket.Chat.Artwork/master/Logos/logo-dark.svg?sanitize=true)

# Rocket.Chat Android native application

[![CircleCI](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop.svg?style=shield)](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a81156a8682e4649994270d3670c3c83)](https://www.codacy.com/app/matheusjardimb/Rocket.Chat.Android)

## Get it from the stores

[![](https://user-images.githubusercontent.com/551004/48210434-74c07100-e35e-11e8-8eee-3ba84ffa74d7.png)](https://play.google.com/store/apps/details?id=chat.rocket.android) [![](https://user-images.githubusercontent.com/551004/48210349-50649480-e35e-11e8-97d9-74a4331faf3a.png)](https://f-droid.org/en/packages/chat.rocket.android/)

## Description

This repository contains all the code related to the Android native application of [Rocket.Chat](https://github.com/RocketChat/Rocket.Chat/#about-rocketchat). To send new pull-requests, always use the branch `develop` as base and open an issue with the description of what you want/need to accomplish, if the issue wasn't created yet.

## How to build

- Make sure that you have the latest **Gradle** and the **Android plugin** versions installed. Go to `File > Project Structure > Project` and make sure that you have the latest versions installed. Refer [this](https://developer.android.com/studio/releases/gradle-plugin.html#updating-gradle) to see the compatible versions.
- Kotlin is already configured in the project. To check, go to `Tools > Kotlin > Configure Kotlin in project`. A message saying kotlin is already configured in the project pops up. You can update kotlin to the latest version by going to `Tools > Kotlin > Configure Kotlin updates` and download the latest version of kotlin.

### SDK Instructions

- This version requires the [Kotlin SDK](https://github.com/RocketChat/Rocket.Chat.Kotlin.SDK) for Rocket.Chat. Clone the Kotlin SDK in by running `git clone https://github.com/RocketChat/Rocket.Chat.Kotlin.SDK.git`.
- First, a build is required for the SDK, so that required jar files are generated. Make sure that the Android repository and the Kotlin SDK have the same immediate parent directory. Change the current directory to `Rocket.Chat.Android/app` and run the `build-sdk.sh` which will result in creating of the required jar file `core*.jar` and `common*.jar` in `Rocket.Chat.Android/app/libs`, by the following steps in your terminal window:

```
cd Rocket.Chat.Android/app
./build-sdk.sh
```

**Note:** *You need to have Java 8 as default Java for the system (project won't build when using a Java 9+ version).*

## How to run

### Command Line

- Connect your physical device to your pc via USB or start an emulator. Run `adb devices` in terminal. You should see your device in the list of devices.
- In order to build the debug apk, run `./gradlew assembleDebug`. This would generate a debug apk which can be found under `Rocket.Chat.Android/app/build/outputs/apk/debug` folder with the name `app-debug.apk`.
- In order to build and install the apk directly to the connected device, run `./gradlew installDebug`.

### Android Studio

- After importing the project in Android Studio, go to `Run > Run app` and then select your device, or create a new virtual device by following the wizard.     

## Bug report & Feature request

Are you having a technical issue trying to compile the app, or setting up Push Notifications? Please use our Community Support channel for that: https://forums.rocket.chat/c/community-support. The issues are only supposed to be used for bugs, improvements, and features in the native Android application.

## Coding Style

Please follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) when contributing.
