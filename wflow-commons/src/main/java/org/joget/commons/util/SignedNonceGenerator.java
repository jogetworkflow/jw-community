package org.joget.commons.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A stateless {@link NonceGenerator} for single-node (Community) deployments.
 *
 * The nonce is a self-contained token: an expiry timestamp plus an HMAC-SHA256
 * signature computed over the nonce attributes and that expiry, using a secret
 * generated once per JVM. {@link #verifyNonce} recomputes the signature and checks
 * the expiry, so no server-side session or cluster state is required. A value that
 * the server did not issue cannot be forged without the secret, which closes the
 * fail-open behaviour of {@link SecurityUtil#verifyNonce} when no generator is wired.
 *
 * This bean is wired only in the Community web application context; clustered and
 * enterprise deployments use the session-based generator instead. Because the secret
 * is per-JVM, pending nonces do not survive a restart and a value is not valid across
 * nodes - both acceptable for a single-node install given the short nonce lifespan.
 */
public class SignedNonceGenerator implements NonceGenerator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final byte FIELD_SEPARATOR = 0x1f; // ASCII unit separator

    // Secret used to sign nonces, generated once per JVM.
    private static final byte[] SECRET = generateSecret();

    private static byte[] generateSecret() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return secret;
    }

    @Override
    public String generateNonce(String[] attributes, int lifespanHour) {
        try {
            long expiry = System.currentTimeMillis() + ((long) lifespanHour * 60L * 60L * 1000L);
            String signature = sign(attributes, expiry);
            String token = expiry + ":" + signature;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LogUtil.error(SignedNonceGenerator.class.getName(), e, "Unable to generate nonce");
            return "";
        }
    }

    @Override
    public boolean verifyNonce(String nonce, String[] attributes) {
        if (nonce == null || nonce.isEmpty() || attributes == null) {
            return false;
        }
        try {
            String token = new String(Base64.getUrlDecoder().decode(nonce), StandardCharsets.UTF_8);
            int separatorIndex = token.indexOf(':');
            if (separatorIndex <= 0) {
                return false;
            }
            long expiry = Long.parseLong(token.substring(0, separatorIndex));
            if (System.currentTimeMillis() > expiry) {
                return false;
            }
            String presentedSignature = token.substring(separatorIndex + 1);
            String expectedSignature = sign(attributes, expiry);
            return constantTimeEquals(expectedSignature, presentedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clearNonces(int requestHash) {
        // Stateless generator: nonces are not stored, so there is nothing to clear.
        // They become invalid automatically once their embedded expiry passes.
    }

    /**
     * Compute the HMAC-SHA256 of the attributes and expiry. Each attribute is
     * length-prefixed so that attribute boundaries cannot be shifted by an attacker
     * that controls part of the attribute content.
     */
    private String sign(String[] attributes, long expiry) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(SECRET, HMAC_ALGORITHM));
        for (String attribute : attributes) {
            byte[] value = (attribute == null) ? new byte[0] : attribute.getBytes(StandardCharsets.UTF_8);
            mac.update(intToBytes(value.length));
            mac.update(value);
            mac.update(FIELD_SEPARATOR);
        }
        mac.update(Long.toString(expiry).getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal());
    }

    private static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
    }

    private static boolean constantTimeEquals(String expected, String presented) {
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = presented.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
