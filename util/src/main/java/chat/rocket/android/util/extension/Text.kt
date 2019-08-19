package chat.rocket.android.util.extension

import java.security.MessageDigest

/**
 * Returns hash for a string.
 *
 * @param hashType The type of the hash.
 * @see HashType
 */
fun String.hash(hashType: HashType): String = MessageDigest
    .getInstance(hashType.method)
    .digest(this.toByteArray())
    .joinToString(separator = "") {
        String.format("%02X", it)
    }

enum class HashType(val method: String) {
    Sha512("SHA-512"),
    Sha256("SHA-256"),
    Sha1("SHA-1")
}