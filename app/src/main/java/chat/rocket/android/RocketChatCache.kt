package chat.rocket.android

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import chat.rocket.android.helper.Logger
import chat.rocket.android.log.RCLog
import chat.rocket.core.utils.Pair
import com.hadisatrio.optional.Optional
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import okhttp3.HttpUrl
import org.json.JSONException
import org.json.JSONObject
import java.util.*


object RocketChatCache {

    private val KEY_SELECTED_SERVER_HOSTNAME = "KEY_SELECTED_SERVER_HOSTNAME"
    private val KEY_SELECTED_SITE_URL = "KEY_SELECTED_SITE_URL"
    private val KEY_SELECTED_SITE_NAME = "KEY_SELECTED_SITE_NAME"
    private val KEY_SELECTED_ROOM_ID = "KEY_SELECTED_ROOM_ID"
    private val KEY_PUSH_ID = "KEY_PUSH_ID"
    private val KEY_HOSTNAME_LIST = "KEY_HOSTNAME_LIST"
    private val KEY_OPENED_ROOMS = "KEY_OPENED_ROOMS"
    private val KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN"
    private val KEY_USER_ID = "KEY_USER_ID"
    private val KEY_USER_NAME = "KEY_USER_NAME"
    private val KEY_USER_USERNAME = "KEY_USER_USERNAME"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("cache", Context.MODE_PRIVATE)
    }

    fun addOpenedRoom(roomId: String, lastSeen: Long) {
        val openedRooms = getOpenedRooms()
        try {
            val room = JSONObject().put("rid", roomId).put("ls", lastSeen)
            openedRooms.put(roomId, room)
        } catch (e: JSONException) {
            RCLog.e(e)
        }

        setString(KEY_OPENED_ROOMS, openedRooms.toString())
    }

    fun removeOpenedRoom(roomId: String) {
        val openedRooms = getOpenedRooms()
        if (openedRooms.has(roomId)) {
            openedRooms.remove(roomId)
        }
    }

    fun getOpenedRooms(): JSONObject {
        val openedRooms = getString(KEY_OPENED_ROOMS, "")

        openedRooms?.let {
            if (openedRooms.isEmpty()) {
                return JSONObject()
            }
            try {
                return JSONObject(openedRooms)
            } catch (e: JSONException) {
                RCLog.e(e)
            }
        }

        return JSONObject()
    }

    fun getSelectedServerHostname(): String? {
        return getString(KEY_SELECTED_SERVER_HOSTNAME, null)
    }

    fun setSelectedRoomId(roomId: String?) {
        try {
            val jsonObject = getSelectedRoomIdJsonObject()
            jsonObject.put(getSelectedServerHostname(), roomId)
            setString(KEY_SELECTED_ROOM_ID, jsonObject.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
            Logger.report(e)
        }
    }

    @Throws(JSONException::class)
    private fun getSelectedRoomIdJsonObject(): JSONObject {
        val json = getString(KEY_SELECTED_ROOM_ID, null) ?: return JSONObject()
        return JSONObject(json)
    }

    fun getOrCreatePushId(): String? {
        val preferences = sharedPreferences
        if (!preferences.contains(KEY_PUSH_ID)) {
            // generates one and save
            val newId = UUID.randomUUID().toString().replace("-", "")
            preferences.edit()
                    .putString(KEY_PUSH_ID, newId)
                    .apply()
            return newId
        }
        return preferences.getString(KEY_PUSH_ID, null)
    }

    fun addSiteName(currentHostname: String, siteName: String) {
        try {
            val hostSiteNamesJson = getSiteName()
            val jsonObject = if (hostSiteNamesJson == null)
                JSONObject()
            else
                JSONObject(hostSiteNamesJson)
            jsonObject.put(currentHostname, siteName)
            setString(KEY_SELECTED_SITE_NAME, jsonObject.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
        }

    }

    fun getHostSiteName(hostname: String): String {
        var host = hostname
        if (hostname.startsWith("http")) {
            val url = HttpUrl.parse(hostname)
            if (url != null) {
                host = url.host()
            }
        }
        try {
            val hostSiteNamesJson = getSiteName()
            val jsonObject = if (hostSiteNamesJson == null)
                JSONObject()
            else
                JSONObject(hostSiteNamesJson)
            val siteUrlFor = getSiteUrlFor(host)
            return if (siteUrlFor == null) "" else jsonObject.optString(host)
        } catch (e: JSONException) {
            RCLog.e(e)
        }
        return ""
    }

    fun removeSiteName(hostname: String) {
        try {
            val siteNameJson = getSiteName()
            val jsonObject = if (siteNameJson == null)
                JSONObject()
            else
                JSONObject(siteNameJson)
            if (jsonObject.has(hostname)) {
                jsonObject.remove(hostname)
            }
            setString(KEY_SELECTED_SITE_NAME, jsonObject.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
        }

    }

    fun addSiteUrl(hostnameAlias: String?, currentHostname: String) {
        var alias: String? = null
        if (hostnameAlias != null) {
            alias = hostnameAlias.toLowerCase()
        }
        try {
            val selectedHostnameAliasJson = getSiteUrlForAllServers()
            val jsonObject = if (selectedHostnameAliasJson == null)
                JSONObject()
            else
                JSONObject(selectedHostnameAliasJson)
            jsonObject.put(alias, currentHostname)
            setString(KEY_SELECTED_SITE_URL, jsonObject.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
        }
    }

    fun getSiteUrlFor(hostname: String): String? {
        try {
            val selectedServerHostname = getSelectedServerHostname()
            return if (getSiteUrlForAllServers() == null) null else JSONObject(getSiteUrlForAllServers())
                    .optString(hostname, selectedServerHostname)
        } catch (e: JSONException) {
            RCLog.e(e)
        }

        return null
    }

    fun addHostname(hostname: String, hostnameAvatarUri: String?, siteName: String) {
        val hostnameList = getString(KEY_HOSTNAME_LIST, null)
        try {
            val json: JSONObject
            if (hostnameList == null) {
                json = JSONObject()
            } else {
                json = JSONObject(hostnameList)
            }
            val serverInfoJson = JSONObject()
            serverInfoJson.put("avatar", hostnameAvatarUri)
            serverInfoJson.put("sitename", siteName)
            // Replace server avatar uri if exists.
            json.put(hostname, if (hostnameAvatarUri == null) JSONObject.NULL else serverInfoJson)
            setString(KEY_HOSTNAME_LIST, json.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
        }
    }

    fun getServerList(): List<Pair<String, Pair<String, String>>> {
        val json = getString(KEY_HOSTNAME_LIST, null) ?: return emptyList()
        try {
            val jsonObj = JSONObject(json)
            val serverList = ArrayList<Pair<String, Pair<String, String>>>()
            val iter = jsonObj.keys()
            while (iter.hasNext()) {
                val hostname = iter.next()
                val serverInfoJson = jsonObj.getJSONObject(hostname)
                serverList.add(Pair(hostname, Pair(
                        "http://" + hostname + "/" + serverInfoJson.getString("avatar"),
                        serverInfoJson.getString("sitename"))))
            }
            return serverList
        } catch (e: JSONException) {
            RCLog.e(e)
        }

        return emptyList()
    }

    /**
     * Wipe all given hostname entries and references from cache.
     */
    fun clearSelectedHostnameReferences() {
        val hostname = getSelectedServerHostname()
        if (hostname != null) {
            setString(KEY_OPENED_ROOMS, null)
            removeSiteName(hostname)
            removeHostname(hostname)
            removeSiteUrl(hostname)
            setSelectedServerHostname(getFirstLoggedHostnameIfAny())
        }
    }

    fun removeHostname(hostname: String) {
        val json = getString(KEY_HOSTNAME_LIST, null)
        if (TextUtils.isEmpty(json)) {
            return
        }
        try {
            val jsonObj = JSONObject(json)
            jsonObj.remove(hostname)
            val result = if (jsonObj.length() == 0) null else jsonObj.toString()
            setString(KEY_HOSTNAME_LIST, result)
        } catch (e: JSONException) {
            RCLog.e(e)
        }

    }

    fun setSelectedServerHostname(hostname: String?) {
        var newHostname: String? = null
        if (hostname != null) {
            newHostname = hostname.toLowerCase()
        }
        setString(KEY_SELECTED_SERVER_HOSTNAME, newHostname)
    }

    fun getSelectedRoomId(): String? {
        try {
            val jsonObject = getSelectedRoomIdJsonObject()
            return jsonObject.optString(getSelectedServerHostname(), null)
        } catch (e: JSONException) {
            RCLog.e(e)
            Logger.report(e)
        }

        return null
    }

    fun removeSelectedRoomId(currentHostname: String) {
        try {
            val selectedRoomIdJsonObject = getSelectedRoomIdJsonObject()
            selectedRoomIdJsonObject.remove(currentHostname)
            val result = if (selectedRoomIdJsonObject.length() == 0) null else selectedRoomIdJsonObject.toString()
            setString(KEY_SELECTED_ROOM_ID, result)
        } catch (e: JSONException) {
            Logger.report(e)
            RCLog.e(e)
        }
    }

    fun getFirstLoggedHostnameIfAny(): String? {
        val json = getString(KEY_HOSTNAME_LIST, null)
        if (json != null) {
            try {
                val jsonObj = JSONObject(json)
                if (jsonObj.length() > 0 && jsonObj.keys().hasNext()) {
                    // Returns the first hostname on the list.
                    return jsonObj.keys().next()
                }
            } catch (e: JSONException) {
                RCLog.e(e)
            }
        }
        return null
    }

    fun setSessionToken(sessionToken: String) {
        val selectedServerHostname = getSelectedServerHostname() ?:
                throw IllegalStateException("Trying to set sessionToken to null hostname")
        val sessions = getString(KEY_SESSION_TOKEN, null)
        try {
            val jsonObject = if (sessions == null) JSONObject() else JSONObject(sessions)
            jsonObject.put(selectedServerHostname, sessionToken)
            setString(KEY_SESSION_TOKEN, jsonObject.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
        }
    }

    fun getSessionToken(): String? {
        val selectedServerHostname = getSelectedServerHostname()
        val sessions = getString(KEY_SESSION_TOKEN, null)
        if (sessions == null || selectedServerHostname == null) {
            return null
        }
        try {
            val jsonObject = JSONObject(sessions)
            if (jsonObject.has(selectedServerHostname)) {
                return jsonObject.optString(selectedServerHostname, null)
            }
        } catch (e: JSONException) {
            RCLog.e(e)
        }

        return null
    }

    private fun removeSiteUrl(hostname: String) {
        try {
            val siteUrlForAllServersJson = getSiteUrlForAllServers()
            val jsonObject = if (siteUrlForAllServersJson == null)
                JSONObject()
            else
                JSONObject(siteUrlForAllServersJson)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val alias = keys.next()
                if (hostname == jsonObject.getString(alias)) {
                    jsonObject.remove(alias)
                    break
                }
            }
            setString(KEY_SELECTED_SITE_URL, jsonObject.toString())
        } catch (e: JSONException) {
            RCLog.e(e)
        }
    }

    private fun getString(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    private fun getSiteUrlForAllServers(): String? {
        return getString(KEY_SELECTED_SITE_URL, null)
    }

    private fun setString(key: String, value: String?) {
        getEditor().putString(key, value).apply()
    }

    private fun getSiteName(): String? {
        return getString(KEY_SELECTED_SITE_NAME, null)
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    fun getSelectedServerHostnamePublisher(): Flowable<Optional<String>> {
        return getValuePublisher(KEY_SELECTED_SERVER_HOSTNAME)
    }

    fun getSelectedRoomIdPublisher(): Flowable<Optional<String>> {
        return getValuePublisher(KEY_SELECTED_ROOM_ID)
                .filter { it.isPresent() }
                .map { it.get() }
                .map { roomValue -> Optional.ofNullable(JSONObject(roomValue).optString(getSelectedServerHostname(), null)) }
    }

    private fun getValuePublisher(key: String): Flowable<Optional<String>> {
        return Flowable.create({ emitter ->
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (key == changedKey && !emitter.isCancelled) {
                    val value = getString(key, null)
                    emitter.onNext(Optional.ofNullable(value))
                }
            }

            emitter.setCancellable {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }

            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }, BackpressureStrategy.LATEST)
    }

    fun setUserId(userId: String?) {
        userId?.let {
            setString(KEY_USER_ID, userId)
        }
    }

    fun getUserId(): String? = getString(KEY_USER_ID, null)

    fun setUserName(name: String?) {
        name?.let {
            setString(KEY_USER_NAME, name)
        }
    }

    fun getUserName(): String? = getString(KEY_USER_NAME, null)

    fun setUserUsername(username: String?) {
        username?.let {
            setString(KEY_USER_USERNAME, username)
        }
    }

    fun getUserUsername(): String? = getString(KEY_USER_USERNAME, null)
}