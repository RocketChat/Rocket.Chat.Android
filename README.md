[![Codacy Badge](https://api.codacy.com/project/badge/Grade/91f528f840e848b6aa512dea4d13ea85)](https://www.codacy.com/app/RocketChat/Rocket-Chat-Android?utm_source=github.com&utm_medium=referral&utm_content=RocketChat/Rocket.Chat.Android&utm_campaign=badger)
[![CircleCI](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop.svg?style=shield)](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop) [![Build Status](https://travis-ci.org/RocketChat/Rocket.Chat.Android.svg?branch=develop)](https://travis-ci.org/RocketChat/Rocket.Chat.Android)

# Rocket.Chat.Android
Rocket.Chat Native Android Application.

## How to build

Retrolambda needs java8 to be installed on your system
```
export ANDROID_HOME=/path/to/android/sdk

git clone https://github.com/RocketChat/Rocket.Chat.Android.git
cd Rocket.Chat.Android

echo "sdk.dir="$ANDROID_HOME > local.properties

./gradlew assembleDebug
```


## Bug report & Feature request

Please report via [GitHub issue](https://github.com/RocketChat/Rocket.Chat.Android/issues) :)
