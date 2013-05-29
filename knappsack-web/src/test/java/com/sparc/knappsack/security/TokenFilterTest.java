package com.sparc.knappsack.security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TokenFilterTest {

    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;

    @Mock(name = "singleUseTokenRepositoryImpl")
    private SingleUseTokenRepository tokenRepository = Mockito.mock(SingleUseTokenRepository.class);

    @InjectMocks
    private TokenFilter tokenFilter = new TokenFilter();

    @Before
    public void before() throws Exception {
        httpServletRequest = getHttpServletRequest();
        httpServletResponse = getHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
     */
    @Test
    public void testAttemptAuthentication() throws Exception {
        Authentication authentication = tokenFilter.attemptAuthentication(httpServletRequest, httpServletResponse);
        assertFalse(authentication.isAuthenticated());

        String tokenString = "testToken";

        Mockito.when(httpServletRequest.getParameter("token")).thenReturn(tokenString);
        SingleUseToken token = Mockito.mock(SingleUseToken.class);
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        date.add(Calendar.YEAR, 1);
        Mockito.when(token.getDate()).thenReturn(date.getTime());
        Mockito.when(tokenRepository.getToken(tokenString)).thenReturn(token);
        Mockito.when(httpServletRequest.getServletPath()).thenReturn("/auth/login");
        authentication = tokenFilter.attemptAuthentication(httpServletRequest, httpServletResponse);
        assertTrue(authentication.isAuthenticated());
    }

    /**
     * Method: doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
     */
    @Test
    public void testDoFilter() throws Exception {
        //TODO: implement test
    }

    /**
     * Method: requiresAuthentication(HttpServletRequest request, HttpServletResponse response)
     */
//    @Test
//    public void testRequiresAuthentication() throws Exception {
//        SecurityContextHolder.getContext().setAuthentication(null);
//        boolean isRequiredAuth = tokenFilter.requiresAuthentication(httpServletRequest, httpServletResponse);
//        assertTrue(isRequiredAuth);
//
//        HttpSession session = Mockito.mock(HttpSession.class);
//
//        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test", "test"));
//        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
//        Mockito.when(httpServletRequest.getServletPath()).thenReturn("/auth/login");
//        isRequiredAuth = tokenFilter.requiresAuthentication(httpServletRequest, httpServletResponse);
//        assertFalse(isRequiredAuth);
//
//        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
//        Mockito.when(httpServletRequest.getServletPath()).thenReturn("/ios/downloadApplication");
//        isRequiredAuth = tokenFilter.requiresAuthentication(httpServletRequest, httpServletResponse);
//        assertTrue(isRequiredAuth);
//
//        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
//        Mockito.when(session.getAttribute("continueAttribute")).thenReturn(new Object());
//        isRequiredAuth = tokenFilter.requiresAuthentication(httpServletRequest, httpServletResponse);
//        assertFalse(isRequiredAuth);
//    }

    private HttpServletRequest getHttpServletRequest() {
        HttpSession httpSession = Mockito.mock(HttpSession.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameterValues(Matchers.eq("name"))).thenReturn(new String[]{"John", "Doe"});
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(request.getSession(false)).thenReturn(httpSession);

        return request;
    }

    private HttpServletResponse getHttpServletResponse() {
        return Mockito.mock(HttpServletResponse.class);
    }


} 
