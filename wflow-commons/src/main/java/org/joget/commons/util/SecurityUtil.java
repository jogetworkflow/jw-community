package org.joget.commons.util;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.owasp.csrfguard.CsrfGuard;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Utility methods used by security feature
 * 
 */
@Service("securityUtil")
public class SecurityUtil implements ApplicationContextAware {

    public final static String ENVELOPE = "%%%%";
    private static ApplicationContext appContext;
    private static DataEncryption de;
    private static NonceGenerator ng;

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    /**
     * Method used by system to set an ApplicationContext
     * @param context
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }

    /**
     * Sets a data encryption implementation
     * @param deImpl
     */
    public void setDataEncryption(DataEncryption deImpl) {
        if (de == null) {
            de = deImpl;
        }
    }

    /**
     * Gets the data encryption implementation
     * @return 
     */
    public static DataEncryption getDataEncryption() {
        if (de == null) {
            try {
                de = (DataEncryption) getApplicationContext().getBean("dataEncryption");
            } catch (Exception e) {
            }
        }
        return de;
    }
    
    /**
     * Sets a nonce generator implementation
     * @param ngImpl 
     */
    public void setNonceGenerator(NonceGenerator ngImpl) {
        if (ng == null) {
            ng = ngImpl;
        }
    }
    
    /**
     * Gets the nonce generator implementation
     * @return 
     */
    public static NonceGenerator getNonceGenerator() {
        if (ng == null) {
            try {
                ng = (NonceGenerator) getApplicationContext().getBean("nonceGenerator");
            } catch (Exception e) {
            }
        }
        return ng;
    }

    /**
     * Encrypt raw content if data encryption implementation is exist
     * @param rawContent
     * @return 
     */
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

    /**
     * Decrypt protected content if data encryption implementation is exist
     * @param protectedContent
     * @return 
     */
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

    /**
     * Computes the hash of a raw content if data encryption implementation is exist
     * @param rawContent
     * @param randomSalt
     * @return 
     */
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

    /**
     * Verify the hash is belong to the raw content if data encryption 
     * implementation is exist
     * @param hash
     * @param randomSalt
     * @param rawContent
     * @return 
     */
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

    /**
     * Generate a random salt value if data encryption implementation is exist
     * @return 
     */
    public static String generateRandomSalt() {
        if (getDataEncryption() != null) {
            return getDataEncryption().generateRandomSalt();
        }
        return "";
    }
    
    /**
     * Check the content is a wrapped in a security envelop if data encryption 
     * implementation is exist
     * @param content
     * @return 
     */
    public static boolean hasSecurityEnvelope(String content) {
        if (content != null && content.startsWith(ENVELOPE) && content.endsWith(ENVELOPE) && getDataEncryption() != null) {
            return true;
        }
        return false;
    }

    protected static String cleanPrefixPostfix(String content) {
        content = content.replaceAll(ENVELOPE, "");

        return content;
    }
    
    /**
     * Generate a nonce value based on attributes if Nonce Generator implementation is exist
     * @param attributes
     * @param lifepanHour
     * @return 
     */
    public static String generateNonce(String[] attributes, int lifepanHour) {

        if (getNonceGenerator() != null) {
            try {
                return getNonceGenerator().generateNonce(attributes, lifepanHour);
            } catch (Exception e) {
                //Ignore
            }
        }
        return "";
    }
    
    /**
     * Verify the nonce is a valid nonce against the attributes if Nonce 
     * Generator implementation is exist
     * @param nonce
     * @param attributes
     * @return 
     */
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

    /**
     * Gets the domain name from a given URL
     * @param url
     * @return 
     */
    public static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (Exception e) {}
        return null;
    }
    
    /**
     * Verify the domain name against a whitelist
     * @param domain
     * @param whitelist
     * @return 
     */
    public static boolean isAllowedDomain(String domain, List<String> whitelist) {
        return whitelist != null && domain != null && whitelist.contains(domain);
    }

    /**
     * Returns the name of the CRSF token
     * @return 
     */
    public static String getCsrfTokenName() {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        return csrfGuard.getTokenName();
    }
    
    /**
     * Returns the value of the CRSF token in the request
     * @param request
     * @return 
     */
    public static String getCsrfTokenValue(HttpServletRequest request) {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        return csrfGuard.getTokenValue(request);
    }

    /**
     * Validates a boolean String
     * @param input
     * @throws IllegalArgumentException if the input is invalid
     */
    public static void validateBooleanInput(Boolean input) throws IllegalArgumentException {
    }

    /**
     * Validates an input String
     * @param input
     * @throws IllegalArgumentException if the input is invalid
     */
    public static void validateStringInput(String input) throws IllegalArgumentException {
        validateInput(input, "^[0-9a-zA-Z_\\-\\.\\#\\:]+$");
    }

    /**
     * Validates input
     * @param input
     * @param patternStr
     * @throws IllegalArgumentException if the input is invalid
     */
    public static void validateInput(String input, String patternStr) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(patternStr);
        if (input != null && !input.isEmpty() && !pattern.matcher(input).matches()) {
            throw new IllegalArgumentException("Invalid input: " + input);
        }
    }

}
