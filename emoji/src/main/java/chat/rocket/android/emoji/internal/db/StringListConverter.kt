package chat.rocket.android.emoji.internal.db

import androidx.room.TypeConverter

internal object StringListConverter {

    @TypeConverter
    @JvmStatic
    fun toString(list: List<String>?): String? {
        return if (list == null) null else list.joinToString(separator = ",")
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")
    }
}
