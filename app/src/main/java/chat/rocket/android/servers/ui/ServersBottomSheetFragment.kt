package chat.rocket.android.servers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.servers.adapter.Selector
import chat.rocket.android.servers.adapter.ServersAdapter
import chat.rocket.android.servers.presentation.ServersPresenter
import chat.rocket.android.servers.presentation.ServersView
import chat.rocket.android.util.extensions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.bottom_sheet_fragment_servers.*
import javax.inject.Inject

const val TAG = "ServersBottomSheetFragment"

class ServersBottomSheetFragment : BottomSheetDialogFragment(), ServersView {
    @Inject
    lateinit var presenter: ServersPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.bottom_sheet_fragment_servers, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getAllServers()
    }

    override fun showServerList(serverList: List<Account>, currentServerUrl: String) {
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = ServersAdapter(serverList, currentServerUrl, object : Selector {
            override fun onServerSelected(serverUrl: String) {
                presenter.changeServer(serverUrl)
            }

            override fun onAddNewServerSelected() {
                presenter.addNewServer()
            }
        })
    }

    override fun hideServerView() = dismiss()

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))
}