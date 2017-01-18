package chat.rocket.android.api.rest;

import org.json.JSONObject;

import rx.Observable;

public interface ServerPolicyApi {

  String SECURE_PROTOCOL = "https://";
  String INSECURE_PROTOCOL = "http://";

  Observable<Response<JSONObject>> getApiInfoSecurely();

  Observable<Response<JSONObject>> getApiInfoInsecurely();
}
