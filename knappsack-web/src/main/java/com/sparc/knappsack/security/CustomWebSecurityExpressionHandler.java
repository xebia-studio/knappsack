package com.sparc.knappsack.security;

import com.sparc.knappsack.components.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {
    @Autowired(required = true)
    private UserService userService;

    @Override
    protected SecurityExpressionRoot createSecurityExpressionRoot(Authentication authentication, FilterInvocation invocation) {
        CustomWebSecurityExpressionRoot root = new CustomWebSecurityExpressionRoot(authentication, invocation);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setUserService(userService);

        return root;
    }
}
