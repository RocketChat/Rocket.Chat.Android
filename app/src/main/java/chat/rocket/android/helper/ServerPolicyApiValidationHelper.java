package chat.rocket.android.helper;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;

import chat.rocket.android.api.rest.ServerPolicyApi;

public class ServerPolicyApiValidationHelper {

  private final ServerPolicyApi serverPolicyApi;

  public ServerPolicyApiValidationHelper(@NonNull ServerPolicyApi serverPolicyApi) {
    this.serverPolicyApi = serverPolicyApi;
  }

  public Flowable<ServerPolicyHelper.ServerInfoResponse> getApiVersion() {
    return serverPolicyApi.getApiInfoSecurely()
        .onErrorResumeNext(serverPolicyApi.getApiInfoInsecurely())
        .map(response -> new ServerPolicyHelper.ServerInfoResponse(
            response.getProtocol().equals(ServerPolicyApi.SECURE_PROTOCOL),
            response.getData()
        ));
  }
}
