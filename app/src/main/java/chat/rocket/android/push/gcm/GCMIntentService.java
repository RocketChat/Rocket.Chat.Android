package chat.rocket.android.push.gcm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import chat.rocket.android.push.PushConstants;
import chat.rocket.android.push.PushManager;

@SuppressLint("NewApi")
public class GCMIntentService extends GcmListenerService implements PushConstants {

  private static final String LOG_TAG = "GCMIntentService";

  @Override
  public void onMessageReceived(String from, Bundle extras) {
    Log.d(LOG_TAG, "onMessage - from: " + from);

    if (extras == null) {
      return;
    }

    Context applicationContext = getApplicationContext();

    extras = normalizeExtras(applicationContext, extras);

    PushManager.INSTANCE.handle(applicationContext, extras);
  }

  /*
   * Change a values key in the extras bundle
   */
  private void replaceKey(Context context, String oldKey, String newKey, Bundle extras,
                          Bundle newExtras) {
    Object value = extras.get(oldKey);
    if (value == null) {
      return;
    }

    if (value instanceof String) {
      value = localizeKey(context, newKey, (String) value);

      newExtras.putString(newKey, (String) value);
    } else if (value instanceof Boolean) {
      newExtras.putBoolean(newKey, (Boolean) value);
    } else if (value instanceof Number) {
      newExtras.putDouble(newKey, ((Number) value).doubleValue());
    } else {
      newExtras.putString(newKey, String.valueOf(value));
    }
  }

  /*
   * Normalize localization for key
   */
  private String localizeKey(Context context, String key, String value) {
    if (key.equals(TITLE) || key.equals(MESSAGE) || key.equals(SUMMARY_TEXT)) {
      try {
        JSONObject localeObject = new JSONObject(value);

        String localeKey = localeObject.getString(LOC_KEY);

        ArrayList<String> localeFormatData = new ArrayList<>();
        if (!localeObject.isNull(LOC_DATA)) {
          String localeData = localeObject.getString(LOC_DATA);
          JSONArray localeDataArray = new JSONArray(localeData);
          for (int i = 0, size = localeDataArray.length(); i < size; i++) {
            localeFormatData.add(localeDataArray.getString(i));
          }
        }

        String packageName = context.getPackageName();
        Resources resources = context.getResources();

        int resourceId = resources.getIdentifier(localeKey, "string", packageName);

        if (resourceId != 0) {
          return resources.getString(resourceId, localeFormatData.toArray());
        } else {
          Log.d(LOG_TAG, "can't find resource for locale key = " + localeKey);

          return value;
        }
      } catch (JSONException e) {
        Log.d(LOG_TAG, "no locale found for key = " + key + ", error " + e.getMessage());

        return value;
      }
    }

    return value;
  }

  /*
   * Replace alternate keys with our canonical value
   */
  private String normalizeKey(String key) {
    if (key.equals(BODY) || key.equals(ALERT) || key.equals(GCM_NOTIFICATION_BODY) || key
        .equals(TWILIO_BODY)) {
      return MESSAGE;
    } else if (key.equals(TWILIO_TITLE)) {
      return TITLE;
    } else if (key.equals(MSGCNT) || key.equals(BADGE)) {
      return COUNT;
    } else if (key.equals(SOUNDNAME) || key.equals(TWILIO_SOUND)) {
      return SOUND;
    } else if (key.startsWith(GCM_NOTIFICATION)) {
      return key.substring(GCM_NOTIFICATION.length() + 1, key.length());
    } else if (key.startsWith(GCM_N)) {
      return key.substring(GCM_N.length() + 1, key.length());
    } else if (key.startsWith(UA_PREFIX)) {
      key = key.substring(UA_PREFIX.length() + 1, key.length());
      return key.toLowerCase();
    } else {
      return key;
    }
  }

  /*
   * Parse bundle into normalized keys.
   */
  private Bundle normalizeExtras(Context context, Bundle extras) {
    Log.d(LOG_TAG, "normalize extras");
    Iterator<String> keyIterator = extras.keySet().iterator();
    Bundle newExtras = new Bundle();

    while (keyIterator.hasNext()) {
      String key = keyIterator.next();

      Log.d(LOG_TAG, "key = " + key);

      // If normalizeKeythe key is "data" or "message" and the value is a json object extract
      // This is to support parse.com and other services. Issue #147 and pull #218
      if (key.equals(PARSE_COM_DATA) || key.equals(MESSAGE)) {
        Object json = extras.get(key);
        // Make sure data is json object stringified
        if (json instanceof String && ((String) json).startsWith("{")) {
          Log.d(LOG_TAG, "extracting nested message data from key = " + key);
          try {
            // If object contains message keys promote each value to the root of the bundle
            JSONObject data = new JSONObject((String) json);
            if (data.has(ALERT) || data.has(MESSAGE) || data.has(BODY) || data.has(TITLE)) {
              Iterator<String> jsonIter = data.keys();
              while (jsonIter.hasNext()) {
                String jsonKey = jsonIter.next();

                Log.d(LOG_TAG, "key = data/" + jsonKey);

                String value = data.getString(jsonKey);
                jsonKey = normalizeKey(jsonKey);
                value = localizeKey(context, jsonKey, value);

                newExtras.putString(jsonKey, value);
              }
            }
          } catch (JSONException e) {
            Log.e(LOG_TAG, "normalizeExtras: JSON exception");
          }
        }
      } else if (key.equals(("notification"))) {
        Bundle value = extras.getBundle(key);
        Iterator<String> iterator = value.keySet().iterator();
        while (iterator.hasNext()) {
          String notifkey = iterator.next();

          Log.d(LOG_TAG, "notifkey = " + notifkey);
          String newKey = normalizeKey(notifkey);
          Log.d(LOG_TAG, "replace key " + notifkey + " with " + newKey);

          String valueData = value.getString(notifkey);
          valueData = localizeKey(context, newKey, valueData);

          newExtras.putString(newKey, valueData);
        }
        continue;
      }

      String newKey = normalizeKey(key);
      Log.d(LOG_TAG, "replace key " + key + " with " + newKey);
      replaceKey(context, key, newKey, extras, newExtras);

    } // while

    return newExtras;
  }

  public static String getAppName(Context context) {
    CharSequence appName = context.getPackageManager()
        .getApplicationLabel(context.getApplicationInfo());
    return (String) appName;
  }
}