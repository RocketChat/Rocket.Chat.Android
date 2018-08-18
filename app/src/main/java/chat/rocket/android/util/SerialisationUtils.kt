package chat.rocket.android.util

import timber.log.Timber
import java.io.*

var byteArray: ByteArray? = null

fun serialiseToken(token: TokenSerialisableModel): ByteArray? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val outputStream = ObjectOutputStream(byteArrayOutputStream)
    try {
        outputStream.writeObject(token)
        byteArray = byteArrayOutputStream.toByteArray()
    } catch (ex: InvalidClassException) {
        Timber.e("Exception: $ex")
    } catch (ex: NotSerializableException) {
        Timber.e("Exception: $ex")
    } catch (ex: IOException) {
        Timber.e("Exception: $ex")
    }
    return byteArray
}