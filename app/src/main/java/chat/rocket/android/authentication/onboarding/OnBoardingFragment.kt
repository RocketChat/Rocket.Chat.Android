package chat.rocket.android.authentication.onboarding


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.getLoginDeepLinkInfo
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.fragment_on_boarding.*

class OnBoardingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_on_boarding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_connect_server.setOnClickListener {
            val deepLinkInfo = activity?.intent?.getLoginDeepLinkInfo()
            (activity as AuthenticationActivity).addFragmentBackStack("ServerFragment", R.id.fragment_container) {
                            ServerFragment.newInstance(deepLinkInfo)
            }
        }
    }

    companion object {
        fun newInstance() = OnBoardingFragment()
    }


}
