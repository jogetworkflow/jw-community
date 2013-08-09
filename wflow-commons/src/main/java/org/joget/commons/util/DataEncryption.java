package org.joget.commons.util;

public interface DataEncryption {
    
    public String encrypt(String rawContent);
    
    public String decrypt(String protectedContent);
    
    public String computeHash(String rawContent, String randomSalt);
    
    public Boolean verifyHash(String hash, String randomSalt, String rawContent);
    
    public String generateRandomSalt();
}