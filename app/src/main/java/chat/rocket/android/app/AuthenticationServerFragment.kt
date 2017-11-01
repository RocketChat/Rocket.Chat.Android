package chat.rocket.android.app

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import chat.rocket.android.R
import kotlinx.android.synthetic.main.fragment_authentication_server.*

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
class AuthenticationServerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_authentication_server, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_server_url.setSelection(text_server_url.length())

        val window = activity.window
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}