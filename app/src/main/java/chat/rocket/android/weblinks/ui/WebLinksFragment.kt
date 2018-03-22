package chat.rocket.android.weblinks.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.room.weblink.WebLinkEntity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.weblinks.presentation.WebLinksPresenter
import chat.rocket.android.weblinks.presentation.WebLinksView
import chat.rocket.android.webview.weblink.ui.webViewIntent
import chat.rocket.android.widget.DividerItemDecoration
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_web_links.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class WebLinksFragment : Fragment(), WebLinksView {
    @Inject
    lateinit var presenter: WebLinksPresenter

    private var listJob: Job? = null

    companion object {
        fun newInstance() = WebLinksFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_web_links)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadWebLinks()
    }

    private fun setupToolbar() {
        (activity as MainActivity).toolbar.title = getString(R.string.title_web_links)
    }

    private fun setupRecyclerView() {
        activity?.apply {
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.addItemDecoration(DividerItemDecoration(this,
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end),
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)))
            recycler_view.itemAnimator = DefaultItemAnimator()
            // TODO - use a ViewModel Mapper instead of using settings on the adapter
            recycler_view.adapter = WebLinksAdapter(this,
                    { webLink ->
                        run {
                            startActivity(this.webViewIntent(webLink.link, if (!webLink.title.isEmpty()) webLink.title else resources.getString(R.string.url_preview_title)))
                        }
                    })
        }
    }

    override suspend fun updateWebLinks(newDataSet: List<WebLinkEntity>) {
        activity?.apply {
            listJob?.cancel()
            listJob = launch(UI) {
                val adapter = recycler_view.adapter as WebLinksAdapter
                if (isActive) {
                    adapter.updateWebLinks(newDataSet)
                }
            }
        }
    }

    override fun showNoWebLinksToDisplay() {
        text_no_data_to_display.setVisible(true)
    }
}