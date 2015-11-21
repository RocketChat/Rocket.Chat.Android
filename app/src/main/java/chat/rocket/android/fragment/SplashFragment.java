package chat.rocket.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.api.Auth;
import chat.rocket.android.api.RocketChatRestAPI;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;
import ollie.query.Delete;
import ollie.query.Select;

public class SplashFragment extends Fragment {

    public SplashFragment() {}

    private ConstrainedActionManager mShowMainActivityManager = new ConstrainedActionManager() {
        @Override
        protected void action() {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerConfig s = Select.from(ServerConfig.class).where("is_primary = 1").fetchSingle();

        if (s == null || TextUtils.isEmpty(s.authToken)) showServerConfigFragment();
        else tryGetRooms(s);
    }

    private void showServerConfigFragment(){
        getFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new ServerConfigFragment())
                .commit();
    }

    private void tryGetRooms(final ServerConfig config){
        new RocketChatRestAPI(config.hostname)
                .getPublicRooms(new Auth(config.account, config.authToken))
                .onSuccess(new Continuation<JSONArray, Object>() {
                    @Override
                    public Object then(Task<JSONArray> task) throws Exception {
                        Delete.from(Room.class).where("?=?", 1, 1).execute();
                        JSONArray rooms = task.getResult();
                        for(int i=0;i<rooms.length();i++){
                            JSONObject room = rooms.getJSONObject(i);
                            Room r = new Room();
                            r._id = room.getString("_id");
                            r.name = room.getString("name");
                            r.timestamp = room.getString("ts");
                            r.save();
                        }

                        mShowMainActivityManager.setShouldAction(true);
                        return null;
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        Exception e = task.getError();
                        if(e instanceof RocketChatRestAPI.AuthorizationRequired) {
                            config.delete();
                            showServerConfigFragment();
                        }
                        else Log.d(Constants.LOG_TAG, "error", task.getError());
                        return null;
                    }
                });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entry_screen, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mShowMainActivityManager.setConstrainedMet(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mShowMainActivityManager.setConstrainedMet(false);
    }
}
