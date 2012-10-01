package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EntityService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AbstractController {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        return createModelAndView(request, ex, "errorTH");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(Exception ex, HttpServletRequest request) {
        return handleGenericException(ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return createModelAndView(request, ex, "statusCodes/403TH");
    }

    public void checkRequiredEntity(EntityService entityService, Long id) {
        if (entityService != null) {
            if (!entityService.doesEntityExist(id)) {
                throw new EntityNotFoundException();
            }
        } else {
            throw new RuntimeException("EntityService not found while checking if entity exists");
        }
    }

    private ModelAndView createModelAndView(HttpServletRequest request, Exception ex, String viewName) {
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.getModel().put("exception", ex);

        User user = userService.getUserFromSecurityContext();
        if (user == null) {
            HttpSession session = request.getSession(false);
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

        return modelAndView;
    }

}
