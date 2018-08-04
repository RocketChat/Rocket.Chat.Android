package chat.rocket.android.emoji

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import chat.rocket.android.emoji.internal.EmojiCategory
import chat.rocket.android.emoji.internal.PREF_EMOJI_RECENTS
import chat.rocket.android.emoji.internal.db.EmojiDatabase
import com.bumptech.glide.Glide
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.coroutines.experimental.buildSequence


object EmojiRepository {

    private val FITZPATRICK_REGEX = "(.*)_(tone[0-9]):".toRegex(RegexOption.IGNORE_CASE)
    private val shortNameToUnicode = HashMap<String, String>()
    private val SHORTNAME_PATTERN = Pattern.compile(":([-+\\w]+):")
    private var customEmojis = listOf<Emoji>()
    private lateinit var preferences: SharedPreferences
    internal lateinit var cachedTypeface: Typeface
    private lateinit var db: EmojiDatabase

    fun load(context: Context, customEmojis: List<Emoji> = emptyList(), path: String = "emoji.json") {
        launch(CommonPool) {
            this@EmojiRepository.customEmojis = customEmojis
            val allEmojis = mutableListOf<Emoji>()
            db = EmojiDatabase.getInstance(context)
            cachedTypeface = Typeface.createFromAsset(context.assets, "fonts/emojione-android.ttf")
            preferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)
            val stream = context.assets.open(path)
            // Load emojis from emojione ttf file temporarily here. We still need to work on them.
            val emojis = loadEmojis(stream).also {
                it.addAll(customEmojis)
            }.toList()

            for (emoji in emojis) {
                val unicodeIntList = mutableListOf<Int>()

                emoji.category = emoji.category.toUpperCase()

                // If unicode is empty it's a custom emoji, just add it.
                if (emoji.unicode.isEmpty()) {
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
                val emojiWithUnicode = emoji.copy(unicode = unicode)

                if (hasFitzpatrick(emoji.shortname)) {
                    val matchResult = FITZPATRICK_REGEX.find(emoji.shortname)
                    val prefix = matchResult!!.groupValues[1] + ":"
                    val fitzpatrick = Fitzpatrick.valueOf(matchResult.groupValues[2])
                    val defaultEmoji = allEmojis.firstOrNull { it.shortname == prefix }
                    val emojiWithFitzpatrick = emojiWithUnicode.copy(fitzpatrick = fitzpatrick.type)

                    if (defaultEmoji != null) {
                        emojiWithFitzpatrick.default = false
                        defaultEmoji.siblings.toMutableList().add(emoji.shortname)
                    } else {
                        // This emoji doesn't have a default tone, ie. :man_in_business_suit_levitating_tone1:
                        // In this case, the default emoji becomes the first toned one.
                        allEmojis.add(emojiWithFitzpatrick)
                    }
                } else {
                    allEmojis.add(emojiWithUnicode)
                }

                shortNameToUnicode.apply {
                    put(emoji.shortname, unicode)
                    emoji.shortnameAlternates.forEach { alternate -> put(alternate, unicode) }
                }
            }

            saveEmojisToDatabase(allEmojis.toList())

            val density = context.resources.displayMetrics.density
            val px = (32 * density).toInt()

            customEmojis.forEach {
                val future = Glide.with(context)
                    .load(it.url)
                    .submit(px, px)
                future.get()
            }
        }
    }

    private suspend fun saveEmojisToDatabase(emojis: List<Emoji>) {
        withContext(CommonPool) {
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
    internal suspend fun getAll(): List<Emoji> = withContext(CommonPool) {
        return@withContext db.emojiDao().loadAllEmojis()
    }

    internal suspend fun getEmojiSequenceByCategory(category: EmojiCategory): Sequence<Emoji> {
        val list = withContext(CommonPool) {
            db.emojiDao().loadEmojisByCategory(category.name.toLowerCase())
        }

        return buildSequence {
            list.forEach {
                yield(it)
            }
        }
    }

    /**
     * Get the emoji given by a specified shortname. Returns null if can't find any.
     *
     * @param shortname The emoji shortname to search for
     *
     * @return Emoji given by shortname or null
     */
    private suspend fun getEmojiByShortname(shortname: String): Emoji? = withContext(CommonPool) {
        return@withContext db.emojiDao().loadEmojiByShortname(shortname)
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
        return withContext(CommonPool) {
            db.emojiDao().loadAllCustomEmojis().also {
                this.customEmojis = it
            }
        }
    }

    internal fun getCustomEmojis(): List<Emoji> = customEmojis

    /**
     * Get all recently used emojis ordered by usage count.
     *
     * @return All recent emojis ordered by usage.
     */
    internal suspend fun getRecents(): List<Emoji> {
        val list = mutableListOf<Emoji>()
        val recentsJson = JSONObject(preferences.getString(PREF_EMOJI_RECENTS, "{}"))

//        for (shortname in recentsJson.keys()) {
//            val emoji = getEmojiByShortname(shortname)
//            emoji?.let {
//                val useCount = recentsJson.getInt(it.shortname)
//                list.add(it.copy(count = useCount))
//            }
//        }
//
//        list.sortWith(Comparator { o1, o2 ->
//            o2.count - o1.count
//        })

        return list
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
        val emojis = ArrayList<Emoji>(emojisJSON.length());
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
        return Emoji(shortname = json.getString("shortname"),
            unicode = json.getString("unicode"),
            shortnameAlternates = buildStringListFromJsonArray(json.getJSONArray("shortnameAlternates")),
            category = json.getString("category"),
            keywords = buildStringListFromJsonArray(json.getJSONArray("keywords")))
    }

    private fun buildStringListFromJsonArray(array: JSONArray): List<String> {
        val list = ArrayList<String>(array.length())
        (0 until array.length()).mapTo(list) { array.getString(it) }
        return list
    }

    private fun inputStreamToString(stream: InputStream): String {
        val sb = StringBuilder()
        val isr = InputStreamReader(stream, Charsets.UTF_8)
        val br = BufferedReader(isr)
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
}