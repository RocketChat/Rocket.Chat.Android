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

Note:
1. Before running UI tests on the emulator device uninstall the existing version of RC app from it.
2. It is advised to turn off all the animation of device, tests may fail if animations are on.


###  Any organization that forks RC can run tests against their own server
1. For tests to pass, Organisations must create a user before hand using details mentioned in UITestConfig file.
2. Also they need to customise the links according to their server requirements.

