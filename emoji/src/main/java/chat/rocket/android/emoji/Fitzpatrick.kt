package chat.rocket.android.emoji

/**
 * Taken the Fitzpatrick scale as reference and adapted to be used with emojione.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fitzpatrick_scale">https://en.wikipedia.org/wiki/Fitzpatrick_scale</a>
 */
sealed class Fitzpatrick(val type: String) {

    object Default: Fitzpatrick("")
    object LightTone: Fitzpatrick("tone1")
    object MediumLightTone: Fitzpatrick("tone2")
    object MediumTone: Fitzpatrick("tone3")
    object MediumDarkTone: Fitzpatrick("tone4")
    object DarkTone: Fitzpatrick("tone5")

    companion object {
        fun valueOf(type: String): Fitzpatrick {
            return when(type) {
                "" -> Default
                "tone1" -> LightTone
                "tone2" -> MediumLightTone
                "tone3" -> MediumTone
                "tone4" -> MediumDarkTone
                "tone5" -> DarkTone
                else -> throw IllegalArgumentException("Fitzpatrick type '$type' is invalid")
            }
        }
    }
}