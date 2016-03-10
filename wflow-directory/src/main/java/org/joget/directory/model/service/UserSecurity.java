package org.joget.directory.model.service;

import java.util.Collection;
import java.util.Map;
import org.joget.directory.model.User;

/**
 * Interface for user security enhancement.	
 * Used to extend the ability of Directory Manager Plugin when managing local user directory
 */
public interface UserSecurity {
    
    /**
     * Get JSON property options to embed into the property options of Directory Manager Plugin
     * @param 
     * @return the JSON property options String.
     */
    public String getPropertyOptions();
    
    /**
     * Set properties to use in User Security Implementation 
     * @param properties
     * @return 
     */
    public void setProperties(Map<String, Object>  properties);
    
    /**
     * Get properties to use in User Security Implementation 
     * @param 
     * @return properties
     */
    public Map<String, Object> getProperties();
    
    /**
     * Disable Hash Login?
     * @param 
     * @return boolean
     */
    public Boolean getDisableHashLogin();
    
    /**
     * Allow Session Timeout?
     * @param 
     * @return boolean
     */
    public Boolean getAllowSessionTimeout();
    
    /**
     * Force Session Timeout? 
     * @param 
     * @return boolean
     */
    public Boolean getForceSessionTimeout();
    
    /**
     * Authenticate all API?
     * @param 
     * @return boolean
     */
    public Boolean getAuthenticateAllApi();
    
    /**
     * Encrypt Password with custom methodology.
     * @param username
     * @param password
     * @return the encrypted password String.
     */
    public String encryptPassword (String username, String password);
    
    /**
     * Verify Password with custom methodology.
     * @param user
     * @param password
     * @return verified Boolean.
     */
    public Boolean verifyPassword (User user, String password);
    
    /**
     * To get a set of password policies to display in user profile form.
     * @param 
     * @return Collection of policies String.
     */
    public Collection<String> passwordPolicies ();
    
    /**
     * To validate password format and validity.
     * @param username
     * @param old password
     * @param new password
     * @param confirm new password
     * @return Collection of error message String.
     */
    public Collection<String> validatePassword (String username, String oldPassword, String newPassword, String confirmPassword);
    
    /**
     * To validate user before insert to database
     * @param user
     * @return 
     */
    public Collection<String> validateUserOnInsert(User user);
    
    /**
     * To validate user before updating
     * @param user
     * @return 
     */
    public Collection<String> validateUserOnUpdate(User user);
    
    /**
     * To validate user before updating profile
     * @param user
     * @return 
     */
    public Collection<String> validateUserOnProfileUpdate(User user);
    
    /**
     * Processing after a user is added.
     * @param user
     * @return 
     */
    public void insertUserPostProcessing(User user);
    
    /**
     * Processing after a user is modified.
     * @param user
     * @return 
     */
    public void updateUserPostProcessing(User user);
    
    /**
     * Processing after a user profile is updated.
     * @param user
     * @return 
     */
    public void updateUserProfilePostProcessing(User user);
    
    /**
     * Processing after a user password is reset.
     * @param user
     * @return 
     */
    public void passwordResetPostProcessing(User user);
    
    /**
     * Processing after a user is deleted.
     * @param username
     * @return 
     */
    public void deleteUserPostProcessing(String username);
    
    /**
     * HTML to add below login form
     * @param 
     * @return 
     */
    public String getLoginFormFooter();
    
    /**
     * HTML to add below user creation form
     * @param 
     * @return 
     */
    public String getUserCreationFormFooter();
    
    /**
     * HTML to add below user editing form
     * @param 
     * @return 
     */
    public String getUserEditingFormFooter(User user);
    
    /**
     * HTML to add below user profile form
     * @param 
     * @return 
     */
    public String getUserProfileFooter(User user);
    
    /**
     * HTML to extend user details page button
     * @param 
     * @return 
     */
    public String getUserDetailsButtons(User user);
    
    /**
     * Processing before user login authentication.
     * @param user
     * @param password
     * @return 
     */
    public void loginPreProcessing(User user, String password);
    
    /**
     * Processing after user login authentication.
     * @param user
     * @param password
     * @param logged in
     * @return 
     */
    public void loginPostProcessing(User user, String password, Boolean loggedIn);
    
    /**
     * Processing after HTTP Request Completed.
     * @param 
     * @return 
     */
    public void requestPostProcessing();
    
    /**
     * Check whether a custom security data is exist for a user.
     * @param 
     * @return 
     */
    public boolean isDataExist(String username);
}
