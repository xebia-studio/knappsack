package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.StorageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.*;

public class DomainServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private DomainService domainService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Test
    public void getTest() {
        Organization organization = getOrganization();

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        organization.getGroups().add(group);

        groupService.save(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);

        Domain orgDomain = domainService.get(organization.getId());
        assertNotNull(orgDomain);
        assertTrue(orgDomain.getName().equals("Test Organization"));

        Domain groupDomain = domainService.get(group.getId());
        assertNotNull(groupDomain);
        assertTrue(groupDomain.getName().equals("Test Group"));

        Domain domain = domainService.get(4l);
        assertNull(domain);
    }

    private Organization getOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.getStorageConfigurations().add(localStorageConfiguration);
        orgStorageConfig.setPrefix("testPrefix");
        orgStorageConfig.setOrganization(organization);
        organization.setOrgStorageConfig(orgStorageConfig);

        organizationService.add(organization);

        return organization;
    }
}
