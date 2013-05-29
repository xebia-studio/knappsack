package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.UserRole;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:**/spring/test-applicationContext.xml"})
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
public abstract class AbstractServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    private User user;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired(required = true)
    private UserService userService;

    @Before
    public void setup() {
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return UserRole.ROLE_ADMIN.toString();
            }
        };
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(grantedAuthority);
        user = getUserWithSecurityContext();
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, "1234", grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    protected User getUserWithSecurityContext() {
        if (user == null) {
            MessageDigestPasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("SHA-256", true);
            user = createUser("user@knappsack.com", true, false);
        }

        return user;
    }

    protected void setActiveOrganizationOnUserInSecurityContext(Organization organization) {
        User securityContextUser = getUserWithSecurityContext();
        securityContextUser.setActiveOrganization(organization);
        userService.update(securityContextUser);
        userService.updateSecurityContext(securityContextUser);

        user = securityContextUser;
    }

    protected User createUser(String email, boolean activated, boolean passwordExpired) {
        MessageDigestPasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("SHA-256", true);
        User user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(email);
        user.setPassword(passwordEncoder.encodePassword("1234", user.getEmail()));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setActivated(activated);
        user.setPasswordExpired(passwordExpired);
        user.getUuid();

        userService.add(user);
        assertTrue(userService.doesEntityExist(user.getId()));

        return user;
    }

    protected void setActiveOrganizationOnUser(User user, Organization organization) {
        user.setActiveOrganization(organization);

        userService.update(user);
        assertTrue(userService.doesEntityExist(user.getId()));
        assertTrue(userService.getUsersByActiveOrganization(organization).contains(user));
    }
}
