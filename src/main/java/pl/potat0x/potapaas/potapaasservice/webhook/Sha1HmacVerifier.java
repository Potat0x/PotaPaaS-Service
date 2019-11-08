package pl.potat0x.potapaas.potapaasservice.webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

final class Sha1HmacVerifier {

    private final String message;
    private final String expectedSha1HexDigest;

    public Sha1HmacVerifier(String message, String expectedSha1HexDigest) {
        this.message = message;
        if (expectedSha1HexDigest.startsWith("sha1=")) {
            this.expectedSha1HexDigest = expectedSha1HexDigest.substring("sha1=".length());
        } else {
            this.expectedSha1HexDigest = expectedSha1HexDigest;
        }
    }

    public boolean isMessageAuthentic(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(message.getBytes());
        return expectedSha1HexDigest.equals(encodeHexString(rawHmac));
    }
}
