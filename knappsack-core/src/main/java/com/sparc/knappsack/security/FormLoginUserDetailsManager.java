package com.sparc.knappsack.security;

import com.sparc.knappsack.components.dao.UserDetailsDao;
import com.sparc.knappsack.components.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

public class FormLoginUserDetailsManager implements UserDetailsManager {

    private static final Logger logger = LoggerFactory.getLogger(FormLoginUserDetailsManager.class);

    private AuthenticationManager authenticationManager;

    private UserDetailsDao userDetailsDao;

    public FormLoginUserDetailsManager(UserDetailsDao userDetailsDao) {
        this.userDetailsDao = userDetailsDao;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.springframework.security.core.userdetails.UserDetails user = userDetailsDao.findByUserName(username.toLowerCase());

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return user;
    }

    @Override
    public void createUser(org.springframework.security.core.userdetails.UserDetails user) {
        if (!userExists(user.getUsername())) {
            userDetailsDao.add((User)user);
        }
    }

    @Override
    public void updateUser(org.springframework.security.core.userdetails.UserDetails user) {
        Assert.isTrue(userExists(user.getUsername()));

        userDetailsDao.update((User) user);
    }

    @Override
    public void deleteUser(String username) {
        User user = userDetailsDao.findByUserName(username);
        if (user != null) {
            userDetailsDao.delete(user);
        }
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

        if (currentUser == null) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException("Can't change password as no Authentication object found in context " +
                    "for current user.");
        }

        String username = currentUser.getName();

        logger.info("Changing password for user '" + username + "'");

        // If an authentication manager has been set, re-authenticate the user with the supplied password.
        if (authenticationManager != null) {
            logger.info("Reauthenticating user '" + username + "' for password change request.");

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        } else {
            logger.info("No authentication manager set. Password won't be re-checked.");
        }

        User user = userDetailsDao.findByUserName(username);

        if (user == null) {
            throw new IllegalStateException("Current user doesn't exist in database.");
        }

        user.setPassword(newPassword);
        userDetailsDao.update(user);
    }

    @Override
    public boolean userExists(String username) {
        return (userDetailsDao.findByUserName(username) != null);
    }

    @SuppressWarnings("unused")
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @SuppressWarnings("unused")
    public void setUserDetailsDao(UserDetailsDao userDetailsDao) {
        this.userDetailsDao = userDetailsDao;
    }
}
