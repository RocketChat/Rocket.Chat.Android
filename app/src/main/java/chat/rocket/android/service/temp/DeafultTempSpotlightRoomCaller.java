package chat.rocket.android.service.temp;

import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.core.temp.TempSpotlightRoomCaller;

public class DeafultTempSpotlightRoomCaller implements TempSpotlightRoomCaller {

  private final MethodCallHelper methodCallHelper;

  public DeafultTempSpotlightRoomCaller(MethodCallHelper methodCallHelper) {
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void search(String term) {
    methodCallHelper.searchSpotlightRooms(term);
  }
}
