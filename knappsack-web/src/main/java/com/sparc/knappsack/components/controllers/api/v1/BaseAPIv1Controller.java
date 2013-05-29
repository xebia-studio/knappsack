package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EntityService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.models.api.v1.Error;
import com.sparc.knappsack.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseAPIv1Controller {

    private static final Logger log = LoggerFactory.getLogger(BaseAPIv1Controller.class);

    public static final String contentType = "application/json";

    @Autowired(required = true)
    private UserService userService;

    @Qualifier("messageSource")
    @Autowired(required = true)
    private MessageSource messageSource;

    @SuppressWarnings("unused")
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public
    @ResponseBody
    Error handleGenericException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        String errorId = LoggingUtil.generateErrorId();
        String errorMessage = generateLogErrorMessage(errorId) + " - Processing error";
        log.error(errorMessage, ex);

        Error error = new Error();
        error.setHttpStatusCode("500");
        error.setDeveloperMessage(messageSource.getMessage("api.genericException.developerMessage", null, request.getLocale()));
        error.setUserMessage(messageSource.getMessage("api.genericException.userMessage", null, request.getLocale()));
        error.setMoreInfo("support@knappsack.com");
        error.setErrorId(errorId);

        response.setStatus(500);

        return error;
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public
    @ResponseBody
    Error handleEntityNotFoundException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        String errorId = LoggingUtil.generateErrorId();
        String errorMessage = generateLogErrorMessage(errorId) + " - No entity found";
        log.error(errorMessage, ex);

        Error error = new Error();
        error.setHttpStatusCode("400");
        error.setDeveloperMessage(messageSource.getMessage("api.entityNotFoundException.developerMessage=", null, request.getLocale()));
        error.setUserMessage(messageSource.getMessage("api.entityNotFoundException.userMessage", null, request.getLocale()));
        error.setMoreInfo("support@knappsack.com");
        error.setErrorId(errorId);

        response.setStatus(400);

        return error;
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public
    @ResponseBody
    Error handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, HttpServletResponse response) {
        String errorId = LoggingUtil.generateErrorId();
        String errorMessage = generateLogErrorMessage(errorId) + " - Access Denied";
        log.error(errorMessage, ex);

        Error error = new Error();
        error.setHttpStatusCode("403");
        error.setDeveloperMessage(messageSource.getMessage("api.accessDenied.developerMessage", null, request.getLocale()));
        error.setUserMessage(messageSource.getMessage("api.accessDenied.userMessage", null, request.getLocale()));
        error.setMoreInfo("support@knappsack.com");
        error.setErrorId(errorId);

        response.setStatus(403);

        return error;
    }

    protected void checkRequiredEntity(EntityService entityService, Long id) {
        if (entityService != null) {
            if (!entityService.doesEntityExist(id)) {
                throw new EntityNotFoundException();
            }
        } else {
            throw new RuntimeException("EntityService not found while checking if entity exists");
        }
    }

    private String generateLogErrorMessage(String errorId) {
        User user = userService.getUserFromSecurityContext();
        return LoggingUtil.generateLogErrorMessage(user, errorId);
    }

}
