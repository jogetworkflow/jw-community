package org.kecak.apps.directory.service;

import io.jsonwebtoken.*;
import org.joget.apps.app.service.AppUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Deprecated
public class TokenAuthenticationService {

    static final long EXPIRATIONTIME = 864_000_000; // 10 days
    //	static final long EXPIRATIONTIME = 6000;
    static final String SECRET = "ThisIsASecret";
    static final String BEARER_AUTH_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";
    static final String ISSUER = "Kecak";

    public static String addAuthentication(String username) {
        String jwt = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS256, getSecret())
                .setIssuer(ISSUER)
                .compact();
        return jwt;
    }

    public static String getSecret() {
        return SECRET;
    }

    /**
     * Get Authenticated User based on token
     *
     * @param bearerToken
     * @return
     * @throws AuthenticationException
     */
    public static User getAuthentication(String bearerToken) throws AuthenticationException {
        if (bearerToken != null) {
            String user = Jwts.parser()
                    .setSigningKey(getSecret())
                    .parseClaimsJws(bearerToken)
                    .getBody().getSubject();
            if (user.equals("")) {
                return null;
            } else {
                ApplicationContext ac = AppUtil.getApplicationContext();
                ExtDirectoryManager dm = (ExtDirectoryManager) ac.getBean("directoryManager");
                WorkflowUserManager wfUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
                wfUserManager.setCurrentThreadUser(user);
                return dm.getUserByUsername(user);
            }
        } else {
            // get from web session
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                if (!"roleAnonymous".equals(username)) {
                    ApplicationContext ac = AppUtil.getApplicationContext();
                    ExtDirectoryManager dm = (ExtDirectoryManager) ac.getBean("directoryManager");
                    return dm.getUserByUsername(username);
                }
            }
        }
        return null;
    }

    /**
     * Get Claims based on HTTP request
     *
     * @param request
     * @return
     */
    public static Claims getClaims(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null && !token.isEmpty() && !token.contains("NOT FOUND")) {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(getSecret())
                    .parseClaimsJws(token.replace(BEARER_AUTH_PREFIX, ""));

           return claims.getBody();
        }
        return null;
    }
}
