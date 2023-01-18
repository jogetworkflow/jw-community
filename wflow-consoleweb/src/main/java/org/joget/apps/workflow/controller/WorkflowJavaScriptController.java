package org.joget.apps.workflow.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to convert output into unicode escaped sequences output via JavaScript document.write
 */
@Controller
public class WorkflowJavaScriptController {

    @Autowired
    private WorkflowUserManager workflowUserManager;

    @RequestMapping("/js/client/inbox")
    public String inbox() throws IOException, ServletException {
        return "client/portlet/inbox";
    }

    @RequestMapping("/js/client/login")
    public String login() throws IOException, ServletException {
        return "client/portlet/login";
    }

    @RequestMapping("/js/client/inbox.js")
    public void inbox(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = workflowUserManager.getCurrentUsername();
        String url = null;
        if (username.equals(WorkflowUserManager.ROLE_ANONYMOUS)) {
            url = "/web/js/client/login";
        } else {
            url = "/web/js/client/inbox";
        }

        unicodeEscapeUrl(request, response, url);
    }

    @RequestMapping("/js/test.js")
    public void test(OutputStream out) throws IOException {

        out.write("document.write(\"".getBytes());
        String message = "test";
        unicodeEscapeSequence(message, out, null);
        out.write("\")".getBytes());
    }

    /**
     * Encode a String into unicode escaped sequence
     * @param source
     * @param out Output will be written to this OutputStream if provided
     * @param writer Output will be printed to this Writer if provided
     * @return The resulting escaped sequence
     * @throws java.io.IOException
     */
    protected String unicodeEscapeSequence(String source, OutputStream out, Writer writer) throws IOException {
        if (source == null) {
            return null;
        }
        String result = "";
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            String escaped = unicodeChar(c);
            result += escaped;
            if (out != null) {
                out.write(escaped.getBytes());
            } else if (writer != null) {
                writer.write(escaped);
            }
        }
        return result;

    }

    /**
     * Encode a single character into unicode escaped sequence
     * @param c
     * @return
     */
    protected String unicodeChar(char c) {
        String pre = "";
        String hex = Integer.toHexString(c);
        switch (hex.length()) {
            case 1:
                pre = "000";
                break;
            case 2:
                pre = "00";
                break;
            case 3:
                pre = "0";
                break;
            case 4:
                pre = "";
                break;
        }
        String escaped = "\\u" + pre + hex;
        return escaped;
    }

    /**
     * Encode a URL within the context (using RequestDispatcher.include) into unicode escaped sequence
     * @param writer Output will be printed to this Writer
     * @param request
     * @param response
     * @param url
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    protected void unicodeEscapeUrl(HttpServletRequest request, HttpServletResponse response, String url) throws IOException, ServletException {
        PrintWriter writer = response.getWriter();

        if (request.getParameter("divId") != null && request.getParameter("divId").trim().length() > 0) {
            writer.print("$(\"#" + StringEscapeUtils.escapeHtml(request.getParameter("divId")) + "\").html(\"");
        } else {
            writer.print("document.write(\"");
        }

        HttpServletResponse unicodeResponse = new UnicodeHttpServletResponse(response);
        unicodeResponse.setContentType("application/javascript; charset=UTF-8");
        request.getRequestDispatcher(url).include(request, unicodeResponse);
        writer.println("\")");
    }

    /**
     * Wrapped HttpServletResponse to override the default PrintWriter
     */
    class UnicodeHttpServletResponse extends HttpServletResponseWrapper {

        HttpServletResponse origResponse;
        ServletOutputStream outputStream;
        PrintWriter writer;

        public UnicodeHttpServletResponse(HttpServletResponse response) throws IOException {
            super(response);
            this.origResponse = response;
            PrintWriter origWriter = response.getWriter();
            this.writer = new PrintWriter(new UnicodePrintWriter(origWriter));
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return origResponse.getOutputStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return this.writer;
        }
    }

    /**
     * Wrapped ServletOutputStream to override the default implementation
     */
    class UnicodeOutputStream extends ServletOutputStream {

        ServletOutputStream origOutput;

        public UnicodeOutputStream(ServletOutputStream out) {
            this.origOutput = out;
        }

        @Override
        public void write(int num) throws IOException {
            char c = new String(new byte[]{(byte) num}).charAt(0);
            String escaped = unicodeChar(c);
            origOutput.write(escaped.getBytes());

        }

        @Override
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(byte[] bytes, int offset, int len) throws IOException {
            String source = new String(bytes, offset, len);
            String escaped = unicodeEscapeSequence(source, this.origOutput, null);
            origOutput.write(escaped.getBytes());
        }

        @Override
        public void print(String str) throws IOException {
            unicodeEscapeSequence(str, this.origOutput, null);
        }

        @Override
        public void println(String str) throws IOException {
            unicodeEscapeSequence(str, this.origOutput, null);
            this.origOutput.println();
        }

        @Override
        public boolean isReady() {
            return origOutput.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            origOutput.setWriteListener(writeListener);
        }
    }

    /**
     * Wrapped Writer to override the default implementation
     */
    class UnicodePrintWriter extends Writer {

        PrintWriter origWriter;

        public UnicodePrintWriter(PrintWriter writer) {
            this.origWriter = writer;
        }

        @Override
        public void write(int num) throws IOException {
            char c = new String(new byte[]{(byte) num}).charAt(0);
            String escaped = unicodeChar(c);
            origWriter.write(escaped);

        }

        @Override
        public void write(char[] cbuf) throws IOException {
            String source = new String(cbuf, 0, cbuf.length);
            unicodeEscapeSequence(source, null, this.origWriter);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String source = new String(cbuf, off, len);
            unicodeEscapeSequence(source, null, this.origWriter);
        }

        @Override
        public void write(String str) throws IOException {
            String source = str;
            unicodeEscapeSequence(source, null, this.origWriter);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            String source = str.substring(off, off + len);
            unicodeEscapeSequence(source, null, this.origWriter);
        }

        @Override
        public void flush() throws IOException {
            this.origWriter.flush();
        }

        @Override
        public void close() throws IOException {
            this.origWriter.close();
        }

        public void print(String str) throws IOException {
            unicodeEscapeSequence(str, null, this.origWriter);
        }

        public void println(String str) throws IOException {
            unicodeEscapeSequence(str, null, this.origWriter);
            this.origWriter.println();
        }
    }
}
