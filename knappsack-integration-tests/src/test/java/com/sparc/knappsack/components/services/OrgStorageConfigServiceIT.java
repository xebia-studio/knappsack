package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class OrgStorageConfigServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private OrgStorageConfigService orgStorageConfigService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Test
    public void addTest() {
        OrgStorageConfig orgStorageConfig = getOrgStorageConfig();
        orgStorageConfigService.add(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("prefix");
        assertNotNull(orgStorageConfig);
    }

    @Test
    public void updateTest() {
        OrgStorageConfig orgStorageConfig = getOrgStorageConfig();
        orgStorageConfigService.add(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("prefix");
        assertNotNull(orgStorageConfig);
        orgStorageConfig.setPrefix("new_prefix");
        orgStorageConfigService.update(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("new_prefix");
        assertNotNull(orgStorageConfig);
    }

    @Test
    public void deleteTest() {
        OrgStorageConfig orgStorageConfig = getOrgStorageConfig();
        orgStorageConfigService.add(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("prefix");
        assertNotNull(orgStorageConfig);
        orgStorageConfigService.delete(orgStorageConfig.getId());
        orgStorageConfig = orgStorageConfigService.getByPrefix("prefix");
        assertNull(orgStorageConfig);
    }

    private OrgStorageConfig getOrgStorageConfig() {
        Organization organization = new Organization();
        organization.setName("Test Organization");
        organizationService.add(organization);
        organization = organizationService.getByName("Test Organization");
        assertNotNull(organization);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);
        orgStorageConfig.setPrefix("prefix");

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Config");
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        storageConfigurations.add(localStorageConfiguration);
        orgStorageConfig.setStorageConfigurations(storageConfigurations);

        return orgStorageConfig;
    }

}
