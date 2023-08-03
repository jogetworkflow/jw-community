package org.joget.directory.model.service;

import org.joget.directory.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface ExtUserDetails extends UserDetails {
    
    public User getUser();
    
    public void updateUser(User user);
}
