package org.joget.workflow.model.service;

import org.joget.directory.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    
    User getUser();
}
