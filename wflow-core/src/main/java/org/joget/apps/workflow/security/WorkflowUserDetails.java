package org.joget.apps.workflow.security;

import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.util.WorkflowUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.joget.directory.model.service.ExtUserDetails;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class WorkflowUserDetails implements ExtUserDetails {

    private User user;
    private Collection<GrantedAuthority> authorities = null;

    public WorkflowUserDetails(User user) {
        super();
        this.user = user;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            try {
                ApplicationContext appContext = WorkflowUtil.getApplicationContext();
                DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
                Collection<Role> roles = directoryManager.getUserRoles(user.getUsername());
                List<GrantedAuthority> gaList = new ArrayList<GrantedAuthority>();

                if (roles != null && !roles.isEmpty()) {
                    for (Role role : roles) {
                        GrantedAuthority ga = new SimpleGrantedAuthority(role.getId());
                        gaList.add(ga);
                    }
                }

                authorities = gaList;
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
                authorities = new ArrayList<GrantedAuthority>();
            }
        }
        return authorities;
    }
    
    public User getUser() {
        return user;
    }
    
    public void updateUser(User updatedUser) {
        //make sure it is same username before update
        if (updatedUser != null && this.user.getUsername().equals(updatedUser.getUsername())) {
            this.user = updatedUser;
        }
    }

    public String getPassword() {
        return user.getPassword();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return user.getActive() == 1;
    }

    public String getSessionId() {
        String sessionId = getUsername();
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }
}
