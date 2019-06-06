package chat.rocket.android.thememanager.model

data class Theme(val id: Int, val name: String) {

    override fun toString(): String {
        return "$id - $name"
    }
}