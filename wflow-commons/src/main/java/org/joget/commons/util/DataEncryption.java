package org.joget.commons.util;

/**
 * Interface for pluggable data encryption methods.
 */
public interface DataEncryption {

    /**
     * Encrypt a String.
     * @param rawContent
     * @return the encrypted String.
     */
    public String encrypt(String rawContent);

    /**
     * Decrypt a String.
     * @param protectedContent
     * @return the decrypted String.
     */
    public String decrypt(String protectedContent);
    
    /**
     * Generate a hash.
     * @param rawContent
     * @param randomSalt 
     * @return the generated hash.
     */
    public String computeHash(String rawContent, String randomSalt);
    
    /**
     * Verify that a hashed value matches the rawContent with the associated salt.
     * @param rawContent
     * @param randomSalt 
     * @return true if the verification is successful.
     */
    public Boolean verifyHash(String hash, String randomSalt, String rawContent);

    /**
     * Generates a random salt to be used for hashing.
     * @return 
     */
    public String generateRandomSalt();
}
