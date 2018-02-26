package chat.rocket.android.server.domain

import chat.rocket.android.util.extensions.mapToTypedArray
import chat.rocket.core.model.Value

typealias PublicSettings = Map<String, Value<Any>>

interface SettingsRepository {
    fun save(url: String, settings: PublicSettings)
    fun get(url: String): PublicSettings?
}

const val ACCOUNT_FACEBOOK = "Accounts_OAuth_Facebook"
const val ACCOUNT_GITHUB = "Accounts_OAuth_Github"
const val ACCOUNT_GITLAB = "Accounts_OAuth_Gitlab"
const val ACCOUNT_GOOGLE = "Accounts_OAuth_Google"
const val ACCOUNT_LINKEDIN = "Accounts_OAuth_Linkedin"
const val ACCOUNT_METEOR = "Accounts_OAuth_Meteor"
const val ACCOUNT_TWITTER = "Accounts_OAuth_Twitter"
const val ACCOUNT_WORDPRESS = "Accounts_OAuth_Wordpress"
const val ACCOUNT_REGISTRATION = "Accounts_RegistrationForm"
const val ACCOUNT_LOGIN_FORM = "Accounts_ShowFormLogin"
const val ACCOUNT_CUSTOM_FIELDS = "Accounts_CustomFields"

const val SITE_URL = "Site_Url"
const val SITE_NAME = "Site_Name"
const val FAVICON_512 = "Assets_favicon_512"
const val USE_REALNAME = "UI_Use_Real_Name"
const val ALLOW_ROOM_NAME_SPECIAL_CHARS = "UI_Allow_room_names_with_special_chars"
const val FAVORITE_ROOMS = "Favorite_Rooms"
const val LDAP_ENABLE = "LDAP_Enable"
const val UPLOAD_STORAGE_TYPE = "FileUpload_Storage_Type"
const val UPLOAD_MAX_FILE_SIZE = "FileUpload_MaxFileSize"
const val UPLOAD_WHITELIST_MIMETYPES = "FileUpload_MediaTypeWhiteList"
const val HIDE_USER_JOIN = "Message_HideType_uj"
const val HIDE_USER_LEAVE = "Message_HideType_ul"
const val HIDE_TYPE_AU = "Message_HideType_au"
const val HIDE_TYPE_RU = "Message_HideType_ru"
const val HIDE_MUTE_UNMUTE = "Message_HideType_mute_unmute"
const val ALLOW_MESSAGE_DELETING = "Message_AllowDeleting"
const val ALLOW_MESSAGE_EDITING = "Message_AllowEditing"
const val SHOW_DELETED_STATUS = "Message_ShowDeletedStatus"
const val SHOW_EDITED_STATUS = "Message_ShowEditedStatus"
const val ALLOW_MESSAGE_PINNING = "Message_AllowPinning"
/*
 * Extension functions for Public Settings.
 *
 * If you need to access a Setting, add a const val key above, add it to the filter on
 * ServerPresenter.kt and a extension function to access it
 */
fun Map<String, Value<Any>>.googleEnabled(): Boolean = this[ACCOUNT_GOOGLE]?.value == true
fun Map<String, Value<Any>>.facebookEnabled(): Boolean = this[ACCOUNT_FACEBOOK]?.value == true
fun Map<String, Value<Any>>.githubEnabled(): Boolean = this[ACCOUNT_GITHUB]?.value == true
fun Map<String, Value<Any>>.linkedinEnabled(): Boolean = this[ACCOUNT_LINKEDIN]?.value == true
fun Map<String, Value<Any>>.meteorEnabled(): Boolean = this[ACCOUNT_METEOR]?.value == true
fun Map<String, Value<Any>>.twitterEnabled(): Boolean = this[ACCOUNT_TWITTER]?.value == true
fun Map<String, Value<Any>>.gitlabEnabled(): Boolean = this[ACCOUNT_GITLAB]?.value == true
fun Map<String, Value<Any>>.wordpressEnabled(): Boolean = this[ACCOUNT_WORDPRESS]?.value == true

fun Map<String, Value<Any>>.useRealName(): Boolean = this[USE_REALNAME]?.value == true

// Message settings
fun Map<String, Value<Any>>.showDeletedStatus(): Boolean = this[SHOW_DELETED_STATUS]?.value == true
fun Map<String, Value<Any>>.showEditedStatus(): Boolean = this[SHOW_EDITED_STATUS]?.value == true
fun Map<String, Value<Any>>.allowedMessagePinning(): Boolean = this[ALLOW_MESSAGE_PINNING]?.value == true
fun Map<String, Value<Any>>.allowedMessageEditing(): Boolean = this[ALLOW_MESSAGE_EDITING]?.value == true
fun Map<String, Value<Any>>.allowedMessageDeleting(): Boolean = this[ALLOW_MESSAGE_DELETING]?.value == true

fun Map<String, Value<Any>>.registrationEnabled(): Boolean {
    val value = this[ACCOUNT_REGISTRATION]
    return value?.value == "Public"
}

fun Map<String, Value<Any>>.uploadMimeTypeFilter(): Array<String> {
    val values = this[UPLOAD_WHITELIST_MIMETYPES]?.value
    values?.let { it as String }?.split(",")?.let {
        return it.mapToTypedArray { it.trim() }
    }

    return arrayOf("*/*")
}

fun Map<String, Value<Any>>.uploadMaxFileSize(): Int {
    return this[UPLOAD_MAX_FILE_SIZE]?.value?.let { it as Int } ?: Int.MAX_VALUE
}

fun Map<String, Value<Any>>.baseUrl() : String? = this[SITE_URL]?.value as String