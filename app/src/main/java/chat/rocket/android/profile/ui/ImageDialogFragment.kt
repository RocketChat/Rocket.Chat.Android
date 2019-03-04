package chat.rocket.android.profile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.image_dialog_fragment.*

private lateinit var avatarUrl: String

fun newInstance(ImageUri: String): Fragment {
    return ImageDialogFragment().apply {
        avatarUrl = ImageUri
    }
}

class ImageDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.image_dialog_fragment)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailImage.setImageURI(avatarUrl)
    }
}