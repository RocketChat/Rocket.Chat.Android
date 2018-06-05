package chat.rocket.android.util

import timber.log.Timber
import java.io.*

var byteArray: ByteArray? = null

fun serialiseToken(token: TokenSerialisableModel): ByteArray? {
    val a = ByteArrayOutputStream()
    val b = ObjectOutputStream(a)
    try {
        b.writeObject(token)
        byteArray = a.toByteArray()
    } catch (ex: InvalidClassException) {
        Timber.e("Excecption: $ex")
    } catch (ex: NotSerializableException) {
        Timber.e("Excecption: $ex")
    } catch (ex: IOException) {
        Timber.e("Excecption: $ex")
    }
    return byteArray
}