package org.joget.commons.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service("securityUtil")
public class SecurityUtil implements ApplicationContextAware {

    public final static String ENVELOPE = "%%%%";
    private static ApplicationContext appContext;
    private static DataEncryption de;
    private static NonceGenerator ng;

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }

    public void setDataEncryption(DataEncryption deImpl) {
        if (de == null) {
            de = deImpl;
        }
    }

    public static DataEncryption getDataEncryption() {
        if (de == null) {
            try {
                de = (DataEncryption) getApplicationContext().getBean("dataEncryption");
            } catch (Exception e) {
            }
        }
        return de;
    }
    
    public void setNonceGenerator(NonceGenerator ngImpl) {
        if (ng == null) {
            ng = ngImpl;
        }
    }
    
    public static NonceGenerator getNonceGenerator() {
        if (ng == null) {
            try {
                ng = (NonceGenerator) getApplicationContext().getBean("nonceGenerator");
            } catch (Exception e) {
            }
        }
        return ng;
    }

    public static String encrypt(String rawContent) {

        if (rawContent != null && getDataEncryption() != null) {
            try {
                return ENVELOPE + getDataEncryption().encrypt(rawContent) + ENVELOPE;
            } catch (Exception e) {
                //Ignore
            }
        }
        return rawContent;
    }

    public static String decrypt(String protectedContent) {
        if (protectedContent.startsWith(ENVELOPE) && protectedContent.endsWith(ENVELOPE) && getDataEncryption() != null) {
            try {
                String tempProtectedContent = cleanPrefixPostfix(protectedContent);
                return getDataEncryption().decrypt(tempProtectedContent);
            } catch (Exception e) {
                //Ignore
            }
        }
        return protectedContent;
    }

    public static String computeHash(String rawContent, String randomSalt) {

        if (rawContent != null && !rawContent.isEmpty()) {
            if (getDataEncryption() != null) {
                return ENVELOPE + getDataEncryption().computeHash(rawContent, randomSalt) + ENVELOPE;
            } else {
                return StringUtil.md5Base16(rawContent);
            }
        }
        return rawContent;
    }

    public static Boolean verifyHash(String hash, String randomSalt, String rawContent) {
        if (hash != null && !hash.isEmpty() && rawContent != null && !rawContent.isEmpty()) {
            if (hash.startsWith(ENVELOPE) && hash.endsWith(ENVELOPE) && getDataEncryption() != null) {
                hash = cleanPrefixPostfix(hash);
                return getDataEncryption().verifyHash(hash, randomSalt, rawContent);
            } else {
                return hash.equals(StringUtil.md5Base16(rawContent));
            }
        }
        return false;
    }

    public static String generateRandomSalt() {
        if (getDataEncryption() != null) {
            return getDataEncryption().generateRandomSalt();
        }
        return "";
    }

    protected static String cleanPrefixPostfix(String content) {
        content = content.replaceAll(ENVELOPE, "");

        return content;
    }
    
    public static String generateNonce(String[] attributes, int lifepanHour) {

        if (getNonceGenerator() != null) {
            try {
                return getNonceGenerator().generateNonce(attributes, lifepanHour);
            } catch (Exception e) {
                //Ignore
            }
        }
        return null;
    }
    
    public static boolean verifyNonce(String nonce, String[] attributes) {

        if (nonce != null && !nonce.isEmpty() && getNonceGenerator() != null) {
            try {
                return getNonceGenerator().verifyNonce(nonce, attributes);
            } catch (Exception e) {
                //Ignore
            }
        }
        return false;
    }
}
