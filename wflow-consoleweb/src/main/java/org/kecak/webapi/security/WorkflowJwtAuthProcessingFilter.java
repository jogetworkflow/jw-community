package org.kecak.webapi.security;


import io.jsonwebtoken.ExpiredJwtException;
import org.kecak.apps.app.service.AuthTokenService;
import org.joget.apps.workflow.security.WorkflowUserDetails;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class WorkflowJwtAuthProcessingFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public final static String TOKEN_HEADER = "Authorization";

    private AuthTokenService authTokenService;
    private DirectoryManager directoryManager;
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain) throws IOException,
                                    ServletException {
        final boolean debug = logger.isDebugEnabled();

        String header = request.getHeader(this.TOKEN_HEADER);
        if ( header!=null && header.startsWith("Bearer ") ) {
            String authToken = header.substring(7);

            try {
                String username = authTokenService.getUsernameFromToken(authToken);
                logger.info("Authenticating user '{}' ", username);
                User user = directoryManager.getUserByUsername(username);
                if (authTokenService.validateToken(authToken, user)) {
                    Collection<Role> roles = directoryManager.getUserRoles(username);
                    List<GrantedAuthority> gaList = new ArrayList<GrantedAuthority>();
                    if (roles != null && !roles.isEmpty()) {
                        for (Role role : roles) {
                            GrantedAuthority ga = new SimpleGrantedAuthority(role.getId());
                            gaList.add(ga);
                        }
                    }
                    UserDetails details = new WorkflowUserDetails(user);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, gaList);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    logger.info("Authorizated user '{}', setting security context", username);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    chain.doFilter(request, response);
                } else {
                    logger.error("Error when authenticating user");
                    authenticationEntryPoint.commence(request, response, new BadCredentialsException("Error when authenticating user"));
                }
            } catch(ExpiredJwtException e) {
                String refreshToken = authTokenService.generateRefreshToken(e.getClaims().getId(), e.getClaims().getSubject());
                JSONObject jsonResponse = new JSONObject();
                try {
                    jsonResponse.put("status", HttpServletResponse.SC_OK);
                    jsonResponse.put("message", "Error 302: Token expired, please refresh token");
                    jsonResponse.put("ref_token", refreshToken);
                    response.getWriter().write(jsonResponse.toString());
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            } catch(Exception e) {
                LogUtil.error(this.getClass().getName(), e, null);
                authenticationEntryPoint.commence(request, response, new BadCredentialsException(e.getMessage()));
            }
        }
        else {
            chain.doFilter(request, response);
        }
    }

    public void setAuthTokenService(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }
}
