package org.joget.commons.spring.web;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * This class is adapted from Spring 4.1.x
 */
public class UriUtils {

    /**
     * Encodes the given URI query with the given encoding.
     *
     * @param query the query to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded query
     * @throws UnsupportedEncodingException when the given encoding parameter is
     * not supported
     */
    public static String encodeQuery(String query, String encoding) throws UnsupportedEncodingException {
        return encodeUriComponent(query, encoding, Type.QUERY);
    }

    /**
     * Encode the given source into an encoded String using the rules specified
     * by the given component and with the given options.
     *
     * @param source the source string
     * @param encoding the encoding of the source string
     * @param type the URI component for the source
     * @return the encoded URI
     * @throws IllegalArgumentException when the given uri parameter is not a
     * valid URI
     */
    private static String encodeUriComponent(String source, String encoding, Type type)
            throws UnsupportedEncodingException {

        if (source == null) {
            return null;
        }
        byte[] bytes = encodeBytes(source.getBytes(encoding), type);
        return new String(bytes, "US-ASCII");
    }

    private static byte[] encodeBytes(byte[] source, Type type) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(source.length);
        for (byte b : source) {
            if (b < 0) {
                b += 256;
            }
            if (type.isAllowed(b)) {
                bos.write(b);
            } else {
                bos.write('%');
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                bos.write(hex1);
                bos.write(hex2);
            }
        }
        return bos.toByteArray();
    }

    /**
     * Enumeration used to identify the allowed characters per URI component.
     * <p>
     * Contains methods to indicate whether a given character is valid in a
     * specific URI component.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
     */
    enum Type {

        QUERY {
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c || '?' == c;
            }

        };

        /**
         * Indicates whether the given character is allowed in this URI
         * component.
         *
         * @return {@code true} if the character is allowed; {@code false}
         * otherwise
         */
        public abstract boolean isAllowed(int c);

        /**
         * Indicates whether the given character is in the {@code ALPHA} set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isAlpha(int c) {
            return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
        }

        /**
         * Indicates whether the given character is in the {@code DIGIT} set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isDigit(int c) {
            return c >= '0' && c <= '9';
        }

        /**
         * Indicates whether the given character is in the {@code gen-delims}
         * set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isGenericDelimiter(int c) {
            return ':' == c || '/' == c || '?' == c || '#' == c || '[' == c || ']' == c || '@' == c;
        }

        /**
         * Indicates whether the given character is in the {@code sub-delims}
         * set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isSubDelimiter(int c) {
            return '!' == c || '$' == c || '&' == c || '\'' == c || '(' == c || ')' == c || '*' == c || '+' == c
                    || ',' == c || ';' == c || '=' == c;
        }

        /**
         * Indicates whether the given character is in the {@code reserved} set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isReserved(int c) {
            return isGenericDelimiter(c) || isSubDelimiter(c);
        }

        /**
         * Indicates whether the given character is in the {@code unreserved}
         * set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isUnreserved(int c) {
            return isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c;
        }

        /**
         * Indicates whether the given character is in the {@code pchar} set.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix
         * A</a>
         */
        protected boolean isPchar(int c) {
            return isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c;
        }
    }

}
