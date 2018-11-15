package chat.rocket.android.util.extension

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@Throws(NoSuchAlgorithmException::class)
fun String.gethash(): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    return digest.digest(this.toByteArray())
}

fun ByteArray.toHex(): String = String.format("%0" + this.size * 2 + "X", BigInteger(1, this))
