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
    private GroupService groupService;

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
        User user = getUser();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        List<UserDomain> userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 1);
        assertTrue(userDomains.get(0).getDomain().getId().equals(organization.getId()));
    }

    @Test
    public void deleteTest() {
        User user = getUser();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
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
        User user = getUser();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);
        List<UserDomain> userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 1);

        userDomain.setDomain(createGroup(organization));
        userDomainService.update(userDomain);

        userDomains = userDomainService.getAll(user, DomainType.ORGANIZATION);
        assertTrue(userDomains.size() == 0);

        userDomains = userDomainService.getAll(user, DomainType.GROUP);
        assertTrue(userDomains.size() == 1);
    }

    @Test
    public void getByUserDomainIdDomainTypeUserRoleTest() {
        User user = getUser();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);

        userDomain = userDomainService.get(user, organization.getId(), UserRole.ROLE_ORG_ADMIN);
        assertNotNull(userDomain);
    }

    @Test
    public void getByUserDomainIdDomainTypeTest() {
        User user = getUser();

        Organization organization = createOrganization();

        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        Role roleOrgAdmin = roleService.getRoleByAuthority("ROLE_ORG_ADMIN");
        user.getRoles().add(roleOrgAdmin);

        userDomain.setRole(roleOrgAdmin);
        userDomain.setUser(user);

        userDomainService.add(userDomain);

        userDomain = userDomainService.get(user, organization.getId());
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

}
