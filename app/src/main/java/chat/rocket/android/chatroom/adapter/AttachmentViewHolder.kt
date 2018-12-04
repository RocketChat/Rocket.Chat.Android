package chat.rocket.android.chatroom.adapter

import android.animation.ValueAnimator
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.AttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.helper.ImageHelper
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.isVisible
import chat.rocket.android.util.extensions.openTabbedUrl
import chat.rocket.android.util.extensions.setOnClickListener
import chat.rocket.core.model.attachment.actions.Action
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.item_message_attachment.view.*
import timber.log.Timber

class AttachmentViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null,
    var actionAttachmentOnClickListener: ActionAttachmentOnClickListener
) : BaseViewHolder<AttachmentUiModel>(itemView, listener, reactionListener) {

    private val messageViews = listOf<View>(
        itemView.text_sender,
        itemView.text_message_time,
        itemView.text_content,
        itemView.text_view_more
    )
    private val audioVideoViews = listOf<View>(
        itemView.audio_video_attachment,
        itemView.play_button
    )

    private val quoteBarColor = ContextCompat.getColor(itemView.context, R.color.quoteBar)

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
        }

    }

    override fun bindViews(data: AttachmentUiModel) {
        with(itemView) {
            file_name.isVisible = false
            text_file_name.isVisible = false

            // Media attachments
            image_attachment.isVisible = data.hasImage
            audio_video_attachment.isVisible = data.hasAudioOrVideo
            when {
                data.hasImage -> bindImage(data)
                data.hasAudioOrVideo -> bindAudioOrVideo(data)
                data.hasFile -> bindFile(data)
            }

            // File description - self describing
            file_description.isVisible = data.hasDescription
            file_description.text = data.description

            // Message attachment
            messageViews.isVisible = data.hasMessage
            if (data.hasMessage) {
                bindMessage(data)
            }

            // Author
            author_icon.isInvisible = !(data.hasAuthorIcon && data.hasAuthorLink && data.hasAuthorName)
            text_author_name.isVisible = data.hasAuthorLink && data.hasAuthorName
            if (data.hasAuthorLink && data.hasAuthorName) {
                bindAuthorLink(data)
            }

            // If not media or message, show the text with quote bar
            attachment_text.isVisible = !data.hasMedia && !data.hasMessage && data.hasText
            attachment_text.text = data.text

            // If it has titleLink and is not "type = file" show the title/titleLink on this field.
            file_name_not_file_type.isVisible = !data.hasFile && data.hasTitleLink
            if (!data.hasFile && data.hasTitleLink) {
                bindTitleLink(data)
            }

            // Fields
            text_fields.isVisible = data.hasFields
            if (data.hasFields) {
                bindFields(data)
            }

            // Actions
            actions_list.isVisible = data.hasActions
            if (data.hasActions) {
                bindActions(data)
            }

            // Quote bar
            quote_bar.isVisible = shouldShowQuoteBar(data)
            if (data.color != null) {
                quote_bar.setColorFilter(data.color)
            } else {
                quote_bar.setColorFilter(quoteBarColor)
            }
        }
    }

    private fun shouldShowQuoteBar(data: AttachmentUiModel): Boolean {
        return data.hasFields || data.hasActions || (data.hasAuthorLink && data.hasAuthorName)
                || data.hasMessage || (!data.hasFile && data.hasTitleLink)
                || (!data.hasMedia && !data.hasMessage && data.hasText)
    }

    private fun bindImage(data: AttachmentUiModel) {
        with(itemView) {
            val controller = Fresco.newDraweeControllerBuilder().apply {
                setUri(data.imageUrl)
                autoPlayAnimations = true
                oldController = image_attachment.controller
            }.build()
            image_attachment.controller = controller
            image_attachment.setOnClickListener {
                ImageHelper.openImage(
                        context,
                        data.imageUrl!!,
                        data.title?.toString()
                )
            }

            file_name.isVisible = data.hasTitle
            file_name.text = data.title

            file_text.isVisible = data.hasText
            file_text.text = data.text
        }
    }

    private fun bindAudioOrVideo(data: AttachmentUiModel) {
        with(itemView) {
            file_name.isVisible = data.hasTitle
            file_name.text = data.title

            file_text.isVisible = data.hasText
            file_text.text = data.text

            val url = if (data.hasVideo) data.videoUrl else data.audioUrl
            audioVideoViews.setOnClickListener { view ->
                url?.let {
                    PlayerActivity.play(view.context, url)
                }
            }

        }
    }

    private fun bindFile(data: AttachmentUiModel) {
        with(itemView) {
            text_file_name.isVisible = true
            text_file_name.content = data.title

            text_file_name.setOnClickListener {
                it.context.startActivity(Intent(Intent.ACTION_VIEW, data.titleLink?.toUri()))
            }
        }
    }

    private fun bindMessage(data: AttachmentUiModel) {
        with(itemView) {
            val collapsedHeight = context.resources.getDimensionPixelSize(R.dimen.quote_collapsed_height)
            val viewMore = context.getString(R.string.msg_view_more)
            val viewLess = context.getString(R.string.msg_view_less)
            text_message_time.text = data.timestamp
            text_sender.text = data.authorName
            text_content.text = data.text
            text_view_more.isVisible = true
            text_view_more.text = if (isExpanded()) viewLess else viewMore
            val lp = text_content.layoutParams
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            text_content.layoutParams = lp
            text_content.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

                override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int,
                                            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    val textMeasuredHeight = bottom - top
                    if (collapsedHeight >= textMeasuredHeight) {
                        text_view_more.isVisible = false
                        text_content.removeOnLayoutChangeListener(this)
                        return
                    }

                    val expandAnimation = ValueAnimator
                            .ofInt(collapsedHeight, textMeasuredHeight)
                            .setDuration(300)
                    expandAnimation.interpolator = LinearInterpolator()

                    val collapseAnimation = ValueAnimator
                            .ofInt(textMeasuredHeight, collapsedHeight)
                            .setDuration(300)
                    collapseAnimation.interpolator = LinearInterpolator()

                    expandAnimation.addUpdateListener {
                        val value = it.animatedValue as Int
                        lp.height = value
                        text_content.layoutParams = lp
                        if (value == textMeasuredHeight) {
                            text_view_more.text = viewLess
                            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        } else {
                            text_view_more.text = viewMore
                        }
                    }

                    collapseAnimation.addUpdateListener {
                        val value = it.animatedValue as Int
                        lp.height = value
                        text_content.layoutParams = lp
                        if (value == textMeasuredHeight) {
                            text_view_more.text = viewLess
                            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        } else {
                            text_view_more.text = viewMore
                        }
                    }

                    text_view_more.setOnClickListener {
                        if (expandAnimation.isRunning) return@setOnClickListener

                        if (isExpanded()) {
                            collapseAnimation.start()
                        } else {
                            expandAnimation.start()
                        }
                    }

                    text_content.removeOnLayoutChangeListener(this)
                }
            })
        }
    }

    private fun bindAuthorLink(data: AttachmentUiModel) {
        with(itemView) {
            author_icon.setImageURI(data.authorIcon)
            text_author_name.content = data.authorName

            text_author_name.setOnClickListener {
                openTabbedUrl(data.authorLink)
            }
        }
    }

    private fun bindTitleLink(data: AttachmentUiModel) {
        with(itemView) {
            val filename = data.title ?: data.titleLink
            file_name_not_file_type.text = filename

            file_name_not_file_type.setOnClickListener {
                openTabbedUrl(data.titleLink)
            }
        }
    }

    private fun bindFields(data: AttachmentUiModel) {
        with(itemView) {
            text_fields.content = data.fields
        }
    }

    private fun bindActions(data: AttachmentUiModel) {
        val actions = data.actions
        val alignment = data.buttonAlignment
        Timber.d("no of actions : ${actions!!.size} : $actions")
        with(itemView) {
            attachment_text.isVisible = data.hasText
            attachment_text.text = data.text
            actions_list.layoutManager = LinearLayoutManager(itemView.context,
                    when (alignment) {
                        "horizontal" -> LinearLayoutManager.HORIZONTAL
                        else -> LinearLayoutManager.VERTICAL //Default
                    }, false)
            actions_list.adapter = ActionsListAdapter(actions, actionAttachmentOnClickListener)
        }
    }

    private fun isExpanded(): Boolean {
        with(itemView) {
            val lp = text_content.layoutParams
            return lp.height == ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
}

interface ActionAttachmentOnClickListener {
    fun onActionClicked(view: View, action: Action)
}