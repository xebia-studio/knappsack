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
import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class UserDomainServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Autowired
    private RoleService roleService;

    private StorageConfiguration storageConfiguration;

    @Before
    public void setup() {
        super.setup();
        storageConfiguration = getStorageConfiguration();
    }

    @Test
    public void addTest() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("test@test.com");

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomainId(organization.getId());
        userDomain.setDomainType(DomainType.ORGANIZATION);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        List<UserDomain> userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 1);
        assertTrue(userDomains.get(0).getDomainId().equals(organization.getId()));
    }

    @Test
    public void deleteTest() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("test@test.com");

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomainId(organization.getId());
        userDomain.setDomainType(DomainType.ORGANIZATION);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        List<UserDomain> userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 1);

        userDomainService.delete(userDomains.get(0).getId());

        userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 0);
    }

    @Test
    public void updateTest() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("test@test.com");

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomainId(organization.getId());
        userDomain.setDomainType(DomainType.ORGANIZATION);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        List<UserDomain> userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 1);

        userDomain.setDomainType(DomainType.GROUP);
        userDomainService.update(userDomain);

        userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 0);

        userDomains = userDomainService.getAll(user, DomainType.GROUP);
        assertTrue(userDomains.size() == 1);
    }

    @Test
    public void getByUserDomainIdDomainTypeUserRoleTest() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("test@test.com");

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomainId(organization.getId());
        userDomain.setDomainType(DomainType.ORGANIZATION);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);

        userDomain = userDomainService.get(user, organization.getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(userDomain);
    }

    @Test
    public void getByUserDomainIdDomainTypeTest() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("test@test.com");

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomainId(organization.getId());
        userDomain.setDomainType(DomainType.ORGANIZATION);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);

        userDomain = userDomainService.get(user, organization.getId(), DomainType.ORGANIZATION);
        assertNotNull(userDomain);
    }

//    TODO: List<UserDomain> getAll(User user, DomainType domainType);
//    @Test
//    public void getAllByUserDomainTypeTest() {
//
//    }


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
        organization.setAccessCode(UUID.randomUUID().toString());
        organizationService.add(organization);
        organizationService.getAll();

        return organization;
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

}
