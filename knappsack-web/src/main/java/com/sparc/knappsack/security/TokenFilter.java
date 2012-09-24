package com.sparc.knappsack.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class TokenFilter extends AbstractAuthenticationProcessingFilter {

    @Value("${singleuse.token.validity.seconds}")
    private int tokenValiditySecond;

    @Autowired(required = true)
    private SingleUseTokenRepository tokenRepository;

    private static final String DEFAULT_FILTER_PROCESSES_URL = "/ios/downloadApplication/**";
    private static final String LOGIN_URL = "/auth/login";
    private static final String GET = "GET";

    private SimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler = new SimpleUrlAuthenticationSuccessHandler();

    protected TokenFilter() {
        super(DEFAULT_FILTER_PROCESSES_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException,
            IOException, ServletException {

        boolean authenticated = false;
        request.getSession(false).setAttribute("continueAttribute", true);

        String tokenParam = request.getParameter("token");

        if (tokenParam != null && !tokenParam.isEmpty()) {
            SingleUseToken token = tokenRepository.getToken(tokenParam);

            if (token != null && (token.getDate().getTime() + tokenValiditySecond*1000 >= System.currentTimeMillis())) {
                authenticated = true;
            } else {
                authenticated = false;
            }
        }

        Collection<GrantedAuthority> grantedAuthorityList;

        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
        grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(grantedAuthority);

        Authentication authentication = new AnonymousAuthenticationToken("key", "anonymousUser", grantedAuthorityList);
        authentication.setAuthenticated(authenticated);

        authenticationSuccessHandler.setDefaultTargetUrl((authenticated ? request.getServletPath() : LOGIN_URL));
        this.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        return authentication;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        if(request.getMethod().equals(GET) && !request.getServletPath().startsWith("/ios/downloadIOSPlist/")) {
            // If the incoming request is a POST, then we send it up
            // to the AbstractAuthenticationProcessingFilter.
            super.doFilter(request, response, chain);
        } else {
            // If it's a GET, we ignore this request and send it
            // to the next filter in the chain.  In this case, that
            // pretty much means the request will hit the /login
            // controller which will process the request to show the
            // login page.
            chain.doFilter(request, response);
        }
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Object continueAttribute = request.getSession(false).getAttribute("continueAttribute");
        if (request.getServletPath().equals("/auth/login")) {
            return false;
        } else if (request.getServletPath().startsWith("/ios/downloadApplication")) {
            return continueAttribute == null;
        }
        return false;
    }

}