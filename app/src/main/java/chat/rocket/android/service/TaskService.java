package chat.rocket.android.service;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

public class TaskService extends GcmTaskService {

    public static final String TAG_KEEP_ALIVE = "TAG_KEEP_ALIVE";

    @Override
    public int onRunTask(TaskParams taskParams) {
        switch (taskParams.getTag()) {
            case TAG_KEEP_ALIVE:
                ConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();
                return GcmNetworkManager.RESULT_SUCCESS;
            default:
                return GcmNetworkManager.RESULT_FAILURE;
        }
    }
}
