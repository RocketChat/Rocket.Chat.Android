package chat.rocket.android.fragment.chatroom;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.activity.room.RoomActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.rocket.android.fragment.sidebar.SidebarMainFragment;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.FileUploadHelper;
import chat.rocket.android.helper.LoadMoreScrollListener;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OnBackPressListener;
import chat.rocket.android.helper.RecyclerViewAutoScrollManager;
import chat.rocket.android.helper.RecyclerViewScrolledToBottomListener;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.chatroom.AbstractNewMessageIndicatorManager;
import chat.rocket.android.layouthelper.chatroom.MessageFormManager;
import chat.rocket.android.layouthelper.chatroom.MessageListAdapter;
import chat.rocket.android.layouthelper.chatroom.MessagePopup;
import chat.rocket.android.layouthelper.chatroom.ModelListAdapter;
import chat.rocket.android.layouthelper.chatroom.PairedMessage;
import chat.rocket.android.layouthelper.extra_action.AbstractExtraActionItem;
import chat.rocket.android.layouthelper.extra_action.MessageExtraActionBehavior;
import chat.rocket.android.layouthelper.extra_action.upload.AbstractUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.AudioUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.ImageUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.VideoUploadActionItem;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.renderer.RocketChatUserStatusProvider;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.service.temp.DeafultTempSpotlightRoomCaller;
import chat.rocket.android.service.temp.DefaultTempSpotlightUserCaller;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RoomToolbar;
import chat.rocket.android.widget.internal.ExtraActionPickerDialogFragment;
import chat.rocket.android.widget.message.MessageFormLayout;
import chat.rocket.android.widget.message.autocomplete.AutocompleteManager;
import chat.rocket.android.widget.message.autocomplete.channel.ChannelSource;
import chat.rocket.android.widget.message.autocomplete.user.UserSource;
import chat.rocket.core.interactors.AutocompleteChannelInteractor;
import chat.rocket.core.interactors.AutocompleteUserInteractor;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Chat room screen.
 */
@RuntimePermissions
public class RoomFragment extends AbstractChatRoomFragment implements
        OnBackPressListener,
        ExtraActionPickerDialogFragment.Callback,
        ModelListAdapter.OnItemClickListener<PairedMessage>,
        RoomContract.View {

    private static final int DIALOG_ID = 1;
    private static final String HOSTNAME = "hostname";
    private static final String ROOM_ID = "roomId";

    private String hostname;
    private String token;
    private String userId;
    private String roomId;
    private String roomType;
    private LoadMoreScrollListener scrollListener;
    private MessageFormManager messageFormManager;
    private RecyclerView messageRecyclerView;
    private RecyclerViewAutoScrollManager recyclerViewAutoScrollManager;
    protected AbstractNewMessageIndicatorManager newMessageIndicatorManager;
    protected Snackbar unreadIndicator;
    private boolean previousUnreadMessageExists;
    private MessageListAdapter messageListAdapter;
    private AutocompleteManager autocompleteManager;

    private List<AbstractExtraActionItem> extraActionItems;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected RoomContract.Presenter presenter;

    private RealmRoomRepository roomRepository;
    private RealmUserRepository userRepository;
    private MethodCallHelper methodCallHelper;
    private AbsoluteUrlHelper absoluteUrlHelper;

    private Message edittingMessage = null;

    private RoomToolbar toolbar;

    private SlidingPaneLayout pane;
    private SidebarMainFragment sidebarFragment;

    public RoomFragment() {
    }

    /**
     * build fragment with roomId.
     */
    public static RoomFragment create(String hostname, String roomId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(ROOM_ID, roomId);

        RoomFragment fragment = new RoomFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        hostname = args.getString(HOSTNAME);
        roomId = args.getString(ROOM_ID);

        roomRepository = new RealmRoomRepository(hostname);

        MessageInteractor messageInteractor = new MessageInteractor(
                new RealmMessageRepository(hostname),
                roomRepository
        );

        userRepository = new RealmUserRepository(hostname);

        absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );

        methodCallHelper = new MethodCallHelper(getContext(), hostname);

        presenter = new RoomPresenter(
                roomId,
                userRepository,
                messageInteractor,
                roomRepository,
                absoluteUrlHelper,
                methodCallHelper,
                ConnectivityManager.getInstance(getContext())
        );

        if (savedInstanceState == null) {
            presenter.loadMessages();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_room;
    }

    @Override
    protected void onSetupView() {
        pane = getActivity().findViewById(R.id.sliding_pane);
        messageRecyclerView = rootView.findViewById(R.id.messageRecyclerView);

        messageListAdapter = new MessageListAdapter(getContext(), hostname);
        messageRecyclerView.setAdapter(messageListAdapter);
        messageListAdapter.setOnItemClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        recyclerViewAutoScrollManager = new RecyclerViewAutoScrollManager(linearLayoutManager) {
            @Override
            protected void onAutoScrollMissed() {
                if (newMessageIndicatorManager != null) {
                    presenter.onUnreadCount();
                }
            }
        };
        messageListAdapter.registerAdapterDataObserver(recyclerViewAutoScrollManager);

        scrollListener = new LoadMoreScrollListener(linearLayoutManager, 40) {
            @Override
            public void requestMoreItem() {
                presenter.loadMoreMessages();
            }
        };
        messageRecyclerView.addOnScrollListener(scrollListener);
        messageRecyclerView.addOnScrollListener(new RecyclerViewScrolledToBottomListener(linearLayoutManager, 1, this::markAsReadIfNeeded));

        newMessageIndicatorManager = new AbstractNewMessageIndicatorManager() {
            @Override
            protected void onShowIndicator(int count, boolean onlyAlreadyShown) {
                if ((onlyAlreadyShown && unreadIndicator != null && unreadIndicator.isShown()) || !onlyAlreadyShown) {
                    unreadIndicator = getUnreadCountIndicatorView(count);
                    unreadIndicator.show();
                }
            }

            @Override
            protected void onHideIndicator() {
                if (unreadIndicator != null && unreadIndicator.isShown()) {
                    unreadIndicator.dismiss();
                }
            }
        };

        setupToolbar();
        setupSidebar();
        setupMessageComposer();
        setupMessageActions();
    }

    private void setupMessageActions() {
        extraActionItems = new ArrayList<>(3); // fixed number as of now
        extraActionItems.add(new ImageUploadActionItem());
        extraActionItems.add(new AudioUploadActionItem());
        extraActionItems.add(new VideoUploadActionItem());
    }

    private void scrollToLatestMessage() {
        if (messageRecyclerView != null)
            messageRecyclerView.scrollToPosition(0);
    }

    protected Snackbar getUnreadCountIndicatorView(int count) {
        // TODO: replace with another custom View widget, not to hide message composer.
        final String caption = getResources().getQuantityString(
                R.plurals.fmt_dialog_view_latest_message_title, count, count);

        return Snackbar.make(rootView, caption, Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_view_latest_message_action, view -> scrollToLatestMessage());
    }

    @Override
    public void onDestroyView() {
        RecyclerView.Adapter adapter = messageRecyclerView.getAdapter();
        if (adapter != null)
            adapter.unregisterAdapterDataObserver(recyclerViewAutoScrollManager);

        compositeDisposable.clear();

        if (autocompleteManager != null) {
            autocompleteManager.dispose();
            autocompleteManager = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onItemClick(PairedMessage pairedMessage) {
        presenter.onMessageSelected(pairedMessage.target);
    }

    private void setupToolbar() {
        toolbar = getActivity().findViewById(R.id.activity_main_toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_room);

        toolbar.setNavigationOnClickListener(view -> {
            if (pane.isSlideable() && !pane.isOpen()) {
                pane.openPane();
            }
        });

        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_pinned_messages:
                    showRoomListFragment(R.id.action_pinned_messages);
                    break;
                case R.id.action_favorite_messages:
                    showRoomListFragment(R.id.action_favorite_messages);
                    break;
//                case R.id.action_file_list:
//                    showRoomListFragment(R.id.action_file_list);
//                    break;
                case R.id.action_member_list:
                    showRoomListFragment(R.id.action_member_list);
                    break;
                default:
                    return super.onOptionsItemSelected(menuItem);
            }
            return true;
        });
    }

    private void setupSidebar() {
        SlidingPaneLayout subPane = getActivity().findViewById(R.id.sub_sliding_pane);
        sidebarFragment = (SidebarMainFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.sidebar_fragment_container);

        if (pane != null) {
            pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {
                    messageFormManager.enableComposingText(false);
                    sidebarFragment.clearSearchViewFocus();
                    //Ref: ActionBarDrawerToggle#setProgress
                    toolbar.setNavigationIconProgress(v);
                }

                @Override
                public void onPanelOpened(View view) {
                    toolbar.setNavigationIconVerticalMirror(true);
                }

                @Override
                public void onPanelClosed(View view) {
                    messageFormManager.enableComposingText(true);
                    toolbar.setNavigationIconVerticalMirror(false);
                    subPane.closePane();
                    closeUserActionContainer();
                }
            });
        }
    }

    public void closeUserActionContainer() {
        sidebarFragment.closeUserActionContainer();
    }

    private void setupMessageComposer() {
        final MessageFormLayout messageFormLayout = rootView.findViewById(R.id.messageComposer);
        messageFormManager = new MessageFormManager(messageFormLayout, this::showExtraActionSelectionDialog);
        messageFormManager.setSendMessageCallback(this::sendMessage);
        messageFormLayout.setEditTextCommitContentListener(this::onCommitContent);

        autocompleteManager = new AutocompleteManager(rootView.findViewById(R.id.messageListRelativeLayout));

        autocompleteManager.registerSource(
                new ChannelSource(
                        new AutocompleteChannelInteractor(
                                roomRepository,
                                new RealmSpotlightRoomRepository(hostname),
                                new DeafultTempSpotlightRoomCaller(methodCallHelper)
                        ),
                        AndroidSchedulers.from(BackgroundLooper.get()),
                        AndroidSchedulers.mainThread()
                )
        );

        Disposable disposable = Single.zip(
                absoluteUrlHelper.getRocketChatAbsoluteUrl(),
                roomRepository.getById(roomId).first(Optional.absent()),
                Pair::create
        )
                .subscribe(
                        pair -> {
                            if (pair.first.isPresent() && pair.second.isPresent()) {
                                autocompleteManager.registerSource(
                                        new UserSource(
                                                new AutocompleteUserInteractor(
                                                        pair.second.get(),
                                                        userRepository,
                                                        new RealmMessageRepository(hostname),
                                                        new RealmSpotlightUserRepository(hostname),
                                                        new DefaultTempSpotlightUserCaller(methodCallHelper)
                                                ),
                                                pair.first.get(),
                                                RocketChatUserStatusProvider.INSTANCE,
                                                AndroidSchedulers.from(BackgroundLooper.get()),
                                                AndroidSchedulers.mainThread()
                                        )
                                );
                            }
                        },
                        throwable -> {
                        }
                );

        compositeDisposable.add(disposable);

        autocompleteManager.bindTo(
                messageFormLayout.getEditText(),
                messageFormLayout
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != AbstractUploadActionItem.RC_UPL || resultCode != Activity.RESULT_OK) {
            return;
        }

        if (data == null || data.getData() == null) {
            return;
        }

        uploadFile(data.getData());
    }

    private void uploadFile(Uri uri) {
        String uplId = new FileUploadHelper(getContext(), RealmStore.get(hostname))
                .requestUploading(roomId, uri);
        if (!TextUtils.isEmpty(uplId)) {
            FileUploadProgressDialogFragment.create(hostname, roomId, uplId)
                    .show(getFragmentManager(), "FileUploadProgressDialogFragment");
        } else {
            // show error.
        }
    }

    private void markAsReadIfNeeded() {
        presenter.onMarkAsRead();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    public void onPause() {
        presenter.release();
        super.onPause();
    }

    private void showExtraActionSelectionDialog() {
        final DialogFragment fragment = ExtraActionPickerDialogFragment
                .create(new ArrayList<>(extraActionItems));
        fragment.setTargetFragment(this, DIALOG_ID);
        fragment.show(getFragmentManager(), "ExtraActionPickerDialogFragment");
    }

    @Override
    public void onItemSelected(int itemId) {
        for (AbstractExtraActionItem extraActionItem : extraActionItems) {
            if (extraActionItem.getItemId() == itemId) {
                RoomFragmentPermissionsDispatcher.onExtraActionSelectedWithCheck(RoomFragment.this, extraActionItem);
                return;
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (edittingMessage != null) {
            edittingMessage = null;
            messageFormManager.clearComposingText();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RoomFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    protected void onExtraActionSelected(MessageExtraActionBehavior action) {
        action.handleItemSelectedOnFragment(RoomFragment.this);
    }

    private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                    Bundle opts, String[] supportedMimeTypes) {
        boolean supported = false;
        for (final String mimeType : supportedMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            return false;
        }

        if (BuildCompat.isAtLeastNMR1()
                && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                return false;
            }
        }

        Uri linkUri = inputContentInfo.getLinkUri();
        if (linkUri == null) {
            return false;
        }

        sendMessage(linkUri.toString());

        try {
            inputContentInfo.releasePermission();
        } catch (Exception e) {
            RCLog.e(e);
            Logger.report(e);
        }

        return true;
    }

    private void sendMessage(String messageText) {
        if (edittingMessage == null) {
            presenter.sendMessage(messageText);
        } else {
            presenter.updateMessage(edittingMessage, messageText);
        }
    }

    @Override
    public void setupWith(RocketChatAbsoluteUrl rocketChatAbsoluteUrl) {
        if (rocketChatAbsoluteUrl != null) {
            token = rocketChatAbsoluteUrl.getToken();
            userId = rocketChatAbsoluteUrl.getUserId();
            messageListAdapter.setAbsoluteUrl(rocketChatAbsoluteUrl);
        }
    }

    @Override
    public void render(Room room) {
        roomType = room.getType();
        setToolbarTitle(room.getName());

        boolean unreadMessageExists = room.isAlert();
        if (newMessageIndicatorManager != null && previousUnreadMessageExists && !unreadMessageExists) {
            newMessageIndicatorManager.reset();
        }
        previousUnreadMessageExists = unreadMessageExists;

        if (room.isChannel()) {
            showToolbarPublicChannelIcon();
            return;
        }

        if (room.isPrivate()) {
            showToolbarPrivateChannelIcon();
        }

        if (room.isLivechat()) {
            showToolbarLivechatChannelIcon();
        }
    }

    @Override
    public void showUserStatus(User user) {
        showToolbarUserStatuslIcon(user.getStatus());
    }

    @Override
    public void updateHistoryState(boolean hasNext, boolean isLoaded) {
        if (messageRecyclerView == null || !(messageRecyclerView.getAdapter() instanceof MessageListAdapter)) {
            return;
        }

        MessageListAdapter adapter = (MessageListAdapter) messageRecyclerView.getAdapter();
        if (isLoaded) {
            scrollListener.setLoadingDone();
        }
        adapter.updateFooter(hasNext, isLoaded);
    }

    @Override
    public void onMessageSendSuccessfully() {
        scrollToLatestMessage();
        messageFormManager.onMessageSend();
        edittingMessage = null;
    }

    @Override
    public void disableMessageInput() {
        messageFormManager.enableComposingText(false);
    }

    @Override
    public void enableMessageInput() {
        messageFormManager.enableComposingText(true);
    }

    @Override
    public void showUnreadCount(int count) {
        newMessageIndicatorManager.updateNewMessageCount(count);
    }

    @Override
    public void showMessages(List<Message> messages) {
        if (messageListAdapter == null) {
            return;
        }
        messageListAdapter.updateData(messages);
    }

    @Override
    public void showMessageSendFailure(Message message) {
        new AlertDialog.Builder(getContext())
                .setPositiveButton(R.string.resend,
                        (dialog, which) -> presenter.resendMessage(message))
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.discard,
                        (dialog, which) -> presenter.deleteMessage(message))
                .show();
    }

    @Override
    public void autoloadImages() {
        messageListAdapter.setAutoloadImages(true);
    }

    @Override
    public void manualLoadImages() {
        messageListAdapter.setAutoloadImages(false);
    }

    @Override
    public void onReply(AbsoluteUrl absoluteUrl, String markdown, Message message) {
        messageFormManager.setReply(absoluteUrl, markdown, message);
    }

    @Override
    public void onCopy(String message) {
        RocketChatApplication context = RocketChatApplication.getInstance();
        ClipboardManager clipboardManager =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", message));
    }

    @Override
    public void showMessageActions(Message message) {
        Activity context = getActivity();
        if (context != null && context instanceof MainActivity) {
            MessagePopup.take(message)
                    .setReplyAction(presenter::replyMessage)
                    .setEditAction(this::onEditMessage)
                    .setCopyAction(msg -> onCopy(message.getMessage()))
                    .showWith(context);
        }
    }

    private void onEditMessage(Message message) {
        edittingMessage = message;
        messageFormManager.setEditMessage(message.getMessage());
    }

    private void showRoomListFragment(int actionId) {
        //TODO: oddly sometimes getActivity() yields null. Investigate the situations this might happen
        //and fix it, removing this null-check
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), RoomActivity.class).putExtra("actionId", actionId)
                    .putExtra("roomId", roomId)
                    .putExtra("roomType", roomType)
                    .putExtra("hostname", hostname)
                    .putExtra("token", token)
                    .putExtra("userId", userId);
            startActivity(intent);
        }
    }
}