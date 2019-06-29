package chat.rocket.android.profile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.image_profile_dialog_fragment.*

internal const val TAG_IMAGE_DIALOG_FRAGMENT = "ImageProfileDialogFragment"
private const val BUNDLE_IMAGE_URL = "image_url"

fun newInstance(imageUrl: String): Fragment = ImageProfileDialogFragment().apply {
    arguments = Bundle(1).apply {
        putString(BUNDLE_IMAGE_URL, imageUrl)
    }
}

class ImageProfileDialogFragment : DialogFragment() {
    private lateinit var imageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            imageUrl = getString(BUNDLE_IMAGE_URL, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.image_profile_dialog_fragment)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showImage()
    }

    private fun showImage() = context?.let {
        Glide.with(it).load(imageUrl.toUri()).into(image_profile)
    }
}