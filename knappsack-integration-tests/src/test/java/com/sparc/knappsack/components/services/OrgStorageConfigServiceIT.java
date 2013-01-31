package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.enums.StorageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static junit.framework.Assert.assertNotNull;

public class OrgStorageConfigServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private OrgStorageConfigService orgStorageConfigService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Test
    public void addTest() {
        OrgStorageConfig orgStorageConfig = getOrganization().getOrgStorageConfig();
        orgStorageConfigService.add(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("prefix");
        assertNotNull(orgStorageConfig);
    }

    @Test
    public void updateTest() {
        OrgStorageConfig orgStorageConfig = getOrganization().getOrgStorageConfig();
        orgStorageConfigService.add(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("prefix");
        assertNotNull(orgStorageConfig);
        orgStorageConfig.setPrefix("new_prefix");
        orgStorageConfigService.update(orgStorageConfig);
        orgStorageConfig = orgStorageConfigService.getByPrefix("new_prefix");
        assertNotNull(orgStorageConfig);
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
        orgStorageConfig.setPrefix("prefix");
        orgStorageConfig.setOrganization(organization);
        organization.setOrgStorageConfig(orgStorageConfig);

        organizationService.add(organization);

        return organization;
    }

}
