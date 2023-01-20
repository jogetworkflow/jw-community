package org.kecak.commons.security;

import org.jasypt.digest.StandardStringDigester;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.salt.ZeroSaltGenerator;
import org.jasypt.util.text.BasicTextEncryptor;
import org.joget.commons.util.DataEncryption;
import org.joget.commons.util.SetupManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class SecureDataEncryptionImpl implements DataEncryption {
    public final static String PROPERTY_SETUP_SECURITY_SALT = "securitySalt";
    public final static String PROPERTY_SETUP_SECURITY_KEY = "securityKey";
    private SetupManager setupManager;

    private static Map<String, String> cache = new HashMap<String, String>();

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    public String getSalt() {
        return setupManager.getSettingValue(PROPERTY_SETUP_SECURITY_SALT);
    }

    public String getKey() {
        return setupManager.getSettingValue(PROPERTY_SETUP_SECURITY_KEY);
    }

    public String encrypt(String rawContent) {
        String k = getKey();
        if (rawContent != null && k != null && !k.isEmpty()) {
            BasicTextEncryptor encryptor = new BasicTextEncryptor();
            encryptor.setPassword(k);
            return encryptor.encrypt(rawContent);
        }
        return rawContent;
    }

    public String decrypt(String protectedContent) {
        String k = getKey();
        if (k != null && !k.isEmpty() && protectedContent != null && !protectedContent.isEmpty()) {
            String decryptedString = cache.get(protectedContent);
            if (decryptedString == null) {
                BasicTextEncryptor encryptor = new BasicTextEncryptor();
                encryptor.setPassword(k);
                decryptedString = encryptor.decrypt(protectedContent);
                cache.put(protectedContent, decryptedString);
            }
            return decryptedString;
        }
        return protectedContent;
    }

    public String computeHash(String rawContent, String randomSalt) {
        if (rawContent != null && !rawContent.isEmpty()) {
            try {
                String content = this.contentWithSalt(rawContent, randomSalt);
                StandardStringDigester digester = new StandardStringDigester();
                digester.setAlgorithm("SHA-256");
                digester.setSaltGenerator((SaltGenerator)new ZeroSaltGenerator());
                digester.setIterations(10);
                return digester.digest(content);
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        return rawContent;
    }

    public Boolean verifyHash(String hash, String randomSalt, String rawContent) {
        try {
            return hash.equals(this.computeHash(rawContent, randomSalt));
        }
        catch (Exception e) {
            return false;
        }
    }

    public String generateRandomSalt() {
        RandomSaltGenerator g = new RandomSaltGenerator();
        byte[] b = g.generateSalt(24);
        try {
            return new String(b, "UTF-8");
        }
        catch (Exception e) {
            return "";
        }
    }

    protected String contentWithSalt(String rawContent, String salt) {
        String s = getSalt();
        String content = "";
        int numOfChar = rawContent.charAt(0) % 6 + 1;
        while (!(rawContent.isEmpty() && s.isEmpty() && salt.isEmpty())) {
            if (salt.length() > numOfChar) {
                content = content + salt.substring(0, numOfChar);
                salt = salt.substring(numOfChar);
            } else {
                content = content + salt;
                salt = "";
            }
            if (rawContent.length() > numOfChar) {
                content = content + rawContent.substring(0, numOfChar);
                rawContent = rawContent.substring(numOfChar);
            } else {
                content = content + rawContent;
                rawContent = "";
            }
            if (s.length() > numOfChar) {
                content = content + s.substring(0, numOfChar);
                s = s.substring(numOfChar);
                continue;
            }
            content = content + s;
            s = "";
        }
        return content;
    }
}
