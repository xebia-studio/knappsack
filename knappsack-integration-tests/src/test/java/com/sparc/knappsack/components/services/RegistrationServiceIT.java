package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.UserModel;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RegistrationServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private RegistrationService registrationService;

    @Autowired(required = true)
    private InvitationService invitationService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private DomainService domainService;

    @Autowired(required = true)
    private RoleService roleService;

    @Test
    public void registerUserTest() {
        UserModel userModel = new UserModel();
        userModel.setEmail("test@test.com");
        userModel.setFirstName("John");
        userModel.setLastName("Doe");
        userModel.setPassword("password");

        User user = registrationService.registerUser(userModel, false);
        assertNotNull(user);
        assertTrue(user.getEmail().equals("test@test.com"));
        assertTrue(user.getFirstName().equals("John"));
        assertTrue(user.getLastName().equals("Doe"));
    }

    @Test
    public void registerUserWithInvitationTest() {
        User user = getUser();

        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Domain orgDomain = domainService.get(organization.getId(), DomainType.ORGANIZATION);
        Assert.assertNotNull(orgDomain);
        Assert.assertTrue(orgDomain.getName().equals("Test Organization"));

        Invitation invitation = new Invitation();
        invitation.setDomainId(organization.getId());
        invitation.setDomainType(DomainType.ORGANIZATION);
        invitation.setEmail(user.getEmail());
        Role role = roleService.getRoleByAuthority(UserRole.ROLE_ORG_USER.toString());
        invitation.setRole(role);
        invitationService.add(invitation);

        UserModel userModel = new UserModel();
        userModel.setEmail("test@test.com");
        userModel.setFirstName("John");
        userModel.setLastName("Doe");
        userModel.setPassword("password");

        user = registrationService.registerUser(userModel, false);
        assertNotNull(user);
        assertTrue(user.getEmail().equals("test@test.com"));
        assertTrue(user.getFirstName().equals("John"));
        assertTrue(user.getLastName().equals("Doe"));
    }
}
