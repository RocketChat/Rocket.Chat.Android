package chat.rocket.android.emoji

sealed class Fitzpatrick(val type: String) {

    object Default: Fitzpatrick("")
    object LightTone: Fitzpatrick("tone1")
    object MediumLightTone: Fitzpatrick("tone2")
    object MediumTone: Fitzpatrick("tone3")
    object MediumDarkTone: Fitzpatrick("tone4")
    object DarkTone: Fitzpatrick("tone5")
}