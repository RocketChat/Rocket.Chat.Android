package chat.rocket.android.helper;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256, MD5, ...
 */
public class CheckSum {
  /**
   * SHA-256.
   */
  public static String sha256(String orig) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      return null;
    }
    messageDigest.update(orig.getBytes(Charset.forName("UTF-8")));

    StringBuilder stringBuilder = new StringBuilder();
    for (byte b : messageDigest.digest()) {
      stringBuilder.append(String.format("%02x", b & 0xff));
    }

    return stringBuilder.toString();
  }
}
