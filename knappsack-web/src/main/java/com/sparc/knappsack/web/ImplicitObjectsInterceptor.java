package com.sparc.knappsack.web;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.properties.SystemProperties;
import com.sparc.knappsack.util.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ImplicitObjectsInterceptor extends HandlerInterceptorAdapter {

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Value("${" + SystemProperties.APPLICATION_DOMAIN + "}")
    private String applicationDomain = "";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession();
        boolean redirect = false;

        if (session != null) {
            SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
            if (context != null) {
                if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() != null && context.getAuthentication().getPrincipal() instanceof User) {
                    User user = userService.getUserFromSecurityContext();

                    String servletPath = request.getServletPath();

                    if (!servletPath.startsWith("/auth") && !servletPath.startsWith("/resources") && user != null) {
                        boolean skipValidation = false;

                        if (user.isPasswordExpired() && !servletPath.startsWith("/profile/changePassword") && !servletPath.startsWith("/auth/forgotPassword")) {
                            response.sendRedirect(request.getContextPath() + "/profile/changePassword");
                            skipValidation = true;
                            redirect = true;
                        } else if (user.isPasswordExpired() && (servletPath.startsWith("/profile/changePassword") || servletPath.startsWith("/auth/forgotPassword"))) {
                            skipValidation = true;
                        }

                        if (!user.isActivated() && !servletPath.startsWith("/activate") && !skipValidation) {
                            response.sendRedirect(request.getContextPath() + "/activate");
                            redirect = true;
                        }
                    }
                }
            }
        }

        // We do not want the healthcheck to ever populate the WebRequest object
        if (request != null && !StringUtils.startsWithIgnoreCase(request.getServletPath(), "/healthcheck")) {
            String serverName;
            //If ApplicationDomain is set on the properties then use that as the server name, else use what came off the request
            if (StringUtils.hasText(applicationDomain) && !("${" + SystemProperties.APPLICATION_DOMAIN + "}").equalsIgnoreCase(applicationDomain)) {
                serverName = applicationDomain;
            } else {
                serverName = request.getServerName();
            }
            WebRequest.getInstance(request.getScheme(), serverName, request.getServerPort(), request.getContextPath());
        }

        if (redirect) {
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

        if (response != null) {
            response.setHeader("X-Frame-Options", "DENY");
        }

        if (request != null && modelAndView != null) {
            User user = userService.getUserFromSecurityContext();
            if (user == null) {
                HttpSession session = request.getSession();
                if (session != null) {
                    SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
                    if (context != null) {
                        if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() != null && context.getAuthentication().getPrincipal() instanceof User) {
                            user = userService.get(((User) context.getAuthentication().getPrincipal()).getId());
                        }
                    }
                }
            }
            modelAndView.getModel().put("user", user);
        }
    }

}
