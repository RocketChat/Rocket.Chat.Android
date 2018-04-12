package chat.rocket.android.members.ui

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.textContent
import kotlinx.android.synthetic.main.fragment_member_bottom_sheet.*

fun newInstance(avatarUri: String, realName: String, username: String, email: String, utcOffset: String): BottomSheetDialogFragment {
    return MemberBottomSheetFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_AVATAR_URI, avatarUri)
            putString(BUNDLE_REAL_NAME, realName)
            putString(BUNDLE_USERNAME, username)
            putString(BUNDLE_EMAIL, email)
            putString(BUNDLE_UTC_OFFSET, utcOffset)
        }
    }
}

private const val BUNDLE_AVATAR_URI = "avatar_uri"
private const val BUNDLE_REAL_NAME = "real_name"
private const val BUNDLE_USERNAME = "username"
private const val BUNDLE_EMAIL = "email"
private const val BUNDLE_UTC_OFFSET = "utc_offset"

class MemberBottomSheetFragment: BottomSheetDialogFragment() {
    private lateinit var avatarUri: String
    private lateinit var realName: String
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var utcOffset: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            avatarUri = bundle.getString(BUNDLE_AVATAR_URI)
            realName = bundle.getString(BUNDLE_REAL_NAME)
            username = bundle.getString(BUNDLE_USERNAME)
            email = bundle.getString(BUNDLE_EMAIL)
            utcOffset = bundle.getString(BUNDLE_UTC_OFFSET)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_member_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMemberDetails()
    }

    private fun showMemberDetails() {
        image_bottom_sheet_avatar.setImageURI(avatarUri)
        text_bottom_sheet_member_name.content = realName
        text_bottom_sheet_member_username.content = username

        if (email.isNotEmpty()) {
            text_member_email_address.textContent = email
        } else {
            text_email_address.setVisible(false)
            text_member_email_address.setVisible(false)
        }

        if (utcOffset.isNotEmpty()){
            text_member_utc.content = utcOffset
        } else {
            text_utc.setVisible(false)
            text_member_utc.setVisible(false)
        }
    }
}