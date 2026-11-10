package org.joget.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit coverage for {@link SignedNonceGenerator}: a valid nonce verifies, and any
 * tampering, attribute mismatch, boundary shift, expiry, or malformed value is rejected.
 */
public class SignedNonceGeneratorTest {

    private final SignedNonceGenerator generator = new SignedNonceGenerator();

    @Test
    public void roundTripVerifies() {
        String[] attrs = new String[] {"AjaxOptions", "crm", "abcdefghij0123456789"};
        String nonce = generator.generateNonce(attrs, 1);
        assertNotNull(nonce);
        assertFalse(nonce.isEmpty());
        assertTrue("a freshly generated nonce must verify with the same attributes",
                generator.verifyNonce(nonce, attrs));
    }

    @Test
    public void roundTripVerifiesForLargeJsonAttribute() {
        StringBuilder json = new StringBuilder("{\"className\":\"x\",\"properties\":{");
        for (int i = 0; i < 500; i++) {
            json.append("\"k").append(i).append("\":\"v").append(i).append("\",");
        }
        json.append("}}");
        String[] attrs = new String[] {"EmbedForm", "crm", "1", json.toString()};
        String nonce = generator.generateNonce(attrs, 1);
        assertTrue(generator.verifyNonce(nonce, attrs));
    }

    @Test
    public void differentAttributeValueIsRejected() {
        String[] attrs = new String[] {"AjaxOptions", "crm", "abcdefghij0123456789"};
        String nonce = generator.generateNonce(attrs, 1);
        String[] attacker = new String[] {"AjaxOptions", "crm", "SCRIPTPAYLOAD00000000"};
        assertFalse("a nonce must not verify for different attribute content",
                generator.verifyNonce(nonce, attacker));
    }

    @Test
    public void differentAttributeCountIsRejected() {
        String[] attrs = new String[] {"AjaxOptions", "crm", "abcdefghij0123456789"};
        String nonce = generator.generateNonce(attrs, 1);
        assertFalse(generator.verifyNonce(nonce, new String[] {"AjaxOptions", "crm"}));
    }

    @Test
    public void attributeBoundaryShiftIsRejected() {
        // Length-prefixing must make {"a","bc"} and {"ab","c"} produce different signatures.
        String nonce = generator.generateNonce(new String[] {"a", "bc"}, 1);
        assertFalse(generator.verifyNonce(nonce, new String[] {"ab", "c"}));
    }

    @Test
    public void expiredNonceIsRejected() {
        // Negative lifespan puts the embedded expiry in the past.
        String[] attrs = new String[] {"AjaxOptions", "crm", "abcdefghij0123456789"};
        String nonce = generator.generateNonce(attrs, -1);
        assertFalse("an expired nonce must not verify", generator.verifyNonce(nonce, attrs));
    }

    @Test
    public void tamperedSignatureIsRejected() {
        String[] attrs = new String[] {"AjaxOptions", "crm", "abcdefghij0123456789"};
        String nonce = generator.generateNonce(attrs, 1);
        // Flip a character in the token to simulate tampering.
        char[] chars = nonce.toCharArray();
        chars[chars.length - 1] = (chars[chars.length - 1] == 'A') ? 'B' : 'A';
        assertFalse(generator.verifyNonce(new String(chars), attrs));
    }

    @Test
    public void emptyNullAndGarbageAreRejected() {
        String[] attrs = new String[] {"AjaxOptions", "crm", "abcdefghij0123456789"};
        assertFalse(generator.verifyNonce(null, attrs));
        assertFalse(generator.verifyNonce("", attrs));
        assertFalse(generator.verifyNonce("not-a-valid-token!!!", attrs));
        assertFalse(generator.verifyNonce(generator.generateNonce(attrs, 1), null));
    }

    @Test
    public void twoNoncesDifferByExpiryButBothVerify() throws Exception {
        String[] attrs = new String[] {"FileUpload", "crm", "1", "field", "image"};
        String first = generator.generateNonce(attrs, 1);
        Thread.sleep(2);
        String second = generator.generateNonce(attrs, 1);
        assertTrue(generator.verifyNonce(first, attrs));
        assertTrue(generator.verifyNonce(second, attrs));
    }
}
