package chat.rocket.android.main.settings.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.wear.widget.WearableLinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainNavigator
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment() {
    @Inject
    lateinit var navigator: MainNavigator

    private lateinit var adapter: SettingsAdapter

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater!!.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        settings_list.layoutManager = WearableLinearLayoutManager(context)
        settings_list.itemAnimator = DefaultItemAnimator()
        settings_list.isCircularScrollingGestureEnabled = true
        settings_list.scrollDegreesPerScreen = 90f

        adapter = SettingsAdapter(context) { position ->
            when (position) {
                0 -> navigator.addAccountFragment()
            }
        }
    }
}