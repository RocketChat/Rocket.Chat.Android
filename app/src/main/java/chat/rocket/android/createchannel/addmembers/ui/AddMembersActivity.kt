package chat.rocket.android.createchannel.addmembers.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.createchannel.addmembers.presentation.AddMembersPresenter
import chat.rocket.android.createchannel.addmembers.presentation.AddMembersView
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.members.adapter.MembersAdapter
import chat.rocket.android.members.viewmodel.MemberViewModel
import chat.rocket.android.util.extensions.asObservable
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.widget.DividerItemDecoration
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_add_members.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddMembersActivity : AppCompatActivity(), AddMembersView {
    @Inject
    lateinit var presenter: AddMembersPresenter
    private var query: String = ""
    private var membersToAdd = arrayListOf<String>()
    private val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    private lateinit var editTextDisposable: Disposable
    private val adapter: MembersAdapter = MembersAdapter {
        it.username?.let {
            if (!membersToAdd.contains(it)) {
                buildChip(it)
                membersToAdd.add(it)
                updateToolBar()
            } else {
                showMessage(getString(R.string.msg_member_already_added))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)
        setupToolBar()
        setupRecyclerView()
        setupInitialChips()
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        subscribeEditText()
    }

    override fun onStop() {
        super.onStop()
        unsubscribeEditText()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun showLoading() {
        view_loading.isVisible = true
    }

    override fun hideLoading() {
        view_loading.isVisible = false
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    override fun showUsers(dataSet: List<MemberViewModel>, total: Long) {
        if (adapter.itemCount == 0) {
            adapter.prependData(dataSet)
            if (dataSet.size >= 30) {
                recycler_view.addOnScrollListener(object :
                    EndlessRecyclerViewScrollListener(linearLayoutManager) {
                    override fun onLoadMore(
                        page: Int,
                        totalItemsCount: Int,
                        recyclerView: RecyclerView?
                    ) {
                        presenter.searchUser(query)
                    }
                })
            }
        } else {
            adapter.appendData(dataSet)
        }
    }

    private fun setupToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.title_add_members, "0")
        toolbar_action_text.text = getString(R.string.action_select_members)
    }

    private fun setupRecyclerView() {
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(DividerItemDecoration(this))
    }

    private fun setupInitialChips() {
        membersToAdd = intent.getStringArrayListExtra("chips")
        for (element in membersToAdd) {
            buildChip(element)
        }
        updateToolBar()
    }

    private fun setListeners() {
        toolbar_action_text.setOnClickListener {
            if (it.isEnabled) {
                val intent = Intent()
                intent.putExtra("members", membersToAdd)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun subscribeEditText() {
        editTextDisposable = text_search_member.asObservable()
            .debounce(500, TimeUnit.MILLISECONDS)
            .filter { t -> t.isNotEmpty() && t != query }
            .subscribe {
                adapter.clearData()
                query = it.toString()
                presenter.searchUser(query)
            }
    }

    private fun unsubscribeEditText() {
        editTextDisposable.dispose()
    }

    private fun buildChip(chipText: String) {
        val chip = Chip(this)
        chip.chipText = chipText
        chip.isCloseIconEnabled = true
        chip.setChipBackgroundColorResource(R.color.icon_grey)
        chip.setOnCloseIconClickListener {
            members_chips.removeView(it)
            membersToAdd.remove((it as Chip).chipText.toString())
            updateToolBar()
        }
        members_chips.addView(chip)
    }

    private fun updateToolBar() {
        if (membersToAdd.isEmpty()) {
            toolbar_action_text.alpha = 0.8F
            toolbar_action_text.isEnabled = false
            members_chips.isVisible = false
        } else {
            toolbar_action_text.alpha = 1.0F
            toolbar_action_text.isEnabled = true
            members_chips.isVisible = true
        }
        toolbar_title.textContent =
                getString(R.string.title_add_members, membersToAdd.size.toString())
    }
}