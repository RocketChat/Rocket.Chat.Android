package chat.rocket.android.emoji

sealed class PhysicalKeyboardConfig(val state: Int) {

    fun stateToConfig(state: Int): PhysicalKeyboardConfig {
        return when (state) {
            0 -> Enter
            1 -> EnterPlusShift
            2 -> EnterPlusControl
            else -> Disable
        }
    }

    object Enter : PhysicalKeyboardConfig(0)
    object EnterPlusShift : PhysicalKeyboardConfig(1)
    object EnterPlusControl : PhysicalKeyboardConfig(2)
    object Disable : PhysicalKeyboardConfig(3)

}
