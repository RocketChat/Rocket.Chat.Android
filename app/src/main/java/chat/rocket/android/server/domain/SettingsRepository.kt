package chat.rocket.android.server.domain

import chat.rocket.android.util.extensions.mapToTypedArray
import chat.rocket.core.model.Value

typealias PublicSettings = Map<String, Value<Any>>

// Authentication methods.
const val LDAP_ENABLE = "LDAP_Enable"
const val CAS_ENABLE = "CAS_enabled"
const val CAS_LOGIN_URL = "CAS_login_url"
const val ACCOUNT_REGISTRATION = "Accounts_RegistrationForm"
const val ACCOUNT_LOGIN_FORM = "Accounts_ShowFormLogin"
const val ACCOUNT_PASSWORD_RESET = "Accounts_PasswordReset"
const val ACCOUNT_CUSTOM_FIELDS = "Accounts_CustomFields"
const val ACCOUNT_GOOGLE = "Accounts_OAuth_Google"
const val ACCOUNT_FACEBOOK = "Accounts_OAuth_Facebook"
const val ACCOUNT_GITHUB = "Accounts_OAuth_Github"
const val ACCOUNT_LINKEDIN = "Accounts_OAuth_Linkedin"
const val ACCOUNT_METEOR = "Accounts_OAuth_Meteor"
const val ACCOUNT_TWITTER = "Accounts_OAuth_Twitter"
const val ACCOUNT_WORDPRESS = "Accounts_OAuth_Wordpress"
const val ACCOUNT_GITLAB = "Accounts_OAuth_Gitlab"
const val ACCOUNT_GITLAB_URL = "API_Gitlab_URL"

const val SITE_URL = "Site_Url"
const val SITE_NAME = "Site_Name"
const val FAVICON_196 = "Assets_favicon_192"
const val FAVICON_512 = "Assets_favicon_512"
const val WIDE_TILE_310 = "Assets_tile_310_wide"
const val USE_REALNAME = "UI_Use_Real_Name"
const val ALLOW_ROOM_NAME_SPECIAL_CHARS = "UI_Allow_room_names_with_special_chars"
const val FAVORITE_ROOMS = "Favorite_Rooms"
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
const val ALLOW_MESSAGE_STARRING = "Message_AllowStarring"
const val STORE_LAST_MESSAGE = "Store_Last_Message"

/*
 * Extension functions for Public Settings.
 *
 * If you need to access a Setting, add a const val key above, add it to the filter on
 * ServerPresenter.kt and a extension function to access it
 */
fun PublicSettings.isLdapAuthenticationEnabled(): Boolean = this[LDAP_ENABLE]?.value == true

fun PublicSettings.isCasAuthenticationEnabled(): Boolean = this[CAS_ENABLE]?.value == true
fun PublicSettings.casLoginUrl(): String = this[CAS_LOGIN_URL]?.value.toString()
fun PublicSettings.isRegistrationEnabledForNewUsers(): Boolean = this[ACCOUNT_REGISTRATION]?.value == "Public"
fun PublicSettings.isLoginFormEnabled(): Boolean = this[ACCOUNT_LOGIN_FORM]?.value == true
fun PublicSettings.isPasswordResetEnabled(): Boolean = this[ACCOUNT_PASSWORD_RESET]?.value == true
fun PublicSettings.isGoogleAuthenticationEnabled(): Boolean = this[ACCOUNT_GOOGLE]?.value == true
fun PublicSettings.isFacebookAuthenticationEnabled(): Boolean = this[ACCOUNT_FACEBOOK]?.value == true
fun PublicSettings.isGithubAuthenticationEnabled(): Boolean = this[ACCOUNT_GITHUB]?.value == true
fun PublicSettings.isLinkedinAuthenticationEnabled(): Boolean = this[ACCOUNT_LINKEDIN]?.value == true
fun PublicSettings.isMeteorAuthenticationEnabled(): Boolean = this[ACCOUNT_METEOR]?.value == true
fun PublicSettings.isTwitterAuthenticationEnabled(): Boolean = this[ACCOUNT_TWITTER]?.value == true
fun PublicSettings.isGitlabAuthenticationEnabled(): Boolean = this[ACCOUNT_GITLAB]?.value == true
fun PublicSettings.gitlabUrl(): String? = this[ACCOUNT_GITLAB_URL]?.value as String?
fun PublicSettings.isWordpressAuthenticationEnabled(): Boolean = this[ACCOUNT_WORDPRESS]?.value == true

fun PublicSettings.useRealName(): Boolean = this[USE_REALNAME]?.value == true
fun PublicSettings.useSpecialCharsOnRoom(): Boolean = this[ALLOW_ROOM_NAME_SPECIAL_CHARS]?.value == true
fun PublicSettings.faviconLarge(): String? = this[FAVICON_512]?.value as String?
fun PublicSettings.favicon(): String? = this[FAVICON_196]?.value as String?
fun PublicSettings.wideTile(): String? = this[WIDE_TILE_310]?.value as String?

// Message settings
fun PublicSettings.showDeletedStatus(): Boolean = this[SHOW_DELETED_STATUS]?.value == true
fun PublicSettings.showEditedStatus(): Boolean = this[SHOW_EDITED_STATUS]?.value == true
fun PublicSettings.allowedMessagePinning(): Boolean = this[ALLOW_MESSAGE_PINNING]?.value == true
fun PublicSettings.allowedMessageStarring(): Boolean = this[ALLOW_MESSAGE_STARRING]?.value == true
fun PublicSettings.allowedMessageEditing(): Boolean = this[ALLOW_MESSAGE_EDITING]?.value == true
fun PublicSettings.allowedMessageDeleting(): Boolean = this[ALLOW_MESSAGE_DELETING]?.value == true

fun PublicSettings.hasShowLastMessage(): Boolean = this[STORE_LAST_MESSAGE] != null
fun PublicSettings.showLastMessage(): Boolean = this[STORE_LAST_MESSAGE]?.value == true

fun PublicSettings.uploadMimeTypeFilter(): Array<String>? {
    val values = this[UPLOAD_WHITELIST_MIMETYPES]?.value as String?
    if (!values.isNullOrBlank()) {
        return values!!.split(",").mapToTypedArray { it.trim() }
    }
    return null
}

fun PublicSettings.uploadMaxFileSize(): Int {
    return this[UPLOAD_MAX_FILE_SIZE]?.value?.let { it as Int } ?: Int.MAX_VALUE
}

fun PublicSettings.baseUrl(): String = this[SITE_URL]?.value as String
fun PublicSettings.siteName(): String? = this[SITE_NAME]?.value as String?

interface SettingsRepository {
    fun save(url: String, settings: PublicSettings)
    fun get(url: String): PublicSettings
}