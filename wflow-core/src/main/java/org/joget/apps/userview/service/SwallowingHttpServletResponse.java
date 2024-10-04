package org.joget.apps.userview.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.joget.commons.util.LogUtil;

/**
 * Used by ExecutionContext to forward results back via javascript.
 * <p>
 * We could like to implement {@link HttpServletResponse}, but there is a bug in
 * WebLogic where it casts to a {@link HttpServletResponseWrapper} so we need to
 * extend that.
 *
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public final class SwallowingHttpServletResponse extends HttpServletResponseWrapper implements HttpServletResponse {

    /**
     * Create a new HttpServletResponse that allows you to catch the body
     *
     * @param response The original HttpServletResponse
     * @param sout The place we copy responses to
     * @param characterEncoding The output encoding
     */
    public SwallowingHttpServletResponse(HttpServletResponse response, Writer sout, String characterEncoding) {
        super(response);

        pout = new PrintWriter(sout);
        outputStream = new WriterOutputStream(sout, characterEncoding);

        this.characterEncoding = characterEncoding;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#addCookie(jakarta.servlet.http.Cookie)
     */
    public void addCookie(Cookie cookie) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#addDateHeader(java.lang.String, long)
     */
    public void addDateHeader(String name, long value) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String name, String value) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#addIntHeader(java.lang.String, int)
     */
    public void addIntHeader(String name, int value) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#containsHeader(java.lang.String)
     */
    public boolean containsHeader(String name) {
        return false;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#encodeRedirectUrl(java.lang.String)
     */
    public String encodeRedirectUrl(String url) {
        return url;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#encodeRedirectURL(java.lang.String)
     */
    public String encodeRedirectURL(String url) {
        return url;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#encodeUrl(java.lang.String)
     */
    public String encodeUrl(String url) {
        return url;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#encodeURL(java.lang.String)
     */
    public String encodeURL(String url) {
        return url;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#flushBuffer()
     */
    public void flushBuffer() throws IOException {
        pout.flush();
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#getBufferSize()
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * @return The MIME type of the content
     * @see jakarta.servlet.ServletResponse#setContentType(String)
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Accessor for any error messages set using {@link #sendError(int)} or
     * {@link #sendError(int, String)}
     *
     * @return The current error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#getLocale()
     */
    public Locale getLocale() {
        return locale;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#getOutputStream()
     */
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Accessor for the redirect URL set using {@link #sendRedirect(String)}
     *
     * @return The redirect URL
     */
    public String getRedirectedUrl() {
        return redirectedUrl;
    }

    /**
     * What HTTP status code should be returned?
     *
     * @return The current http status code
     */
    public int getStatus() {
        return status;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#getWriter()
     */
    public PrintWriter getWriter() {
        return pout;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#isCommitted()
     */
    public boolean isCommitted() {
        return false;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#reset()
     */
    public void reset() {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#resetBuffer()
     */
    public void resetBuffer() {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#sendError(int)
     */
    public void sendError(int newStatus) {
        if (committed) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }

        LogUtil.warn(getClass().getName(), "Ignoring call to sendError(" + newStatus + ')');

        status = newStatus;
        committed = true;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#sendError(int, java.lang.String)
     */
    public void sendError(int newStatus, String newErrorMessage) {
        if (committed) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }

        LogUtil.warn(getClass().getName(), "Ignoring call to sendError(" + newStatus + ", " + newErrorMessage + ')');

        status = newStatus;
        errorMessage = newErrorMessage;
        committed = true;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
     */
    public void sendRedirect(String location) {
        if (committed) {
            throw new IllegalStateException("Cannot send redirect - response is already committed");
        }

        LogUtil.warn(getClass().getName(), "Ignoring call to sendRedirect(" + location + ')');

        redirectedUrl = location;
        committed = true;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#setBufferSize(int)
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @param characterEncoding The new encoding to use for response strings
     * @see jakarta.servlet.ServletResponseWrapper#getCharacterEncoding()
     */
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#setContentLength(int)
     */
    public void setContentLength(int i) {
        // The content length of the original document is not likely to be the
        // same as the content length of the new document.
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#setDateHeader(java.lang.String, long)
     */
    public void setDateHeader(String name, long value) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String name, String value) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#setIntHeader(java.lang.String, int)
     */
    public void setIntHeader(String name, int value) {
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.ServletResponseWrapper#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    public void setStatus(int status) {
        this.status = status;
        LogUtil.warn(getClass().getName(), "Ignoring call to setStatus(" + status + ')');
    }

    /**
     * @see jakarta.servlet.http.HttpServletResponse#setStatus(int,
     * java.lang.String)
     * @deprecated
     */
    @Deprecated
    public void setStatus(int newStatus, String newErrorMessage) {
        status = newStatus;
        errorMessage = newErrorMessage;
        LogUtil.warn(getClass().getName(), "Ignoring call to setStatus(" + newStatus + ", " + newErrorMessage + ')');
    }

    /**
     * The ignored buffer size
     */
    private int bufferSize = 0;

    /**
     * The character encoding used
     */
    private String characterEncoding;

    /**
     * Has the response been comitted
     */
    private boolean committed = false;

    /**
     * The MIME type of the output body
     */
    private String contentType;

    /**
     * The error message sent with a status != HttpServletResponse.SC_OK
     */
    private String errorMessage;

    /**
     * Locale setting: defaults to platform default
     */
    private Locale locale = Locale.getDefault();

    /**
     * The forwarding output stream
     */
    private final ServletOutputStream outputStream;

    /**
     * The forwarding output stream
     */
    private final PrintWriter pout;

    /**
     * Where are we to redirect the user to?
     */
    private String redirectedUrl;

    /**
     * The HTTP status
     */
    private int status = HttpServletResponse.SC_OK;

    public final class WriterOutputStream extends ServletOutputStream {

        /**
         * ctor using platform default encoding
         *
         * @param writer
         */
        public WriterOutputStream(Writer writer) {
            this.writer = writer;
        }

        /**
         * ctor that allows us to specify how strings are created
         *
         * @param writer
         * @param encoding
         */
        public WriterOutputStream(Writer writer, String encoding) {
            this.writer = writer;
            this.encoding = encoding;
        }

        /* (non-Javadoc)
     * @see javax.servlet.ServletOutputStream#print(java.lang.String)
         */
        public void print(String s) throws IOException {
            writer.write(s);
        }

        /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
         */
        public void write(byte[] ba) throws IOException {
            if (encoding == null) {
                writer.write(new String(ba));
            } else {
                writer.write(new String(ba, encoding));
            }
        }

        /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
         */
        public void write(byte[] ba, int off, int len) throws IOException {
            if (encoding == null) {
                writer.write(new String(ba, off, len));
            } else {
                writer.write(new String(ba, off, len, encoding));
            }
        }

        /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
         */
        public synchronized void write(int bite) throws IOException {
            buffer[0] = (byte) bite;
            write(buffer);
        }

        /* (non-Javadoc)
     * @see java.io.OutputStream#close()
         */
        public void close() throws IOException {
            if (writer != null) {
                writer.close();
                writer = null;
                encoding = null;
            }
        }

        /* (non-Javadoc)
     * @see java.io.OutputStream#flush()
         */
        public void flush() throws IOException {
            writer.flush();
        }

        /**
         * The destination of all our printing
         */
        private Writer writer;

        /**
         * What string encoding should we use
         */
        private String encoding = null;

        /**
         * Buffer for write(int)
         */
        private byte[] buffer = new byte[1];

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener wl) {
            // not supported
        }
    }
}
