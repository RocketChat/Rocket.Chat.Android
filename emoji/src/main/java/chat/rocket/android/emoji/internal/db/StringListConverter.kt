package chat.rocket.android.emoji.internal.db

import androidx.room.TypeConverter

class StringListConverter {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",") ?: emptyList()
    }
}
