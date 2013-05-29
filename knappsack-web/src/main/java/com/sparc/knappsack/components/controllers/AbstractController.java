package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EntityService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AbstractController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        String errorId = LoggingUtil.generateErrorId();
        String errorMessage = generateLogErrorMessage(errorId) + "- Generic Exception";

        log.error(errorMessage, ex);
        return createModelAndView(request, ex, "redirect:/error", errorId);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ModelAndView handleEntityNotFoundException(Exception ex, HttpServletRequest request) {
        return handleGenericException(ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return createModelAndView(request, ex, "redirect:/403", "");
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

    private ModelAndView createModelAndView(HttpServletRequest request, Exception ex, String viewName, String errorId) {
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

        if (StringUtils.hasText(errorId)) {
            modelAndView.getModel().put("errorId", errorId);
        }

        return modelAndView;
    }

    private String generateLogErrorMessage(String errorId) {
        User user = userService.getUserFromSecurityContext();
        return LoggingUtil.generateLogErrorMessage(user, errorId);
    }

}
