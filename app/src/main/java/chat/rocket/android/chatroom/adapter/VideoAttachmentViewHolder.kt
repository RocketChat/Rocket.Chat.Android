package chat.rocket.android.chatroom.adapter

import android.media.MediaMetadataRetriever
import android.view.View
import chat.rocket.android.chatroom.viewmodel.VideoAttachmentViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.message_attachment.view.*
import timber.log.Timber

class VideoAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<VideoAttachmentViewModel>(itemView, listener, reactionListener) {

    private var disposable: Disposable? = null

    init {
        with(itemView) {
            image_attachment.setVisible(false)
            audio_video_attachment.setVisible(true)
            setupActionMenu(attachment_container)
            setupActionMenu(audio_video_attachment)
        }
    }

    override fun bindViews(data: VideoAttachmentViewModel) {
        with(itemView) {
            disposable = getVideoPreview(data.attachmentUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { video_attachment.setImageBitmap(it) },
                            { Timber.e(it) }
                    )
            file_name.text = data.attachmentTitle
            audio_video_attachment.setOnClickListener { view ->
                data.attachmentUrl.let { url ->
                    PlayerActivity.play(view.context, url)
                }
            }
        }
    }

    /**
     * Disposes the Observable to avoid wrong preview being displayed on scrolling.
     */
    fun unbindView() {
        disposable?.isDisposed?.not()?.let {
            disposable?.dispose()
        }
    }

    private fun getVideoPreview(attachmentUrl: String) = Observable.fromCallable {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(attachmentUrl, HashMap())
        val bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        mediaMetadataRetriever.release()
        bitmap
    }
}