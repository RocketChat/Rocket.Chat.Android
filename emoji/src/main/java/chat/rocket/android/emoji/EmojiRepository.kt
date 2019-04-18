package chat.rocket.android.emoji

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.Log
import chat.rocket.android.emoji.internal.EmojiCategory
import chat.rocket.android.emoji.internal.PREF_EMOJI_RECENTS
import chat.rocket.android.emoji.internal.db.EmojiDatabase
import chat.rocket.android.emoji.internal.isCustom
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

object EmojiRepository {
    private val FITZPATRICK_REGEX = "(.*)_(tone[0-9]):".toRegex(RegexOption.IGNORE_CASE)
    private val shortNameToUnicode = HashMap<String, String>()
    private val SHORTNAME_PATTERN = Pattern.compile(":([-+\\w]+):")
    private var customEmojis = listOf<Emoji>()
    private lateinit var preferences: SharedPreferences
    internal lateinit var cachedTypeface: Typeface
    private lateinit var db: EmojiDatabase
    private lateinit var currentServerUrl: String

    fun setCurrentServerUrl(url: String) {
        currentServerUrl = url
    }

    fun getCurrentServerUrl(): String? {
        return if (::currentServerUrl.isInitialized) currentServerUrl else null
    }

    fun load(
        context: Context,
        customEmojis: List<Emoji> = emptyList(),
        path: String = "emoji.json"
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            this@EmojiRepository.customEmojis = customEmojis
            val allEmojis = mutableListOf<Emoji>()
            db = EmojiDatabase.getInstance(context)

            if (!::cachedTypeface.isInitialized) {
                cachedTypeface =
                        Typeface.createFromAsset(context.assets, "fonts/emojione-android.ttf")
            }

            preferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)
            val stream = context.assets.open(path)
            // Load emojis from emojione ttf file temporarily here. We still need to work on them.
            val emojis = loadEmojis(stream).also {
                it.addAll(customEmojis)
            }.toList()

            for (emoji in emojis) {
                val unicodeIntList = mutableListOf<Int>()

                emoji.category = emoji.category

                if (emoji.isCustom()) {
                    allEmojis.add(emoji)
                    continue
                }

                emoji.unicode.split("-").forEach {
                    val value = it.toInt(16)
                    if (value >= 0x10000) {
                        val surrogatePair = calculateSurrogatePairs(value)
                        unicodeIntList.add(surrogatePair.first)
                        unicodeIntList.add(surrogatePair.second)
                    } else {
                        unicodeIntList.add(value)
                    }
                }

                val unicodeIntArray = unicodeIntList.toIntArray()
                val unicode = String(unicodeIntArray, 0, unicodeIntArray.size)
                emoji.unicode = unicode

                if (hasFitzpatrick(emoji.shortname)) {
                    val matchResult = FITZPATRICK_REGEX.find(emoji.shortname)
                    val prefix = matchResult!!.groupValues[1] + ":"
                    val fitzpatrick = Fitzpatrick.valueOf(matchResult.groupValues[2])
                    val defaultEmoji = allEmojis.firstOrNull { it.shortname == prefix }
                    emoji.fitzpatrick = fitzpatrick.type

                    emoji.isDefault = if (defaultEmoji != null) {
                        defaultEmoji.siblings.add(emoji.shortname)
                        false
                    } else {
                        true
                    }

                    emoji.isDefault = false
                }

                allEmojis.add(emoji)

                shortNameToUnicode.apply {
                    put(emoji.shortname, unicode)
                    emoji.shortnameAlternates.forEach { alternate -> put(alternate, unicode) }
                }
            }

            saveEmojisToDatabase(allEmojis.toList())

            // Prefetch all custom emojis to make cache.
            val px = context.resources.getDimensionPixelSize(R.dimen.custom_emoji_large)

            customEmojis.forEach {
                try {
                    val future = Glide.with(context).load(it.url).submit(px, px)
                    future.get()
                } catch (ex: Exception) {
                    Log.d("EmojiRepository", "Error fetching custom emoji ${it.shortname}", ex)
                    if (ex is GlideException) {
                        ex.logRootCauses("EmojiRepository")
                    }
                }
            }
        }
    }

    private suspend fun saveEmojisToDatabase(emojis: List<Emoji>) {
        withContext(Dispatchers.IO) {
            db.emojiDao().insertAllEmojis(*emojis.toTypedArray())
        }
    }

    private fun hasFitzpatrick(shortname: String): Boolean {
        return FITZPATRICK_REGEX matches shortname
    }

    /**
     * Get all loaded emojis as list of Emoji objects.
     *
     * @return All emojis for all categories.
     */
    suspend fun getAll(): List<Emoji> = withContext(Dispatchers.IO) {
        return@withContext db.emojiDao().loadAllEmojis()
    }

    internal suspend fun getEmojiSequenceByCategory(category: EmojiCategory): Sequence<Emoji> {
        val list = withContext(Dispatchers.IO) {
            db.emojiDao().loadEmojisByCategory(category.name)
        }

        return sequence {
            list.forEach { yield(it) }
        }
    }

    internal suspend fun getEmojiSequenceByCategoryAndUrl(
        category: EmojiCategory,
        url: String
    ): Sequence<Emoji> {
        val list = withContext(Dispatchers.IO) {
            db.emojiDao().loadEmojisByCategoryAndUrl(category.name, "$url%")
        }

        return sequence {
            list.forEach { yield(it) }
        }
    }

    /**
     * Get the emoji given by a specified shortname. Returns null if can't find any.
     *
     * @param shortname The emoji shortname to search for
     *
     * @return Emoji given by shortname or null
     */
    private suspend fun getEmojiByShortname(shortname: String): Emoji? =
        withContext(Dispatchers.IO) {
            return@withContext db.emojiDao().loadAllCustomEmojis().firstOrNull()
        }

    /**
     * Add an emoji to the Recents category.
     */
    internal fun addToRecents(emoji: Emoji) {
        val emojiShortname = emoji.shortname
        val recentsJson = JSONObject(preferences.getString(PREF_EMOJI_RECENTS, "{}"))

        if (recentsJson.has(emojiShortname)) {
            val useCount = recentsJson.getInt(emojiShortname)
            recentsJson.put(emojiShortname, useCount + 1)
        } else {
            recentsJson.put(emojiShortname, 1)
        }

        preferences.edit().putString(PREF_EMOJI_RECENTS, recentsJson.toString()).apply()
    }

    internal suspend fun getCustomEmojisAsync(): List<Emoji> {
        return withContext(Dispatchers.IO) {
            db.emojiDao().loadAllCustomEmojis().also {
                customEmojis = it
            }
        }
    }

    fun getCustomEmojis(): List<Emoji> = customEmojis

    /**
     * Get all recently used emojis ordered by usage count.
     *
     * @return All recent emojis ordered by usage.
     */
    internal suspend fun getRecents(): List<Emoji> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Emoji>()
        val recentsJson = JSONObject(preferences.getString(PREF_EMOJI_RECENTS, "{}"))

        val allEmojis = db.emojiDao().loadAllEmojis()
        val len = recentsJson.length()
        val recentShortnames = recentsJson.keys()
        for (i in 0 until len) {
            val shortname = recentShortnames.next()
            allEmojis.firstOrNull {
                if (it.shortname == shortname) {
                    if (it.isCustom()) {
                        return@firstOrNull getCurrentServerUrl()?.let { url ->
                            it.url?.startsWith(url)
                        } ?: false
                    }
                    return@firstOrNull true
                }
                false
            }?.let {
                val useCount = recentsJson.getInt(it.shortname)
                list.add(it.copy(count = useCount))
            }
        }

        list.sortWith(Comparator { o1, o2 ->
            o2.count - o1.count
        })

        return@withContext list
    }

    /**
     * Replace shortnames to unicode characters.
     */
    fun shortnameToUnicode(input: CharSequence): String {
        val matcher = SHORTNAME_PATTERN.matcher(input)
        var result: String = input.toString()

        while (matcher.find()) {
            val unicode = shortNameToUnicode.get(":${matcher.group(1)}:") ?: continue

            result = result.replace(":" + matcher.group(1) + ":", unicode)
        }

        return result
    }

    private fun loadEmojis(stream: InputStream): MutableList<Emoji> {
        val emojisJSON = JSONArray(inputStreamToString(stream))
        val emojis = ArrayList<Emoji>(emojisJSON.length())
        for (i in 0 until emojisJSON.length()) {
            val emoji = buildEmojiFromJSON(emojisJSON.getJSONObject(i))
            emoji?.let {
                emojis.add(it)
            }
        }
        return emojis
    }

    private fun buildEmojiFromJSON(json: JSONObject): Emoji? {
        if (!json.has("shortname") || !json.has("unicode")) {
            return null
        }
        return Emoji(
            shortname = json.getString("shortname"),
            unicode = json.getString("unicode"),
            shortnameAlternates = buildStringListFromJsonArray(json.getJSONArray("shortnameAlternates")),
            category = json.getString("category"),
            keywords = buildStringListFromJsonArray(json.getJSONArray("keywords"))
        )
    }

    private fun buildStringListFromJsonArray(array: JSONArray): List<String> {
        val list = ArrayList<String>(array.length())
        (0 until array.length()).mapTo(list) { array.getString(it) }
        return list
    }

    private fun inputStreamToString(stream: InputStream): String {
        val sb = StringBuilder()
        val isr = InputStreamReader(stream, Charsets.UTF_8)
        val br = BufferedReader(isr as Reader?)
        var read: String? = br.readLine()
        while (read != null) {
            sb.append(read)
            read = br.readLine()
        }
        br.close()
        return sb.toString()
    }

    private fun calculateSurrogatePairs(scalar: Int): Pair<Int, Int> {
        val temp: Int = (scalar - 0x10000) / 0x400
        val s1: Int = Math.floor(temp.toDouble()).toInt() + 0xD800
        val s2: Int = ((scalar - 0x10000) % 0x400) + 0xDC00
        return Pair(s1, s2)
    }

    fun init(context: Context) {
        GlobalScope.launch {
            db = EmojiDatabase.getInstance(context)
            preferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)
            cachedTypeface = Typeface.createFromAsset(context.assets, "fonts/emojione-android.ttf")
        }
    }
}