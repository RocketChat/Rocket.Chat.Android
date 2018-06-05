package chat.rocket.android.util

import timber.log.Timber
import java.io.*

fun deserialiseToken(bytesArray: ByteArray): TokenSerialisableModel {
    val a = ByteArrayInputStream(bytesArray)
    val b = ObjectInputStream(a)
    var returnObject: TokenSerialisableModel? = null
    try {
        returnObject = b.readObject() as TokenSerialisableModel
    } catch (ex: ClassNotFoundException) {
        Timber.e("Excecption: $ex")
    } catch (ex: InvalidClassException) {
        Timber.e("Excecption: $ex")
    } catch (ex: StreamCorruptedException) {
        Timber.e("Excecption: $ex")
    } catch (ex: OptionalDataException) {
        Timber.e("Excecption: $ex")
    } catch (ex: IOException) {
        Timber.e("Excecption: $ex")
    }
    return returnObject!!
}