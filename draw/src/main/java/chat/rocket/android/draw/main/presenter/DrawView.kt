package chat.rocket.android.draw.main.presenter

interface DrawView {

    /**
     * Sends the [ByteArray] of the processed draw image (compressed).
     */
    fun sendByteArray(byteArray: ByteArray)

    /**
     * Shows a message indicating that something was wrong while processing the draw image.
     */
    fun showWrongProcessingMessage()
}