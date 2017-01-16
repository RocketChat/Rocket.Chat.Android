package chat.rocket.android.helper;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import chat.rocket.android.api.rest.ServerPolicyApi;

public class ServerPolicyApiValidationHelper {

  public static void getApiVersion(@NonNull ServerPolicyApi serverPolicyApi,
                                   @NonNull Callback callback) {
    trySecurely(serverPolicyApi, new Callback() {
      @Override
      public void onSuccess(boolean usesSecureConnection, JSONObject apiInfo) {
        callback.onSuccess(usesSecureConnection, apiInfo);
      }

      @Override
      public void onResponseError() {
        callback.onResponseError();
      }

      @Override
      public void onNetworkError() {
        tryInsecurely(serverPolicyApi, callback);
      }
    });
  }

  private static void trySecurely(@NonNull ServerPolicyApi serverPolicyApi,
                                  @NonNull Callback callback) {
    serverPolicyApi.getApiInfoSecurely(getServerPolicyApiCallback(true, callback));
  }

  private static void tryInsecurely(@NonNull ServerPolicyApi serverPolicyApi,
                                    @NonNull Callback callback) {
    serverPolicyApi.getApiInfoInsecurely(getServerPolicyApiCallback(false, callback));
  }

  private static ServerPolicyApi.Callback getServerPolicyApiCallback(boolean isSecureConnection,
                                                                     @NonNull Callback callback) {
    return new ServerPolicyApi.Callback() {
      @Override
      public void onSuccess(JSONObject jsonObject) {
        callback.onSuccess(isSecureConnection, jsonObject);
      }

      @Override
      public void onResponseError() {
        callback.onResponseError();
      }

      @Override
      public void onNetworkError() {
        callback.onNetworkError();
      }
    };
  }

  interface Callback {
    void onSuccess(boolean usesSecureConnection, JSONObject apiInfo);

    void onResponseError();

    void onNetworkError();
  }
}
