/**
 * Generator steps:
 *
 * 1. Download EmojiOne json file from: https://raw.githubusercontent.com/emojione/emojione/master/emoji.json
 * 2. Install sdkman, kotlin and kscript for cli usage
 * 3. Run: kscript generator.kts
 *
 * This file will output a json file named emoji-parsed.json
 */
@file:DependsOn("org.json:json:20090211")

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

val stream = File("emoji.json").inputStream()
val sb = StringBuilder()
val isr = InputStreamReader(stream, Charsets.UTF_8)
val br = BufferedReader(isr)
var read: String? = br.readLine()
while (read != null) {
    sb.append(read)
    read = br.readLine()
}
br.close()
val json = JSONObject(sb.toString())
val all = JSONArray()
val jsonList = mutableListOf<JSONObject>()
json.keys().forEach {
    val oldJson = json.getJSONObject(it as String) as JSONObject
    val newJson = JSONObject().apply {
        put("shortname", oldJson.getString("shortname"))
        put("category", oldJson.getString("category"))
        put("shortnameAlternates", oldJson.getJSONArray("shortname_alternates"))
        put("keywords", oldJson.getJSONArray("keywords"))
        put("order", oldJson.getInt("order"))
        val codePoints = oldJson.get("code_points") as JSONObject
        val unicode = codePoints.getString("fully_qualified")
        put("unicode", unicode)
    }
    all.put(newJson)
    jsonList.add(newJson)
}
Collections.sort(jsonList, { o1, o2 ->
    val order1 = o1.getInt("order")
    val order2 = o2.getInt("order")
    return@sort order1 - order2
})
File("emoji-parsed.json").printWriter(Charsets.UTF_8).use { out ->
    out.println("[")
    for (i in 0..jsonList.size - 1) {
        out.print(jsonList.get(i).toString(2))
        if (i < jsonList.size - 1) {
            out.println(",")
        }
    }
    out.println("]")
}
println("Ok")

