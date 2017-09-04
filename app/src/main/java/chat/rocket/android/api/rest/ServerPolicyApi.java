package chat.rocket.android.api.rest;

import io.reactivex.Flowable;
import org.json.JSONObject;

public interface ServerPolicyApi {

  String SECURE_PROTOCOL = "https://";
  String INSECURE_PROTOCOL = "http://";

  Flowable<Response<JSONObject>> getApiInfoSecurely();

  Flowable<Response<JSONObject>> getApiInfoInsecurely();
}
