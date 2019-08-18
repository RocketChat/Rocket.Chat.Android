### How to run Unit tests and UI tests on local machine

#### Unit tests

1. Fork the repo and setup the project on your local machine.
2. Open Android Studio terminal
3. Run gradlew test

#### UI tests

1. Fork the repo and setup the project on your local machine.
2. Open Android Studio terminal and run the android emulator.
3. Run gradlew connectedAndroidTest command on your AS Terminal (To run Android tests on each module and build variant in the project)
4. Run gradlew connectedPlayDebugAndroidTest (for specific play variant)

**Note:**
1. Before running UI tests on the emulator device uninstall the existing version of RC app from it.
2. It is advised to turn off all the animation of device, tests may fail if animations are on. To turn the animations off go to developer 
option in device settings.
3. It is advised to have good network connection and sufficient ram in you machine while running the UI tests


###  Any organization that forks RC can run tests against their own server
- Organisation using Rocket.Chat fork can also run the tests by doing some minor changes.
- For tests to work properly, Organisations have to follow the instructions mentioned in [Config file](https://github.com/GOVINDDIXIT/Rocket.Chat.Android/blob/develop/app/src/main/java/testConfig/Config.kt) carefully which also include creating new user.
- They have to create additional channels as mentioned in the config file.
- Also they need to customise the links present in the config file according to their server requirements.

