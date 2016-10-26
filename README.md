[![Circle CI](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop.svg?style=shield)](https://circleci.com/gh/RocketChat/Rocket.Chat.Android/tree/develop)

# Rocket.Chat.Android
Another implementation of Rocket.Chat Native Android Application.

![screenshots](https://cloud.githubusercontent.com/assets/11763113/11993320/ccdcf296-aa72-11e5-9950-e08f7a280516.png)

*Warning: This app is not production ready. It is under heavy development and any contributions are welcome.*


## How to build

Retrolambda needs java8 to be installed on your system
```
export ANDROID_HOME=/path/to/android/sdk

git clone https://github.com/RocketChat/Rocket.Chat.Android.git
cd Rocket.Chat.Android
git submodule init && git submodule update

echo "sdk.dir="$ANDROID_HOME > local.properties
echo 'REPRO_APP_TOKEN="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"' >> local.properties

cp app/fabric.properties.sample app/fabric.properties

./gradlew assembleDebug
```

Repro SDK Token can be dummy strings as above with the modification of RocketChatApplication.java as below.

```
diff --git a/app/src/main/java/chat/rocket/android/RocketChatApplication.java b/app/src/main/java/chat/rocket/android/RocketChatApplication.java
index d545c49..7687610 100644
--- a/app/src/main/java/chat/rocket/android/RocketChatApplication.java
+++ b/app/src/main/java/chat/rocket/android/RocketChatApplication.java
@@ -32,6 +32,6 @@ public class RocketChatApplication extends Application {

         CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
         Fabric.with(this, new Crashlytics.Builder().core(core).build());
-        Repro.setup(BuildConfig.REPRO_APP_TOKEN);
+        //Repro.setup(BuildConfig.REPRO_APP_TOKEN); //disable Repro!
     }
 }
 ```

If problem occurs even after patching, please remove (or comment out) [this line](https://github.com/RocketChat/Rocket.Chat.Android/blob/f18e20b7bff71e6143838c8258a07e91b0a9f9a0/app/build.gradle#L37).
