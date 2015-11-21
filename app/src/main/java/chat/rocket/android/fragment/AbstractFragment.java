package chat.rocket.android.fragment;

import android.support.v4.app.Fragment;

public class AbstractFragment extends Fragment {
    protected void finish(){
        if(getFragmentManager().getBackStackEntryCount()==0){
            getActivity().finish();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }
}
