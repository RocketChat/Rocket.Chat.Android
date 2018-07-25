package chat.rocket.android.authentication.loginoptions.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R


class LoginOptionsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_options, container, false)
    }

    companion object {
        fun newInstance() = LoginOptionsFragment()
    }

}
