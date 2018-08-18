package chat.rocket.android.util

import timber.log.Timber
import java.io.*

fun deserialiseToken(bytesArray: ByteArray): TokenSerialisableModel {
    val byteArrayOutputStream = ByteArrayInputStream(bytesArray)
    val outputStream = ObjectInputStream(byteArrayOutputStream)
    var returnObject: TokenSerialisableModel? = null
    try {
        returnObject = outputStream.readObject() as TokenSerialisableModel
    } catch (ex: ClassNotFoundException) {
        Timber.e("Exception: $ex")
    } catch (ex: InvalidClassException) {
        Timber.e("Exception: $ex")
    } catch (ex: StreamCorruptedException) {
        Timber.e("Exception: $ex")
    } catch (ex: OptionalDataException) {
        Timber.e("Exception: $ex")
    } catch (ex: IOException) {
        Timber.e("Exception: $ex")
    }
    return returnObject!!
}