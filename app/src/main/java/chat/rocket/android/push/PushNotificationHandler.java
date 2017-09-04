package chat.rocket.android.push;

import android.app.Notification;
import android.app.NotificationChannel;
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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String appName = getAppName(context);
    String packageName = context.getPackageName();
    Resources resources = context.getResources();

    String hostname = getHostname(extras);
    String roomId = getRoomId(extras);

    int notId = parseInt(NOT_ID, extras);
    Intent notificationIntent = new Intent(context, MainActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationIntent.putExtra(PUSH_BUNDLE, extras);
    notificationIntent.putExtra(NOT_ID, notId);

    if (hostname != null && roomId != null && isValidHostname(context, hostname)) {
      notificationIntent.putExtra(HOSTNAME, hostname);
      notificationIntent.putExtra(ROOM_ID, roomId);
    }

    int requestCode = random.nextInt();
    PendingIntent contentIntent = PendingIntent
        .getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    if (Build.VERSION.SDK_INT >= 26) {
      String channelId = "rocket-chat-channel";
      CharSequence name = "RocketChatMessage";
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel channel = new NotificationChannel(channelId, name, importance);
      channel.enableLights(true);
      notificationManager.createNotificationChannel(channel);

      Notification.Builder notificationBuilder = new Notification.Builder(context, channelId)
              .setWhen(System.currentTimeMillis())
              .setContentTitle(fromHtml(extras.getString(TITLE)))
              .setTicker(fromHtml(extras.getString(TITLE)))
              .setContentIntent(contentIntent)
              .setChannelId(channelId)
              .setAutoCancel(true);

      setNotificationImportance(extras, channel);
      setNotificationVibration(extras, vibrateOption, channel);
      setNotificationMessage(notId, extras, notificationBuilder);
      setNotificationCount(context, extras, notificationBuilder);
      setNotificationSmallIcon(extras, packageName, resources, notificationBuilder,
              localIcon);
      setNotificationLargeIcon(context, extras, packageName, resources, notificationBuilder);
      setNotificationLedColor(extras, channel);
      if (soundOption) {
        setNotificationSound(context, extras, channel);
      }
      createActions(context, extras, notificationBuilder, resources, packageName, notId);
      notificationManager.notify(notId, notificationBuilder.build());
    } else {
      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
              .setWhen(System.currentTimeMillis())
              .setContentTitle(fromHtml(extras.getString(TITLE)))
              .setTicker(fromHtml(extras.getString(TITLE)))
              .setContentIntent(contentIntent)
              .setAutoCancel(true);

      setNotificationCount(context, extras, notificationBuilder);
      setNotificationVibration(extras, vibrateOption, notificationBuilder);
      setNotificationIconColor(extras.getString("color"), notificationBuilder, localIconColor);
      setNotificationSmallIcon(extras, packageName, resources, notificationBuilder,
              localIcon);
      setNotificationLargeIcon(context, extras, packageName, resources, notificationBuilder);
      if (soundOption) {
        setNotificationSound(context, extras, notificationBuilder);
      }
      setNotificationLedColor(extras, notificationBuilder);
      setNotificationPriority(extras, notificationBuilder);
      setNotificationMessage(notId, extras, notificationBuilder);
      setVisibility(context, extras, notificationBuilder);
      createActions(context, extras, notificationBuilder, resources, packageName, notId);
      notificationManager.notify(appName, notId, notificationBuilder.build());
    }
  }

  private void createActions(Context context, Bundle extras, NotificationCompat.Builder builder,
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
          builder.addAction(wAction);
        } else {
          builder.addAction(
              resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
              action.getString(TITLE), pIntent);
        }
        wAction = null;
        pIntent = null;
      }
      builder.extend(new NotificationCompat.WearableExtender().addActions(wActions));
      wActions.clear();
    } catch (JSONException e) {
      // nope
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
  private void createActions(Context context, Bundle extras, Notification.Builder builder,
                             Resources resources, String packageName, int notId) {
    Log.d(LOG_TAG, "create actions: with in-line");
    String actions = extras.getString(ACTIONS);
    if (actions == null) {
      return;
    }

    try {
      JSONArray actionsArray = new JSONArray(actions);
      ArrayList<Notification.Action> wActions = new ArrayList<>();
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

        Notification.Action.Builder actionBuilder = new Notification.Action.Builder(
                resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
                action.getString(TITLE), pIntent);

        android.app.RemoteInput remoteInput;
        if (inline) {
          Log.d(LOG_TAG, "create remote input");
          String replyLabel = "Enter your reply here";
          remoteInput = new android.app.RemoteInput.Builder(INLINE_REPLY)
                  .setLabel(replyLabel)
                  .build();
          actionBuilder.addRemoteInput(remoteInput);
        }

        Notification.Action wAction = actionBuilder.build();
        wActions.add(actionBuilder.build());

        if (inline) {
          builder.addAction(wAction);
        } else {
          builder.addAction(
                  resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
                  action.getString(TITLE), pIntent);
        }
        wAction = null;
        pIntent = null;
      }
      builder.extend(new Notification.WearableExtender().addActions(wActions));
      wActions.clear();
    } catch (JSONException e) {
      // nope
    }
  }

  private void setNotificationCount(Context context, Bundle extras,
                                    NotificationCompat.Builder builder) {
    int count = extractBadgeCount(extras);
    if (count >= 0) {
      Log.d(LOG_TAG, "count =[" + count + "]");
      builder.setNumber(count);
    }
  }

  private void setNotificationCount(Context context, Bundle extras,
                                    Notification.Builder builder) {
    int count = extractBadgeCount(extras);
    if (count >= 0) {
      Log.d(LOG_TAG, "count =[" + count + "]");
      builder.setNumber(count);
    }
  }

  private void setVisibility(Context context, Bundle extras, NotificationCompat.Builder builder) {
    String visibilityStr = extras.getString(VISIBILITY);
    if (visibilityStr == null) {
      return;
    }

    try {
      Integer visibility = Integer.parseInt(visibilityStr);
      if (visibility >= NotificationCompat.VISIBILITY_SECRET
              && visibility <= NotificationCompat.VISIBILITY_PUBLIC) {
        builder.setVisibility(visibility);
      } else {
        Log.e(LOG_TAG, "Visibility parameter must be between -1 and 1");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  private void setNotificationVibration(Bundle extras, Boolean vibrateOption,
                                        NotificationCompat.Builder builder) {
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
      builder.setVibrate(results);
    } else {
      if (vibrateOption) {
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
      }
    }
  }

  @RequiresApi(api = 26)
  private void setNotificationVibration(Bundle extras, Boolean vibrateOption,
                                        NotificationChannel channel) {
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
      channel.setVibrationPattern(results);
    } else {
      if (vibrateOption) {
        channel.enableVibration(true);
      }
    }
  }

  private void setNotificationMessage(int notId, Bundle extras,
                                      NotificationCompat.Builder builder) {
    String message = extras.getString(MESSAGE);

    String style = extras.getString(STYLE, STYLE_TEXT);
    if (STYLE_INBOX.equals(style)) {
      setNotification(notId, message);

      builder.setContentText(fromHtml(message));

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

        builder.setStyle(notificationInbox);
      } else {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        if (message != null) {
          bigText.bigText(fromHtml(message));
          bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));
          builder.setStyle(bigText);
        }
      }
    } else if (STYLE_PICTURE.equals(style)) {
      setNotification(notId, "");

      NotificationCompat.BigPictureStyle bigPicture = new NotificationCompat.BigPictureStyle();
      bigPicture.bigPicture(getBitmapFromURL(extras.getString(PICTURE)));
      bigPicture.setBigContentTitle(fromHtml(extras.getString(TITLE)));
      bigPicture.setSummaryText(fromHtml(extras.getString(SUMMARY_TEXT)));

      builder.setContentTitle(fromHtml(extras.getString(TITLE)));
      builder.setContentText(fromHtml(message));

      builder.setStyle(bigPicture);
    } else {
      setNotification(notId, "");

      NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

      if (message != null) {
        builder.setContentText(fromHtml(message));

        bigText.bigText(fromHtml(message));
        bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));

        String summaryText = extras.getString(SUMMARY_TEXT);
        if (summaryText != null) {
          bigText.setSummaryText(fromHtml(summaryText));
        }

        builder.setStyle(bigText);
      }
    }
  }

  private void  setNotificationMessage(int notId, Bundle extras,
                                      Notification.Builder builder) {
    String message = extras.getString(MESSAGE);

    String style = extras.getString(STYLE, STYLE_TEXT);
    if (STYLE_INBOX.equals(style)) {
      setNotification(notId, message);

      builder.setContentText(fromHtml(message));

      ArrayList<String> messageList = getMessageList(notId);
      Integer sizeList = messageList.size();
      if (sizeList > 1) {
        String sizeListMessage = sizeList.toString();
        String stacking = sizeList + " more";
        if (extras.getString(SUMMARY_TEXT) != null) {
          stacking = extras.getString(SUMMARY_TEXT);
          stacking = stacking.replace("%n%", sizeListMessage);
        }
        Notification.InboxStyle notificationInbox = new Notification.InboxStyle()
                .setBigContentTitle(fromHtml(extras.getString(TITLE)))
                .setSummaryText(fromHtml(stacking));

        for (int i = messageList.size() - 1; i >= 0; i--) {
          notificationInbox.addLine(fromHtml(messageList.get(i)));
        }

        builder.setStyle(notificationInbox);
      } else {
        Notification.BigTextStyle bigText = new Notification.BigTextStyle();
        if (message != null) {
          bigText.bigText(fromHtml(message));
          bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));
          builder.setStyle(bigText);
        }
      }
    } else if (STYLE_PICTURE.equals(style)) {
      setNotification(notId, "");

      Notification.BigPictureStyle bigPicture = new Notification.BigPictureStyle();
      bigPicture.bigPicture(getBitmapFromURL(extras.getString(PICTURE)));
      bigPicture.setBigContentTitle(fromHtml(extras.getString(TITLE)));
      bigPicture.setSummaryText(fromHtml(extras.getString(SUMMARY_TEXT)));

      builder.setContentTitle(fromHtml(extras.getString(TITLE)));
      builder.setContentText(fromHtml(message));

      builder.setStyle(bigPicture);
    } else {
      setNotification(notId, "");

      Notification.BigTextStyle bigText = new Notification.BigTextStyle();

      if (message != null) {
        builder.setContentText(fromHtml(message));

        bigText.bigText(fromHtml(message));
        bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));

        String summaryText = extras.getString(SUMMARY_TEXT);
        if (summaryText != null) {
          bigText.setSummaryText(fromHtml(summaryText));
        }

        builder.setStyle(bigText);
      }
    }
  }

  private void setNotificationSound(Context context, Bundle extras,
                                    NotificationCompat.Builder builder) {
    String soundname = extras.getString(SOUNDNAME);
    if (soundname == null) {
      soundname = extras.getString(SOUND);
    }
    if (SOUND_RINGTONE.equals(soundname)) {
      builder.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI);
    } else if (soundname != null && !soundname.contentEquals(SOUND_DEFAULT)) {
      Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
          + "://" + context.getPackageName() + "/raw/" + soundname);
      Log.d(LOG_TAG, sound.toString());
      builder.setSound(sound);
    } else {
      builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
    }
  }

  @RequiresApi(api = 26)
  private void setNotificationSound(Context context, Bundle extras,
                                    NotificationChannel channel) {
    String soundname = extras.getString(SOUNDNAME);
    if (soundname == null) {
      soundname = extras.getString(SOUND);
    }
    if (SOUND_RINGTONE.equals(soundname)) {
      channel.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI,
              Notification.AUDIO_ATTRIBUTES_DEFAULT);
    } else if (soundname != null && !soundname.contentEquals(SOUND_DEFAULT)) {
      Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
              + "://" + context.getPackageName() + "/raw/" + soundname);
      Log.d(LOG_TAG, sound.toString());
      channel.setSound(sound, Notification.AUDIO_ATTRIBUTES_DEFAULT);
    } else {
      channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
              Notification.AUDIO_ATTRIBUTES_DEFAULT);
    }
  }

  private void setNotificationLedColor(Bundle extras, NotificationCompat.Builder builder) {
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
      builder.setLights(Color.argb(results[0], results[1], results[2], results[3]), 500, 500);
    } else {
      Log.e(LOG_TAG, "ledColor parameter must be an array of length == 4 (ARGB)");
    }

  }

  @RequiresApi(api = 26)
  private void setNotificationLedColor(Bundle extras, NotificationChannel channel) {
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
      channel.setLightColor(Color.argb(results[0], results[1], results[2], results[3]));
    } else {
      Log.e(LOG_TAG, "ledColor parameter must be an array of length == 4 (ARGB)");
    }

  }

  private void setNotificationPriority(Bundle extras, NotificationCompat.Builder builder) {
    String priorityStr = extras.getString(PRIORITY);
    if (priorityStr == null) {
      return;
    }

    try {
      Integer priority = Integer.parseInt(priorityStr);
      if (priority >= NotificationCompat.PRIORITY_MIN
          && priority <= NotificationCompat.PRIORITY_MAX) {
        builder.setPriority(priority);
      } else {
        Log.e(LOG_TAG, "Priority parameter must be between -2 and 2");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

  }

  @RequiresApi(api = 26)
  private void setNotificationImportance(Bundle extras, NotificationChannel channel) {
    String priorityStr = extras.getString(PRIORITY);
    if (priorityStr == null) {
      return;
    }

    try {
      Integer priority = Integer.parseInt(priorityStr);
      if (priority >= NotificationCompat.PRIORITY_MIN
              && priority <= NotificationCompat.PRIORITY_MAX) {
        channel.setImportance(priority);
      } else {
        Log.e(LOG_TAG, "Priority parameter must be between -2 and 2");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

  }

  private void setNotificationLargeIcon(Context context, Bundle extras, String packageName,
                                        Resources resources, NotificationCompat.Builder builder) {
    String gcmLargeIcon = extras.getString(IMAGE); // from gcm
    if (gcmLargeIcon == null || "".equals(gcmLargeIcon)) {
      return;
    }

    if (gcmLargeIcon.startsWith("http://") || gcmLargeIcon.startsWith("https://")) {
      builder.setLargeIcon(getBitmapFromURL(gcmLargeIcon));
      Log.d(LOG_TAG, "using remote large-icon from gcm");
    } else {
      AssetManager assetManager = context.getAssets();
      InputStream istr;
      try {
        istr = assetManager.open(gcmLargeIcon);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        builder.setLargeIcon(bitmap);
        Log.d(LOG_TAG, "using assets large-icon from gcm");
      } catch (IOException e) {
        int largeIconId = resources.getIdentifier(gcmLargeIcon, DRAWABLE, packageName);
        if (largeIconId != 0) {
          Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
          builder.setLargeIcon(largeIconBitmap);
          Log.d(LOG_TAG, "using resources large-icon from gcm");
        } else {
          Log.d(LOG_TAG, "Not setting large icon");
        }
      }
    }
  }

  private void setNotificationLargeIcon(Context context, Bundle extras, String packageName,
                                        Resources resources, Notification.Builder builder) {
    String gcmLargeIcon = extras.getString(IMAGE); // from gcm
    if (gcmLargeIcon == null || "".equals(gcmLargeIcon)) {
      return;
    }

    if (gcmLargeIcon.startsWith("http://") || gcmLargeIcon.startsWith("https://")) {
      builder.setLargeIcon(getBitmapFromURL(gcmLargeIcon));
      Log.d(LOG_TAG, "using remote large-icon from gcm");
    } else {
      AssetManager assetManager = context.getAssets();
      InputStream istr;
      try {
        istr = assetManager.open(gcmLargeIcon);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        builder.setLargeIcon(bitmap);
        Log.d(LOG_TAG, "using assets large-icon from gcm");
      } catch (IOException e) {
        int largeIconId = resources.getIdentifier(gcmLargeIcon, DRAWABLE, packageName);
        if (largeIconId != 0) {
          Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
          builder.setLargeIcon(largeIconBitmap);
          Log.d(LOG_TAG, "using resources large-icon from gcm");
        } else {
          Log.d(LOG_TAG, "Not setting large icon");
        }
      }
    }
  }


  private void setNotificationSmallIcon(Bundle extras, String packageName,
                                        Resources resources, NotificationCompat.Builder builder,
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
    builder.setSmallIcon(iconId);
  }

  private void setNotificationSmallIcon(Bundle extras, String packageName,
                                        Resources resources, Notification.Builder builder,
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
    builder.setSmallIcon(iconId);
  }

  private void setNotificationIconColor(String color, NotificationCompat.Builder builder,
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
      builder.setColor(iconColor);
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
