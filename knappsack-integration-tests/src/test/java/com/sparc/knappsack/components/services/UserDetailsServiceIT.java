package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserDetailsServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private UserDetailsService userDetailsService;

    @Autowired(required = true)
    private UserService userService;

    @Test
    public void loadUserByUsernameTest() {
        User user = getUser();
        userService.add(user);

        user = userDetailsService.loadUserByUsername(user.getEmail());
        assertNotNull(user);
    }

//    @Test
//    public void loadUserDetailsTest() {
//        User user = getUser();
//        userService.add(user);
//        List<String> openIdIdentifiers = new ArrayList<String>();
//        openIdIdentifiers.add("testOpenIdIdentifier");
//        user.setOpenIdIdentifiers(openIdIdentifiers);
//
//        OpenIDAuthenticationToken token = mock(OpenIDAuthenticationToken.class);
//        when(token.getName()).thenReturn("testOpenIdIdentifier");
//        List<OpenIDAttribute> attributes = new ArrayList<OpenIDAttribute>();
//        OpenIDAttribute attribute = new OpenIDAttribute("email", user.getEmail());
//        attributes.add(attribute);
//        when(token.getAttributes()).thenReturn(attributes);
//
//        UserDetails userDetails = userDetailsService.loadUserDetails(token);
//        assertNotNull(userDetails);
//        assertTrue(userDetails.getUsername().equals(user.getEmail()));
//    }

    @Test
    public void loadUserDetailsByEmailTest() {
        User user = getUser();
        userService.add(user);
        Set<String> openIdIdentifiers = new HashSet<String>();
        openIdIdentifiers.add("oldOpenIdIdentifier");
        user.setOpenIdIdentifiers(openIdIdentifiers);

        OpenIDAuthenticationToken token = mock(OpenIDAuthenticationToken.class);
        when(token.getName()).thenReturn("newOpenIdIdentifier");
        List<OpenIDAttribute> attributes = new ArrayList<OpenIDAttribute>();
        List<String> attributeValues = new ArrayList<String>();
        attributeValues.add(user.getEmail());
        OpenIDAttribute attribute = new OpenIDAttribute("email", "", attributeValues);
        attributes.add(attribute);
        when(token.getAttributes()).thenReturn(attributes);

        UserDetails userDetails = userDetailsService.loadUserDetails(token);
        assertNotNull(userDetails);
        assertTrue(userDetails.getUsername().equals(user.getEmail()));
    }
}
