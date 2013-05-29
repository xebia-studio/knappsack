package com.sparc.knappsack.security;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

public class CustomWebSecurityExpressionRoot extends SecurityExpressionRoot {
    //private FilterInvocation filterInvocation;
    /** Allows direct access to the request object */
    public final HttpServletRequest request;

    private UserService userService;
    private SingleUseTokenRepository singleUseTokenRepository;

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

    public boolean hasValidIOSToken() {
        if (request == null || !isValidRequestMethod(request, RequestMethod.GET, RequestMethod.HEAD)) {
            return false;
        }

        return singleUseTokenRepository.validateAndExpireTokenForKey(request.getParameter("token"), (RequestMethod.valueOf(request.getMethod()) == RequestMethod.HEAD ? false : true));
    }

    private boolean isValidRequestMethod(HttpServletRequest request, RequestMethod... requestMethods) {
        if (request != null && StringUtils.hasText(request.getMethod()) && requestMethods != null) {
            for (RequestMethod requestMethod : requestMethods) {
                if (request.getMethod().equalsIgnoreCase(requestMethod.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    void setUserService(UserService userService) {
        this.userService = userService;
    }

    void setSingleUseTokenRepository(SingleUseTokenRepository singleUseTokenRepository) {
        this.singleUseTokenRepository = singleUseTokenRepository;
    }
}
