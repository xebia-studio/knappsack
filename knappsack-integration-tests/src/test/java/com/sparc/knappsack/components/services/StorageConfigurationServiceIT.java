package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;
import com.sparc.knappsack.models.OrganizationModel;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertNull;

public class StorageConfigurationServiceIT extends AbstractServiceTests {

    private static final String STORAGE_CONFIG_NAME = "Local Storage Config";

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Test
    public void addStorageConfig() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfigurationService.add(storageConfiguration);
        storageConfiguration = storageConfigurationService.getByName(storageConfiguration.getName());
        assertNotNull(storageConfiguration);
    }

    @Test
    public void getAll() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfigurationService.add(storageConfiguration);
        storageConfiguration = getStorageConfiguration();
        storageConfiguration.setName("Test Storage Config 2");
        storageConfigurationService.add(storageConfiguration);
        List<StorageConfiguration> storageConfigurations = storageConfigurationService.getAll();
        assertEquals(storageConfigurations.size(), 2);
    }

    @Test()
    public void deleteStorageConfig() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfigurationService.add(storageConfiguration);
        storageConfigurationService.delete(storageConfiguration.getId());
        storageConfiguration = storageConfigurationService.get(storageConfiguration.getId());
        assertNull(storageConfiguration);
    }

    @Test
    public void deleteStorageConfigWithOrganization() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfigurationService.add(storageConfiguration);
        storageConfiguration = storageConfigurationService.getByName(storageConfiguration.getName());

        OrganizationModel organizationModel = new OrganizationModel();
        organizationModel.setStorageConfigurationId(storageConfiguration.getId());
        organizationModel.setStoragePrefix("test");
        organizationModel.setName("Test Organization");

        organizationService.createOrganization(organizationModel);

        storageConfigurationService.delete(storageConfiguration.getId());
        storageConfiguration = storageConfigurationService.getByName(storageConfiguration.getName());
        assertNotNull(storageConfiguration);
    }

    @Test
    public void update() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfigurationService.add(storageConfiguration);
        storageConfiguration.setBaseLocation("/update");
        storageConfigurationService.update(storageConfiguration);
        storageConfiguration = storageConfigurationService.getByName(storageConfiguration.getName());
        assertEquals(storageConfiguration.getBaseLocation(), "/update");
    }

    @Test
    public void getByName() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfigurationService.add(storageConfiguration);
        storageConfiguration = storageConfigurationService.getByName(STORAGE_CONFIG_NAME);
        assertNotNull(storageConfiguration);
    }

    @Test
    public void createStorageConfigurationTest() {
        StorageForm form = new StorageForm();
        form.setBaseLocation("/base/location");
        form.setName("Local Storage");
        form.setEditing(false);
        form.setStorageType(StorageType.LOCAL);

        StorageConfiguration storageConfiguration = storageConfigurationService.createStorageConfiguration(form);
        storageConfigurationService.add(storageConfiguration);
        List<StorageConfiguration> storageConfigurations = storageConfigurationService.getAll();
        assertTrue(storageConfigurations.size() == 1);
        storageConfiguration = storageConfigurations.get(0);
        Assert.assertNotNull(storageConfiguration);
        assertTrue(storageConfiguration.getName().equals("Local Storage"));
        assertTrue(storageConfiguration.getBaseLocation().equals("/base/location"));
        assertTrue(storageConfiguration.getStorageType().equals(StorageType.LOCAL));
    }

    private StorageConfiguration getStorageConfiguration() {
        StorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        storageConfiguration.setBaseLocation("/test");
        storageConfiguration.setName(STORAGE_CONFIG_NAME);
        storageConfiguration.setStorageType(StorageType.LOCAL);

        return storageConfiguration;
    }
}
