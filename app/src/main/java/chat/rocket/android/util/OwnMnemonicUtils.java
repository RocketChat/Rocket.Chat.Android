package chat.rocket.android.util;

import org.web3j.crypto.MnemonicUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

import static org.web3j.crypto.Hash.sha256;

public class OwnMnemonicUtils extends MnemonicUtils {

    /*
    * Overriding functions to fix an Out-of-Bounds exception caused by original method in web3j library
    * */

    private static final List<String> WORD_LIST = populateWordList();

    public static String generateMnemonic(byte[] initialEntropy) {
        validateInitialEntropy(initialEntropy);

        int ent = initialEntropy.length * 8;
        int checksumLength = ent / 32;

        byte checksum = calculateChecksum(initialEntropy);
        boolean[] bits = convertToBits(initialEntropy, checksum);

        int iterations = (ent + checksumLength) / 11;
        StringBuilder mnemonicBuilder = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            int index = toInt(nextElevenBits(bits, i));
            mnemonicBuilder.append(WORD_LIST.get(index));

            boolean notLastIteration = i < iterations - 1;
            if (notLastIteration) {
                mnemonicBuilder.append(" ");
            }
        }

        return mnemonicBuilder.toString();
    }

    private static boolean[] nextElevenBits(boolean[] bits, int i) {
        int from = i * 11;
        int to = from + 11;
        return Arrays.copyOfRange(bits, from, to);
    }

    private static void validateInitialEntropy(byte[] initialEntropy) {
        if (initialEntropy == null) {
            throw new IllegalArgumentException("Initial entropy is required");
        }

        int ent = initialEntropy.length * 8;
        if (ent < 128 || ent > 256 || ent % 32 != 0) {
            throw new IllegalArgumentException("The allowed size of ENT is 128-256 bits of "
                    + "multiples of 32");
        }
    }

    private static boolean[] convertToBits(byte[] initialEntropy, byte checksum) {
        int ent = initialEntropy.length * 8;
        int checksumLength = ent / 32;
        int totalLength = ent + checksumLength;
        boolean[] bits = new boolean[totalLength];

        for (int i = 0; i < initialEntropy.length; i++) {
            for (int j = 0; j < 8; j++) {
                byte b = initialEntropy[i];
                bits[8 * i + j] = toBit(b, j);
            }
        }

        for (int i = 0; i < checksumLength; i++) {
            bits[ent + i] = toBit(checksum, i);
        }

        return bits;
    }

    private static int toInt(boolean[] bits) {
        int value = 0;
        for (int i = 0; i < bits.length; i++) {
            boolean isSet = bits[i];
            if (isSet)  {
                value += 1 << bits.length - i - 1;
            }
        }

        return value;
    }

    private static byte calculateChecksum(byte[] initialEntropy) {
        int ent = initialEntropy.length * 8;
        byte mask = (byte) (0xff << 8 - ent / 32);
        byte[] bytes = sha256(initialEntropy);

        return (byte) (bytes[0] & mask);
    }

    private static boolean toBit(byte value, int index) {
        return ((value >>> (7 - index)) & 1) > 0;
    }

    private static List<String> populateWordList() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("en-mnemonic-word-list.txt");
        try {
            return readAllLines(inputStream);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static List<String> readAllLines(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> data = new ArrayList<>();
        for (String line; (line = br.readLine()) != null; ) {
            data.add(line);
        }
        return data;
    }
}
