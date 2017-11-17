package chat.rocket.android.api.rest;

import org.json.JSONObject;

import io.reactivex.Flowable;

public interface ServerPolicyApi {

  String SECURE_PROTOCOL = "https://";
  String INSECURE_PROTOCOL = "http://";

  Flowable<Response<JSONObject>> getApiInfoSecurely();

  Flowable<Response<JSONObject>> getApiInfoInsecurely();
}
