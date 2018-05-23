package chat.rocket.android.createChannel.addMembers.ui

import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.EditText
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.createChannel.addMembers.presentation.AddMembersPresenter
import chat.rocket.android.createChannel.addMembers.presentation.AddMembersView
import chat.rocket.android.members.adapter.MembersAdapter
import chat.rocket.android.members.viewmodel.MemberViewModel
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.widget.DividerItemDecoration
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_add_members.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddMembersActivity : AppCompatActivity(), AddMembersView {

    @Inject
    lateinit var presenter: AddMembersPresenter
    private var membersToAdd: ArrayList<String> = ArrayList()

    private val adapter: MembersAdapter = MembersAdapter { memberViewModel ->
        addNewChip(memberViewModel)
        updateToolBar()
        search_view.setText("")
    }
    private var linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)
        setUpToolBar()
        setUpRecyclerView()
        observableFromSearchView(search_view)
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { item -> item.length > 1 }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                run {
                    adapter.reAllocateArrayList()
                    presenter.queryUsersFromRegex(query)
                }
            }
    }

    private fun addNewChip(memberViewModel: MemberViewModel) {
        val memberChip = Chip(this)
        memberChip.chipText = memberViewModel.username
        memberChip.isCloseIconEnabled = true
        memberChip.setChipBackgroundColorResource(R.color.icon_grey)
        memberChip.setOnCloseIconClickListener { view ->
            members_chips.removeView(view)
            membersToAdd.remove((view as Chip).chipText.toString())
            updateToolBar()
        }
        members_chips.addView(memberChip)
        memberViewModel.username?.let { membersToAdd.add(it) }
        Toast.makeText(this, membersToAdd.size.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun updateToolBar() {
        toolbar_title.text = getString(R.string.title_add_members, membersToAdd.size)
    }

    override fun showMembers(dataSet: List<MemberViewModel>, total: Long) {
        if (adapter.itemCount == 0) {
            adapter.prependData(dataSet)
//                if (dataSet.size >= 59) { // TODO Check why the API retorns the specified count -1
//                    search_results.addOnScrollListener(object :
//                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
//                        override fun onLoadMore(
//                            page: Int,
//                            totalItemsCount: Int,
//                            recyclerView: RecyclerView?
//                        ) {
//                            presenter.loadChatRoomsMembers(chatRoomId, chatRoomType, page * 60L)
//                        }
//                    })
//                }
        } else {
            adapter.appendData(dataSet)
        }

    }

    override fun showLoading() {
        progress_bar.setVisible(true)
    }

    override fun hideLoading() {
        progress_bar.setVisible(false)
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


    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.title_add_members, 0)
        toolbar_action_text.text = getString(R.string.action_select_members)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setUpRecyclerView() {
        search_results.layoutManager = linearLayoutManager
        search_results.adapter = adapter
        search_results.addItemDecoration(DividerItemDecoration(this))

    }

    private fun observableFromSearchView(searchView: EditText): Observable<String> {
        val observableSubject: BehaviorSubject<String> = BehaviorSubject.create()
        RxTextView.textChanges(searchView).subscribe { text ->
            if (text.isNotBlank()) {
                observableSubject.onNext(text.toString())
            }
        }
        return observableSubject
    }
}