package com.sparc.knappsack.web;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DisabledDomainInterceptor extends HandlerInterceptorAdapter {

    @Autowired(required = true)
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = userService.getUserFromSecurityContext();
        if (user == null || user.isSystemAdmin()) {
            return true;
        }

        String servletPath = request.getServletPath();
        Organization organization = user.getActiveOrganization();
        if (organization != null && organization.getDomainConfiguration().isDisabledDomain()) {
            if (!servletPath.startsWith("/manager/organization/accountManagement")
                    && !servletPath.startsWith("/disabled")
                    && !servletPath.startsWith("/activeOrganization")
                    && !servletPath.startsWith("/image")
                    && !servletPath.startsWith("/getSystemNotifications")
                    && !servletPath.startsWith("/contacts")
                    && !servletPath.startsWith("/profile/changePassword")
                    && !servletPath.startsWith("/profile/resetPassword")
                    && !servletPath.startsWith("/legal")
                    && !servletPath.startsWith("/activate")
                    && !servletPath.startsWith("/manager/modifyBandwidthBilling")
                    && !servletPath.startsWith("/error")
                    && !servletPath.startsWith("/403")
                    && !servletPath.startsWith("/404")) {
                response.sendRedirect(request.getContextPath() + "/disabled");
                return false;
            }
        }

        return true;
    }

}
