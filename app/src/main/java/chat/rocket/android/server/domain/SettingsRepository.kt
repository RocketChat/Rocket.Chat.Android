package chat.rocket.android.server.domain

import chat.rocket.core.model.Value

interface SettingsRepository {
    fun save(url: String, settings: Map<String, Value<Any>>)
    fun get(url: String): Map<String, Value<Any>>?
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
const val STORAGE_TYPE = "FileUpload_Storage_Type"
const val HIDE_USER_JOIN = "Message_HideType_uj"
const val HIDE_USER_LEAVE = "Message_HideType_ul"
const val HIDE_TYPE_AU = "Message_HideType_au"
const val HIDE_TYPE_RU = "Message_HideType_ru"
const val HIDE_MUTE_UNMUTE = "Message_HideType_mute_unmute"
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

fun Map<String, Value<Any>>.registrationEnabled(): Boolean {
    val value = this[ACCOUNT_REGISTRATION]
    return value?.value == "Public"
}