package com.sparc.knappsack.security;

import com.sparc.knappsack.exceptions.OpenIDUserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class OpenIDAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${valid.google.apps.domains}")
    private String[] validGoogleAppsDomains;

    private String openIdRegistrationUrl;

    @SuppressWarnings("unused")
    public String getOpenIdRegistrationUrl() {
        return openIdRegistrationUrl;
    }

    public void setOpenIdRegistrationUrl(String openIdRegistrationUrl) {
        this.openIdRegistrationUrl = openIdRegistrationUrl;
    }

    @Autowired(required = true)
    private NormalizedOpenIdAttributesBuilder normalizedOpenIdAttributesBuilder;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        try {
            if (openIdAuthenticationSuccessfulButUserIsNotRegistered(exception)) {
                redirectToOpenIdRegistrationUrl(request, response, exception);
            } else {
                super.onAuthenticationFailure(request, response, exception);
            }
        } catch (AuthenticationException e) {
            super.onAuthenticationFailure(request, response, e);
        }
    }

    private void redirectToOpenIdRegistrationUrl(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        addOpenIdAttributesToSession(request, getOpenIdAuthenticationToken((OpenIDUserNotFoundException) exception));
        redirectStrategy.sendRedirect(request, response, openIdRegistrationUrl);
    }

    private void addOpenIdAttributesToSession(HttpServletRequest request, OpenIDAuthenticationToken openIdAuthenticationToken) throws ServletException {
        HttpSession session = request.getSession();
        sessionShouldBePresent(session);
        NormalizedOpenIdAttributes normalizedOpenIdAttributes = normalizedOpenIdAttributesBuilder.build(openIdAuthenticationToken);
        session.setAttribute("openid", normalizedOpenIdAttributes);
    }

    private void sessionShouldBePresent(HttpSession session) throws ServletException {
        if (session == null) {
            throw new ServletException("No session found");
        }
    }

    private boolean openIdAuthenticationSuccessfulButUserIsNotRegistered(AuthenticationException exception) {
        boolean success;

        if (exception instanceof OpenIDUserNotFoundException &&
                getOpenIdAuthenticationToken((OpenIDUserNotFoundException) exception) instanceof OpenIDAuthenticationToken &&
                OpenIDAuthenticationStatus.SUCCESS.equals((getOpenIdAuthenticationToken((OpenIDUserNotFoundException) exception)).getStatus())) {
            success = true;
        } else {
            return false;
        }

        return success;

    }

    private OpenIDAuthenticationToken getOpenIdAuthenticationToken(OpenIDUserNotFoundException exception) {
        return exception.getOpenIDAuthenticationToken();
    }
}
