package chat.rocket.android.fragment;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.api.Auth;
import chat.rocket.android.api.RocketChatRestAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;

public class SplashFragment extends AbstractFragment {

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

        ServerConfig s = getPrimaryServerConfig();

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
                .getPublicRooms(new Auth(config.authUserId, config.authToken))
                .onSuccess(new Continuation<JSONArray, Object>() {
                    @Override
                    public Object then(Task<JSONArray> task) throws Exception {
                        final JSONArray rooms = task.getResult();

                        RocketChatDatabaseHelper.write(getContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
                            @Override
                            public Object process(SQLiteDatabase db) throws JSONException {
                                Room.delete(db, null,null);
                                for (int i = 0; i < rooms.length(); i++) {
                                    JSONObject room = rooms.getJSONObject(i);
                                    Room r = new Room();
                                    r.id = room.getString("_id");
                                    r.name = room.getString("name");
                                    r.timestamp = room.getString("ts");
                                    r.put(db);
                                }

                                return null;
                            }
                        });

                        mShowMainActivityManager.setShouldAction(true);
                        return null;
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        Exception e = task.getError();
                        if (e instanceof RocketChatRestAPI.AuthorizationRequired) {
                            RocketChatDatabaseHelper.write(getContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
                                @Override
                                public Object process(SQLiteDatabase db) throws JSONException {
                                    config.delete(db);

                                    return null;
                                }
                            });
                            showServerConfigFragment();
                        } else {
                            showErrorFragment(task.getError().getMessage());
                        }
                        return null;
                    }
                });

    }

    private void showErrorFragment(String msg) {
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, LoginErrorFragment.create(msg))
                .commit();
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
