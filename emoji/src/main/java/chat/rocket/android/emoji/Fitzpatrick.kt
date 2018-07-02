package chat.rocket.android.emoji

/**
 * Taken the Fitzpatrick scale as reference adapted to be used with emojione.
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
}