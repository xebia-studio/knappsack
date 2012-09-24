package com.sparc.knappsack.security;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

public class CustomWebSecurityExpressionRoot extends SecurityExpressionRoot {
    //private FilterInvocation filterInvocation;
    /** Allows direct access to the request object */
    public final HttpServletRequest request;

    private UserService userService;

    public CustomWebSecurityExpressionRoot(Authentication a, FilterInvocation fi) {
        super(a);
        this.request = fi.getRequest();
    }

    public boolean isSystemAdmin() {
        User user = userService.getUserFromSecurityContext();
        return user != null && user.isSystemAdmin();
    }

    public boolean isGroupAdmin() {
        User user = userService.getUserFromSecurityContext();
        return user != null && user.isGroupAdmin();
    }

    public boolean isOrganizationAdmin() {
        User user = userService.getUserFromSecurityContext();
        return user != null && user.isOrganizationAdmin();
    }

    public boolean isSystemOrOrganizationAdmin() {
        User user = userService.getUserFromSecurityContext();
        return user != null && user.isSystemOrOrganizationAdmin();
    }

    public boolean isOrganizationOrGroupAdmin() {
        User user = userService.getUserFromSecurityContext();
        return user != null && user.isOrganizationOrGroupAdmin();
    }

    public boolean isAnyAdmin() {
        User user = userService.getUserFromSecurityContext();
        return user != null && user.isAnyAdmin();
    }

    void setUserService(UserService userService) {
        this.userService = userService;
    }
}
