package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.enums.UserRole;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class UserDomainServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    private StorageConfiguration storageConfiguration;

    @Before
    public void setup() {
        super.setup();
        storageConfiguration = getStorageConfiguration();
    }

    @Test
    public void addTest() {
        User user = getUserWithSecurityContext();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        assertNotNull(userDomain.getId());

        UserDomain newUserDomain = userDomainService.get(user, organization.getId());
        assertNotNull(newUserDomain);
        assertEquals(newUserDomain, userDomain);
    }

    @Test
    public void deleteTest() {
        User user = getUserWithSecurityContext();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        assertNotNull(userDomain.getId());

        userDomainService.delete(userDomain.getId());

        assertNull(userDomainService.get(user, organization.getId()));
    }

    @Test
    public void updateTest() {
        User user = getUserWithSecurityContext();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        assertNotNull(userDomain.getId());

        Group group = createGroup(organization);
        userDomain.setDomain(group);
        userDomainService.update(userDomain);

        assertNull(userDomainService.get(user, organization.getId()));

        UserDomain groupUserDomain = userDomainService.get(user, group.getId());
        assertNotNull(groupUserDomain);
        assertEquals(groupUserDomain, userDomain);
    }

    @Test
    public void getAll_ForDomainId_Test() {
        User user1 = createUser("user1@knappsack.com", true, false);
        User user2 = createUser("user2@knappsack.com", true, false);

        Organization organization = createOrganization();
        UserDomain firstUser_UserDomain = addUserToDomain(organization, user1, UserRole.ROLE_ORG_ADMIN);
        UserDomain secondUser_UserDomain = addUserToDomain(organization, user2, UserRole.ROLE_ORG_ADMIN);

        List<UserDomain> returnedUserDomains = userDomainService.getAll(organization.getId());
        assertNotNull(returnedUserDomains);
        assertEquals(returnedUserDomains.size(), 2);

        returnedUserDomains = userDomainService.getAll(null);
        assertNotNull(returnedUserDomains);
        assertEquals(returnedUserDomains.size(), 0);

        returnedUserDomains = userDomainService.getAll(0L);
        assertNotNull(returnedUserDomains);
        assertEquals(returnedUserDomains.size(), 0);
    }

    @Test
    public void get_ForUserDomainIdUserRole_Test() {
        User user1 = createUser("user1@knappsack.com", true, false);
        Organization organization = createOrganization();
        UserDomain firstUser_UserDomain = addUserToDomain(organization, user1, UserRole.ROLE_ORG_ADMIN);

        assertEquals(firstUser_UserDomain, userDomainService.get(firstUser_UserDomain.getUser(), firstUser_UserDomain.getDomain().getId(), firstUser_UserDomain.getRole().getUserRole()));
    }

    @Test
    public void getAll_ForDomainIdAndUserRoles_Test() {
        User user1 = createUser("user1@knappsack.com", true, false);
        User user2 = createUser("user2@knappsack.com", true, false);

        Organization organization = createOrganization();
        UserDomain firstUser_UserDomain = addUserToDomain(organization, user1, UserRole.ROLE_ORG_ADMIN);
        UserDomain secondUser_UserDomain = addUserToDomain(organization, user2, UserRole.ROLE_ORG_USER);

        List<UserDomain> userDomains = userDomainService.getAll(organization.getId(), UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_ORG_USER);
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 2);

        userDomains = userDomainService.getAll(organization.getId(), UserRole.ROLE_ORG_ADMIN);
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 1);
        assertEquals(firstUser_UserDomain, userDomains.get(0));
    }

    @Test
    public void getAll_ForDomainIdDomainTypeUserRoles_Test() {
        User user1 = createUser("user1@knappsack.com", true, false);
        User user2 = createUser("user2@knappsack.com", true, false);

        Organization organization = createOrganization();
        Group group = createGroup(organization);
        UserDomain firstUser_UserDomain = addUserToDomain(organization, user1, UserRole.ROLE_ORG_ADMIN);
        UserDomain secondUser_UserDomain = addUserToDomain(group, user2, UserRole.ROLE_GROUP_ADMIN);

        List<UserDomain> userDomains = userDomainService.getAll(organization.getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_ORG_USER);
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 1);
        assertEquals(userDomains.get(0), firstUser_UserDomain);

        userDomains = userDomainService.getAll(group.getId(), DomainType.GROUP, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 1);
        assertEquals(secondUser_UserDomain, userDomains.get(0));

        userDomains = userDomainService.getAll(group.getId(), DomainType.GROUP, UserRole.ROLE_GROUP_USER);
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 0);
    }

    @Test
    public void removeUserDomainFromDomain_Test() {
        User user1 = createUser("user1@knappsack.com", true, false);
        User user2 = createUser("user2@knappsack.com", true, false);

        Organization organization = createOrganization();
        UserDomain firstUser_UserDomain = addUserToDomain(organization, user1, UserRole.ROLE_ORG_ADMIN);
        UserDomain secondUser_UserDomain = addUserToDomain(organization, user2, UserRole.ROLE_ORG_USER);

        List<UserDomain> userDomains = userDomainService.getAll(organization.getId(), UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_ORG_USER);
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 2);

        userDomainService.removeUserDomainFromDomain(organization.getId(), user1.getId());

        userDomains = userDomainService.getAll(organization.getId());
        assertNotNull(userDomains);
        assertEquals(userDomains.size(), 1);
        assertEquals(secondUser_UserDomain, userDomains.get(0));
    }

    private Organization createOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization Add");
        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);
        orgStorageConfig.setPrefix("add_test");
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        storageConfigurations.add(storageConfiguration);
        orgStorageConfig.setStorageConfigurations(storageConfigurations);
        organization.setOrgStorageConfig(orgStorageConfig);
        organizationService.add(organization);
        organizationService.getAll();

        return organization;
    }

    private Group createGroup(Organization organization) {
        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.add(group);

        return group;
    }

    private StorageConfiguration getStorageConfiguration() {
        StorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        storageConfiguration.setBaseLocation("/test");
        storageConfiguration.setName("Local Storage Config");
        storageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(storageConfiguration);
        List<StorageConfiguration> storageConfigurations = storageConfigurationService.getAll();
        return storageConfigurations.get(0);
    }

    private UserDomain addUserToDomain(Domain domain, User user, UserRole userRole) {
        if (domain == null || user == null || userRole == null) {
            fail("Domain, User, and UserRole are all required when attempting to add User to Domain");
        }

        UserDomain userDomain = userService.addUserToDomain(user, domain, userRole);
        assertNotNull(userDomain);
        assertNotNull(userDomain.getId());

        return userDomain;
    }

}
