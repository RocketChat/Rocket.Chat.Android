package chat.rocket.android.widget.emoji

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

object EmojiRepository {
    private val shortNameToUnicode = HashMap<String, String>()
    private val SHORTNAME_PATTERN = Pattern.compile(":([-+\\w]+):")
    private val ALL_EMOJIS = mutableListOf<Emoji>()
    private lateinit var preferences: SharedPreferences
    internal lateinit var cachedTypeface: Typeface

    fun load(context: Context, path: String = "emoji.json") {
        preferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)
        ALL_EMOJIS.clear()
        cachedTypeface = Typeface.createFromAsset(context.assets, "fonts/emojione-android.ttf")
        val stream = context.assets.open(path)
        val emojis = loadEmojis(stream)
        emojis.forEach {
            val unicodeIntList = mutableListOf<Int>()
            it.unicode.split("-").forEach {
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
            ALL_EMOJIS.add(it.copy(unicode = unicode))
            shortNameToUnicode.apply {
                put(it.shortname, unicode)
                it.shortnameAlternates.forEach { alternate -> put(alternate, unicode) }
            }
        }
    }

    /**
     * Get all loaded emojis as list of Emoji objects.
     *
     * @return All emojis for all categories.
     */
    fun getAll() = ALL_EMOJIS

    /**
     * Get all emojis for a given category.
     *
     * @param category Emoji category such as: PEOPLE, NATURE, ETC
     *
     * @return All emoji from specified category
     */
    fun getEmojisByCategory(category: EmojiCategory): List<Emoji> {
        return ALL_EMOJIS.filter { it.category.toLowerCase() == category.name.toLowerCase() }
    }

    /**
     * Get the emoji given by a specified shortname. Returns null if can't find any.
     *
     * @param shortname The emoji shortname to search for
     *
     * @return Emoji given by shortname or null
     */
    fun getEmojiByShortname(shortname: String) = ALL_EMOJIS.firstOrNull { it.shortname == shortname }

    /**
     * Add an emoji to the Recents category.
     */
    fun addToRecents(emoji: Emoji) {
        val emojiShortname = emoji.shortname
        val recentsJson = JSONObject(preferences.getString(EmojiKeyboardPopup.PREF_EMOJI_RECENTS, "{}"))
        if (recentsJson.has(emojiShortname)) {
            val useCount = recentsJson.getInt(emojiShortname)
            recentsJson.put(emojiShortname, useCount + 1)
        } else {
            recentsJson.put(emojiShortname, 1)
        }
        preferences.edit().putString(EmojiKeyboardPopup.PREF_EMOJI_RECENTS, recentsJson.toString()).apply()
    }

    /**
     * Get all recently used emojis ordered by usage count.
     *
     * @return All recent emojis ordered by usage.
     */
    fun getRecents(): List<Emoji> {
        val list = mutableListOf<Emoji>()
        val recentsJson = JSONObject(preferences.getString(EmojiKeyboardPopup.PREF_EMOJI_RECENTS, "{}"))
        for (shortname in recentsJson.keys()) {
            val emoji = getEmojiByShortname(shortname)
            emoji?.let {
                val useCount = recentsJson.getInt(it.shortname)
                list.add(it.copy(count = useCount))
            }
        }
        Collections.sort(list, { o1, o2 ->
            o2.count - o1.count
        })
        return list
    }

    /**
     * Replace shortnames to unicode characters.
     */
    fun shortnameToUnicode(input: CharSequence, removeIfUnsupported: Boolean): String {
        val matcher = SHORTNAME_PATTERN.matcher(input)
        val supported = Build.VERSION.SDK_INT >= 16
        var result: String = input.toString()

        while (matcher.find()) {
            val unicode = shortNameToUnicode.get(":${matcher.group(1)}:")
            if (unicode == null) {
                continue
            }

            if (supported) {
                result = result.replace(":" + matcher.group(1) + ":", unicode)
            } else if (!supported && removeIfUnsupported) {
                result = result.replace(":" + matcher.group(1) + ":", "")
            }
        }

        return result
    }

    private fun loadEmojis(stream: InputStream): List<Emoji> {
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
        for (i in 0..array.length() - 1) {
            list.add(array.getString(i))
        }
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