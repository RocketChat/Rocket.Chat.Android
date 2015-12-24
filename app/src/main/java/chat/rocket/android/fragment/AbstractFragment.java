package chat.rocket.android.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;

public class AbstractFragment extends Fragment {

    protected @IdRes int getContainerId() {
        return R.id.activity_main_container;
    }

    private FragmentManager.OnBackStackChangedListener mBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            Fragment f = getFragmentManager().findFragmentById(getContainerId());
            if (f instanceof FragmentManager.OnBackStackChangedListener) {
                ((FragmentManager.OnBackStackChangedListener) f).onBackStackChanged();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public void onPause() {
        getFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
        super.onPause();
    }

    protected void finish(){
        if(getFragmentManager().getBackStackEntryCount()==0){
            getActivity().finish();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    protected AppCompatActivity getAppCompatActivity(){
        return (AppCompatActivity) getActivity();
    }

    protected ServerConfig getPrimaryServerConfig() {
        return RocketChatDatabaseHelper.read(getContext(), new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
            @Override
            public ServerConfig process(SQLiteDatabase db) {
                return ServerConfig.getPrimaryConfig(db);
            }
        });
    }

}
