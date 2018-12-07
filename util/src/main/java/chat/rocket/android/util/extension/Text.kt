package chat.rocket.android.util.extension

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Returns a SHA-256 hash for a string.
 */
@Throws(NoSuchAlgorithmException::class)
fun String.gethash(): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    return digest.digest(this.toByteArray())
}

/**
 * Return the hex of a [ByteArray].
 */
fun ByteArray.toHex(): String = String.format("%0" + this.size * 2 + "X", BigInteger(1, this))