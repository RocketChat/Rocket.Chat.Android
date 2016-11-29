# UNDER HEAVY DEVELOPMENT for v1.0-beta

* rough design document is [here](https://github.com/RocketChat/Rocket.Chat.Android/blob/doc/README.md)
* Rocket.Chat.Android.Lily is moved to [deprecated_lily](https://github.com/RocketChat/Rocket.Chat.Android/tree/deprecated_lily) branch.

Contribution to [the issue with `v1.0-beta` without asignee](https://github.com/RocketChat/Rocket.Chat.Android/issues?utf8=%E2%9C%93&q=is%3Aopen%20is%3Aissue%20no%3Aassignee%20v1.0-beta) is very Welcome! :smile:

---

[![CircleCI](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop.svg?style=shield)](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop)

# Rocket.Chat.Android
Rocket.Chat Native Android Application.

![screenshots](https://cloud.githubusercontent.com/assets/11763113/11993320/ccdcf296-aa72-11e5-9950-e08f7a280516.png)

*Warning: This app is not production ready. It is under heavy development and any contributions are welcome.*


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

Please report via instabug

![Instabug](https://cloud.githubusercontent.com/assets/11763113/20717302/6b0918d2-b698-11e6-86f5-df25813f0158.png)

Of course, [GitHub issue](https://github.com/RocketChat/Rocket.Chat.Android/issues) is also available :)
