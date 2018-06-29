package chat.rocket.android.about.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    companion object {
        fun newInstance() = AboutFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_about, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupViews()
    }

    private fun setupViews() {
        text_version_name.text = getString(R.string.msg_version, BuildConfig.VERSION_NAME)
        text_build_number.text = getString(R.string.msg_build, BuildConfig.VERSION_CODE)
    }

    private fun setupToolbar() {
        val toolbar = (activity as MainActivity).toolbar
        toolbar.title = getString(R.string.title_about)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            this.activity?.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
    }
}
