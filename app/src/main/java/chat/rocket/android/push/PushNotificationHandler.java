package chat.rocket.android.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.util.SparseArrayCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;

public class PushNotificationHandler implements PushConstants {

  private static final String LOG_TAG = "PushNotificationHandler";

  private static SparseArrayCompat<ArrayList<String>> messageMap = new SparseArrayCompat<>();

  private Random random = new Random();

  public static synchronized void cleanUpNotificationStack(int notId) {
    messageMap.remove(notId);
  }

  private synchronized void setNotification(int notId, String message) {
    ArrayList<String> messageList = messageMap.get(notId);
    if (messageList == null) {
      messageList = new ArrayList<>();
      messageMap.put(notId, messageList);
    }

    if (message.isEmpty()) {
      messageList.clear();
    } else {
      messageList.add(message);
    }
  }

  private synchronized ArrayList<String> getMessageList(int notId) {
    return messageMap.get(notId);
  }

  public void showNotificationIfPossible(Context context, Bundle extras) {

    // Send a notification if there is a message or title, otherwise just send data
    String message = extras.getString(MESSAGE);
    String title = extras.getString(TITLE);
    String contentAvailable = extras.getString(CONTENT_AVAILABLE);
    String forceStart = extras.getString(FORCE_START);

    Log.d(LOG_TAG, "message =[" + message + "]");
    Log.d(LOG_TAG, "title =[" + title + "]");
    Log.d(LOG_TAG, "contentAvailable =[" + contentAvailable + "]");
    Log.d(LOG_TAG, "forceStart =[" + forceStart + "]");

    if ((message != null && message.length() != 0) ||
        (title != null && title.length() != 0)) {

      Log.d(LOG_TAG, "create notification");

      if (title == null || title.isEmpty()) {
        extras.putString(TITLE, getAppName(context));
      }

      createNotification(context, extras);
    }
  }

  public void createNotification(Context context, Bundle extras) {
    NotificationManager mNotificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String appName = getAppName(context);
    String packageName = context.getPackageName();
    Resources resources = context.getResources();

    String hostname = getHostname(extras);
    String roomId = getRoomId(extras);

    if (hostname == null || roomId == null || !isValidHostname(context, hostname)) {
      return;
    }

    int notId = parseInt(NOT_ID, extras);
    Intent notificationIntent = new Intent(context, MainActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationIntent.putExtra(PUSH_BUNDLE, extras);
    notificationIntent.putExtra(HOSTNAME, hostname);
    notificationIntent.putExtra(ROOM_ID, roomId);
    notificationIntent.putExtra(NOT_ID, notId);

    int requestCode = random.nextInt();
    PendingIntent contentIntent = PendingIntent
        .getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
        .setWhen(System.currentTimeMillis())
        .setContentTitle(fromHtml(extras.getString(TITLE)))
        .setTicker(fromHtml(extras.getString(TITLE)))
        .setContentIntent(contentIntent)
        .setAutoCancel(true);

    SharedPreferences prefs = context
        .getSharedPreferences(PushConstants.COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
    String localIcon = prefs.getString(ICON, null);
    String localIconColor = prefs.getString(ICON_COLOR, null);
    boolean soundOption = prefs.getBoolean(SOUND, true);
    boolean vibrateOption = prefs.getBoolean(VIBRATE, true);
    Log.d(LOG_TAG, "stored icon=" + localIcon);
    Log.d(LOG_TAG, "stored iconColor=" + localIconColor);
    Log.d(LOG_TAG, "stored sound=" + soundOption);
    Log.d(LOG_TAG, "stored vibrate=" + vibrateOption);

        /*
         * Notification Vibration
         */

    setNotificationVibration(extras, vibrateOption, notificationBuilder);

        /*
         * Notification Icon Color
         *
         * Sets the small-icon background color of the notification.
         * To use, add the `iconColor` key to plugin android options
         *
         */
    setNotificationIconColor(extras.getString("color"), notificationBuilder, localIconColor);

        /*
         * Notification Icon
         *
         * Sets the small-icon of the notification.
         *
         * - checks the plugin options for `icon` key
         * - if none, uses the application icon
         *
         * The icon value must be a string that maps to a drawable resource.
         * If no resource is found, falls
         *
         */
    setNotificationSmallIcon(context, extras, packageName, resources, notificationBuilder,
        localIcon);

        /*
         * Notification Large-Icon
         *
         * Sets the large-icon of the notification
         *
         * - checks the gcm data for the `image` key
         * - checks to see if remote image, loads it.
         * - checks to see if assets image, Loads It.
         * - checks to see if resource image, LOADS IT!
         * - if none, we don't set the large icon
         *
         */
    setNotificationLargeIcon(context, extras, packageName, resources, notificationBuilder);

        /*
         * Notification Sound
         */
    if (soundOption) {
      setNotificationSound(context, extras, notificationBuilder);
    }

        /*
         *  LED Notification
         */
    setNotificationLedColor(extras, notificationBuilder);

        /*
         *  Priority Notification
         */
    setNotificationPriority(extras, notificationBuilder);

        /*
         * Notification message
         */
    setNotificationMessage(notId, extras, notificationBuilder);

        /*
         * Notification count
         */
    setNotificationCount(context, extras, notificationBuilder);

        /*
         * Notification count
         */
    setVisibility(context, extras, notificationBuilder);

        /*
         * Notification add actions
         */
    createActions(context, extras, notificationBuilder, resources, packageName, notId);

    mNotificationManager.notify(appName, notId, notificationBuilder.build());
  }

  private void createActions(Context context, Bundle extras, NotificationCompat.Builder mBuilder,
                             Resources resources, String packageName, int notId) {
    Log.d(LOG_TAG, "create actions: with in-line");
    String actions = extras.getString(ACTIONS);
    if (actions == null) {
      return;
    }

    try {
      JSONArray actionsArray = new JSONArray(actions);
      ArrayList<NotificationCompat.Action> wActions = new ArrayList<>();
      for (int i = 0; i < actionsArray.length(); i++) {
        int min = 1;
        int max = 2000000000;
        Random random = new Random();
        int uniquePendingIntentRequestCode = random.nextInt((max - min) + 1) + min;
        Log.d(LOG_TAG, "adding action");
        JSONObject action = actionsArray.getJSONObject(i);
        Log.d(LOG_TAG, "adding callback = " + action.getString(CALLBACK));
        boolean foreground = action.optBoolean(FOREGROUND, true);
        boolean inline = action.optBoolean("inline", false);
        Intent intent;
        PendingIntent pIntent;
        if (inline) {
          Log.d(LOG_TAG, "Version: " + android.os.Build.VERSION.SDK_INT + " = "
              + android.os.Build.VERSION_CODES.M);
          if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
            Log.d(LOG_TAG, "push activity");
            intent = new Intent(context, MainActivity.class);
          } else {
            Log.d(LOG_TAG, "push receiver");
            intent = new Intent(context, BackgroundActionButtonHandler.class);
          }

          updateIntent(intent, action.getString(CALLBACK), extras, foreground, notId);

          if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
            Log.d(LOG_TAG, "push activity for notId " + notId);
            pIntent =
                PendingIntent.getActivity(context, uniquePendingIntentRequestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT);
          } else {
            Log.d(LOG_TAG, "push receiver for notId " + notId);
            pIntent = PendingIntent
                .getBroadcast(context, uniquePendingIntentRequestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT);
          }
        } else if (foreground) {
          intent = new Intent(context, MainActivity.class);
          updateIntent(intent, action.getString(CALLBACK), extras, foreground, notId);
          pIntent = PendingIntent
              .getActivity(context, uniquePendingIntentRequestCode, intent,
                  PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
          intent = new Intent(context, BackgroundActionButtonHandler.class);
          updateIntent(intent, action.getString(CALLBACK), extras, foreground, notId);
          pIntent = PendingIntent
              .getBroadcast(context, uniquePendingIntentRequestCode, intent,
                  PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
            resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
            action.getString(TITLE), pIntent);

        RemoteInput remoteInput;
        if (inline) {
          Log.d(LOG_TAG, "create remote input");
          String replyLabel = "Enter your reply here";
          remoteInput = new RemoteInput.Builder(INLINE_REPLY)
              .setLabel(replyLabel)
              .build();
          actionBuilder.addRemoteInput(remoteInput);
        }

        NotificationCompat.Action wAction = actionBuilder.build();
        wActions.add(actionBuilder.build());

        if (inline) {
          mBuilder.addAction(wAction);
        } else {
          mBuilder.addAction(
              resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
              action.getString(TITLE), pIntent);
        }
        wAction = null;
        pIntent = null;
      }
      mBuilder.extend(new NotificationCompat.WearableExtender().addActions(wActions));
      wActions.clear();
    } catch (JSONException e) {
      // nope
    }
  }

  private void setNotificationCount(Context context, Bundle extras,
                                    NotificationCompat.Builder mBuilder) {
    int count = extractBadgeCount(extras);
    if (count >= 0) {
      Log.d(LOG_TAG, "count =[" + count + "]");
      mBuilder.setNumber(count);
    }
  }

  private void setVisibility(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
    String visibilityStr = extras.getString(VISIBILITY);
    if (visibilityStr == null) {
      return;
    }

    try {
      Integer visibility = Integer.parseInt(visibilityStr);
      if (visibility >= NotificationCompat.VISIBILITY_SECRET
          && visibility <= NotificationCompat.VISIBILITY_PUBLIC) {
        mBuilder.setVisibility(visibility);
      } else {
        Log.e(LOG_TAG, "Visibility parameter must be between -1 and 1");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  private void setNotificationVibration(Bundle extras, Boolean vibrateOption,
                                        NotificationCompat.Builder mBuilder) {
    String vibrationPattern = extras.getString(VIBRATION_PATTERN);
    if (vibrationPattern != null) {
      String[] items = vibrationPattern.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
      long[] results = new long[items.length];
      for (int i = 0; i < items.length; i++) {
        try {
          results[i] = Long.parseLong(items[i].trim());
        } catch (NumberFormatException nfe) {
        }
      }
      mBuilder.setVibrate(results);
    } else {
      if (vibrateOption) {
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
      }
    }
  }

  private void setNotificationMessage(int notId, Bundle extras,
                                      NotificationCompat.Builder mBuilder) {
    String message = extras.getString(MESSAGE);

    String style = extras.getString(STYLE, STYLE_TEXT);
    if (STYLE_INBOX.equals(style)) {
      setNotification(notId, message);

      mBuilder.setContentText(fromHtml(message));

      ArrayList<String> messageList = getMessageList(notId);
      Integer sizeList = messageList.size();
      if (sizeList > 1) {
        String sizeListMessage = sizeList.toString();
        String stacking = sizeList + " more";
        if (extras.getString(SUMMARY_TEXT) != null) {
          stacking = extras.getString(SUMMARY_TEXT);
          stacking = stacking.replace("%n%", sizeListMessage);
        }
        NotificationCompat.InboxStyle notificationInbox = new NotificationCompat.InboxStyle()
            .setBigContentTitle(fromHtml(extras.getString(TITLE)))
            .setSummaryText(fromHtml(stacking));

        for (int i = messageList.size() - 1; i >= 0; i--) {
          notificationInbox.addLine(fromHtml(messageList.get(i)));
        }

        mBuilder.setStyle(notificationInbox);
      } else {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        if (message != null) {
          bigText.bigText(fromHtml(message));
          bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));
          mBuilder.setStyle(bigText);
        }
      }
    } else if (STYLE_PICTURE.equals(style)) {
      setNotification(notId, "");

      NotificationCompat.BigPictureStyle bigPicture = new NotificationCompat.BigPictureStyle();
      bigPicture.bigPicture(getBitmapFromURL(extras.getString(PICTURE)));
      bigPicture.setBigContentTitle(fromHtml(extras.getString(TITLE)));
      bigPicture.setSummaryText(fromHtml(extras.getString(SUMMARY_TEXT)));

      mBuilder.setContentTitle(fromHtml(extras.getString(TITLE)));
      mBuilder.setContentText(fromHtml(message));

      mBuilder.setStyle(bigPicture);
    } else {
      setNotification(notId, "");

      NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

      if (message != null) {
        mBuilder.setContentText(fromHtml(message));

        bigText.bigText(fromHtml(message));
        bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));

        String summaryText = extras.getString(SUMMARY_TEXT);
        if (summaryText != null) {
          bigText.setSummaryText(fromHtml(summaryText));
        }

        mBuilder.setStyle(bigText);
      }
    }
  }

  private void setNotificationSound(Context context, Bundle extras,
                                    NotificationCompat.Builder mBuilder) {
    String soundname = extras.getString(SOUNDNAME);
    if (soundname == null) {
      soundname = extras.getString(SOUND);
    }
    if (SOUND_RINGTONE.equals(soundname)) {
      mBuilder.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI);
    } else if (soundname != null && !soundname.contentEquals(SOUND_DEFAULT)) {
      Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
          + "://" + context.getPackageName() + "/raw/" + soundname);
      Log.d(LOG_TAG, sound.toString());
      mBuilder.setSound(sound);
    } else {
      mBuilder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
    }
  }

  private void setNotificationLedColor(Bundle extras, NotificationCompat.Builder mBuilder) {
    String ledColor = extras.getString(LED_COLOR);
    if (ledColor == null) {
      return;
    }

    // Converts parse Int Array from ledColor
    String[] items = ledColor.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
    int[] results = new int[items.length];
    for (int i = 0; i < items.length; i++) {
      try {
        results[i] = Integer.parseInt(items[i].trim());
      } catch (NumberFormatException nfe) {
      }
    }

    if (results.length == 4) {
      mBuilder.setLights(Color.argb(results[0], results[1], results[2], results[3]), 500, 500);
    } else {
      Log.e(LOG_TAG, "ledColor parameter must be an array of length == 4 (ARGB)");
    }

  }

  private void setNotificationPriority(Bundle extras, NotificationCompat.Builder mBuilder) {
    String priorityStr = extras.getString(PRIORITY);
    if (priorityStr == null) {
      return;
    }

    try {
      Integer priority = Integer.parseInt(priorityStr);
      if (priority >= NotificationCompat.PRIORITY_MIN
          && priority <= NotificationCompat.PRIORITY_MAX) {
        mBuilder.setPriority(priority);
      } else {
        Log.e(LOG_TAG, "Priority parameter must be between -2 and 2");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

  }

  private void setNotificationLargeIcon(Context context, Bundle extras, String packageName,
                                        Resources resources, NotificationCompat.Builder mBuilder) {
    String gcmLargeIcon = extras.getString(IMAGE); // from gcm
    if (gcmLargeIcon == null || "".equals(gcmLargeIcon)) {
      return;
    }

    if (gcmLargeIcon.startsWith("http://") || gcmLargeIcon.startsWith("https://")) {
      mBuilder.setLargeIcon(getBitmapFromURL(gcmLargeIcon));
      Log.d(LOG_TAG, "using remote large-icon from gcm");
    } else {
      AssetManager assetManager = context.getAssets();
      InputStream istr;
      try {
        istr = assetManager.open(gcmLargeIcon);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        mBuilder.setLargeIcon(bitmap);
        Log.d(LOG_TAG, "using assets large-icon from gcm");
      } catch (IOException e) {
        int largeIconId = resources.getIdentifier(gcmLargeIcon, DRAWABLE, packageName);
        if (largeIconId != 0) {
          Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
          mBuilder.setLargeIcon(largeIconBitmap);
          Log.d(LOG_TAG, "using resources large-icon from gcm");
        } else {
          Log.d(LOG_TAG, "Not setting large icon");
        }
      }
    }
  }

  private void setNotificationSmallIcon(Context context, Bundle extras, String packageName,
                                        Resources resources, NotificationCompat.Builder mBuilder,
                                        String localIcon) {
    int iconId = 0;
    String icon = extras.getString(ICON);
    if (icon != null && !"".equals(icon)) {
      iconId = resources.getIdentifier(icon, DRAWABLE, packageName);
      Log.d(LOG_TAG, "using icon from plugin options");
    } else if (localIcon != null && !"".equals(localIcon)) {
      iconId = resources.getIdentifier(localIcon, DRAWABLE, packageName);
      Log.d(LOG_TAG, "using icon from plugin options");
    }
    if (iconId == 0) {
      Log.d(LOG_TAG, "no icon resource found - using default icon");
      iconId = resources.getIdentifier("rocket_chat_notification", DRAWABLE, packageName);
    }
    mBuilder.setSmallIcon(iconId);
  }

  private void setNotificationIconColor(String color, NotificationCompat.Builder mBuilder,
                                        String localIconColor) {
    int iconColor = 0;
    if (color != null && !"".equals(color)) {
      try {
        iconColor = Color.parseColor(color);
      } catch (IllegalArgumentException e) {
        Log.e(LOG_TAG, "couldn't parse color from android options");
      }
    } else if (localIconColor != null && !"".equals(localIconColor)) {
      try {
        iconColor = Color.parseColor(localIconColor);
      } catch (IllegalArgumentException e) {
        Log.e(LOG_TAG, "couldn't parse color from android options");
      }
    }
    if (iconColor != 0) {
      mBuilder.setColor(iconColor);
    }
  }

  private void updateIntent(Intent intent, String callback, Bundle extras, boolean foreground,
                            int notId) {
    intent.putExtra(CALLBACK, callback);
    intent.putExtra(PUSH_BUNDLE, extras);
    intent.putExtra(FOREGROUND, foreground);
    intent.putExtra(NOT_ID, notId);
  }

  public Bitmap getBitmapFromURL(String strURL) {
    try {
      URL url = new URL(strURL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      return BitmapFactory.decodeStream(input);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getAppName(Context context) {
    CharSequence appName = context.getPackageManager()
        .getApplicationLabel(context.getApplicationInfo());
    return (String) appName;
  }

  private int parseInt(String value, Bundle extras) {
    int retval = 0;

    try {
      retval = Integer.parseInt(extras.getString(value));
    } catch (NumberFormatException e) {
      Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
    } catch (Exception e) {
      Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
    }

    return retval;
  }

  private Spanned fromHtml(String source) {
    if (source != null) {
      return Html.fromHtml(source);
    } else {
      return null;
    }
  }

  private int extractBadgeCount(Bundle extras) {
    int count = -1;
    String msgcnt = extras.getString(COUNT);

    try {
      if (msgcnt != null) {
        count = Integer.parseInt(msgcnt);
      }
    } catch (NumberFormatException e) {
      Log.e(LOG_TAG, e.getLocalizedMessage(), e);
    }

    return count;
  }

  private String getHostname(Bundle extras) {
    try {
      JSONObject jsonObject = new JSONObject(extras.getString("ejson", "[]"));
      if (!jsonObject.has("host")) {
        return null;
      }

      return ServerPolicyHelper.enforceHostname(jsonObject.getString("host"));
    } catch (Exception e) {
      return null;
    }
  }

  private String getRoomId(Bundle extras) {
    try {
      JSONObject jsonObject = new JSONObject(extras.getString("ejson", "[]"));
      if (!jsonObject.has("rid")) {
        return null;
      }

      return jsonObject.getString("rid");
    } catch (Exception e) {
      return null;
    }
  }

  private boolean isValidHostname(Context context, String hostname) {
    final List<ServerInfo> serverInfoList =
        ConnectivityManager.getInstance(context.getApplicationContext()).getServerList();

    for (ServerInfo serverInfo : serverInfoList) {
      if (serverInfo.getHostname().equals(hostname)) {
        return true;
      }
    }

    return false;
  }
}
