package org.owasp.csrfguard.token.storage.impl;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.joget.workflow.util.WorkflowUtil;
import org.owasp.csrfguard.token.storage.Token;
import org.owasp.csrfguard.token.storage.TokenHolder;

public class SessionTokenHolder implements TokenHolder {

    protected final String CSRF_TOKEN_VALUE = "CSRF_TOKEN_VALUE";
    
    @Override
    public void setMasterToken(final String sessionKey, final String value) {
        Token token = getToken(sessionKey);
        if (token != null) {
            token.setMasterToken(value);
            updateToken(token);
        }
    }

    @Override
    public String createMasterTokenIfAbsent(final String sessionKey, final Supplier<String> valueSupplier) {
        Token token = getToken(sessionKey);
        if (token != null) {
            if (token.getMasterToken() == null) {
                token.setMasterToken(valueSupplier.get());
                updateToken(token);
            }
            return token.getMasterToken();
        }
        return "";
    }

    @Override
    public String createPageTokenIfAbsent(final String sessionKey, final String resourceUri, final Supplier<String> valueSupplier) {
        final Token token = getToken(sessionKey);
        if (token != null) {
            if (token.getMasterToken() == null) {
                token.setMasterToken(valueSupplier.get());
            }
            String pageToken = token.setPageTokenIfAbsent(resourceUri, valueSupplier);
            updateToken(token);
            return pageToken;
        }
        return "";
    }

    @Override
    public Token getToken(final String sessionKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Token token = (Token) session.getAttribute(CSRF_TOKEN_VALUE);
                
                if (token == null) {
                    token = new SessionToken();
                    session.setAttribute(CSRF_TOKEN_VALUE, token);
                }
                return token;
            }
        }
        return null;
    }
    
    protected void updateToken(Token token) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute(CSRF_TOKEN_VALUE, token);
            }
        }
    }

    @Override
    public String getPageToken(final String sessionKey, final String resourceUri) {
        final Token token = getToken(sessionKey);

        return Objects.nonNull(token) ? token.getPageToken(resourceUri) : null;
    }

    @Override
    public void setPageToken(final String sessionKey, final String resourceUri, final String value) {
        final Token token = getToken(sessionKey);
        if (token != null) {
            token.setPageToken(resourceUri, value);
            updateToken(token);
        }
    }

    @Override
    public void setPageTokens(final String sessionKey, final Map<String, String> pageTokens) {
        final Token token = getToken(sessionKey);
        if (token != null) {
            token.setPageTokens(pageTokens);
            updateToken(token);
        }
    }

    @Override
    public Map<String, String> getPageTokens(final String sessionKey) {
        final Token token = getToken(sessionKey);

        return Objects.nonNull(token) ? token.getPageTokens() : null;
    }

    @Override
    public void remove(final String sessionKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(CSRF_TOKEN_VALUE);
            }
        }
    }

    @Override
    public void rotateAllPageTokens(final String sessionKey, final Supplier<String> tokenValueSupplier) {
        final Token token = getToken(sessionKey);
        if (token != null) {
            token.rotateAllPageTokens(tokenValueSupplier);
            updateToken(token);
        }
    }

    @Override
    public void regenerateUsedPageToken(final String sessionKey, final String tokenFromRequest, final Supplier<String> tokenValueSupplier) {
        final Token token = getToken(sessionKey);
        if (token != null) {
            token.regenerateUsedPageToken(tokenFromRequest, tokenValueSupplier);
            updateToken(token);
        }
    }
}
