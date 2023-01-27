package org.joget.commons.util;

public interface NonceGenerator {
    
    /**
     * Generate nonce.
     * @param attributes
     * @param lifepanHour
     * @return the nonce String.
     */
    public String generateNonce(String[] attributes, int lifepanHour);
    
    /**
     * Verify nonce.
     * @param attributes
     * @param url
     * @return true or false.
     */
    public boolean verifyNonce(String nonce, String[] attributes);
}
