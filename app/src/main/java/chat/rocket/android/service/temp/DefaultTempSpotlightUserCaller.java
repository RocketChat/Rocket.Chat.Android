package chat.rocket.android.service.temp;

import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.core.temp.TempSpotlightUserCaller;

public class DefaultTempSpotlightUserCaller implements TempSpotlightUserCaller {

  private final MethodCallHelper methodCallHelper;

  public DefaultTempSpotlightUserCaller(MethodCallHelper methodCallHelper) {
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void search(String term) {
    methodCallHelper.searchSpotlightUsers(term);
  }
}
