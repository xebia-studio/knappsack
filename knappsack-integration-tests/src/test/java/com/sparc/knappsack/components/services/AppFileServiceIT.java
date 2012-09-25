package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.models.ImageModel;
import com.sparc.knappsack.util.WebRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppFileServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private AppFileService appFileService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Test
    public void addTest() {
        AppFile appFile = getAppFile();
        appFileService.add(appFile);
        List<AppFile> appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 1);
    }

    @Test
    public void updateTest() {
        AppFile appFile = getAppFile();
        appFileService.add(appFile);
        List<AppFile> appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 1);
        appFile.setName("Updated AppFile");
        appFileService.update(appFile);
        appFile = appFileService.get(appFile.getId());
        assertTrue(appFile.getName().equals("Updated AppFile"));
    }

    /*@Test
    public void deleteTest() {
        AppFile appFile = getAppFile();
        appFileService.add(appFile);
        List<AppFile> appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 1);
        appFileService.delete(appFile.getId());
        appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 0);
    }*/

    @Test
    public void createImageModelTest() {
        WebRequest.getInstance("http", "serverName", 80, "/knappsack");
        AppFile appFile = getAppFile();
        appFileService.add(appFile);

        List<AppFile> appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 1);

        ImageModel imageModel = appFileService.createImageModel(appFile);
        assertNotNull(imageModel);
        assertTrue(imageModel.getUrl().equals("http://serverName/knappsack/image/" + appFile.getId()));
    }

    private AppFile getAppFile() {
        StorageConfiguration storageConfiguration = getStorageConfiguration();
        storageConfiguration.setBaseLocation("/baseLocation");
        storageConfiguration.setName("Local Storage");
        storageConfiguration.setStorageType(StorageType.LOCAL);

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

        Category category = new Category();
        category.setOrganization(organization);
        category.setDescription("Test Category");
        category.setName("Test Category");
        category.setStorageConfiguration(storageConfiguration);
        organization.getCategories().add(category);

        organizationService.add(organization);

        AppFile categoryIcon = new AppFile();
        categoryIcon.setName("Category Icon");
        categoryIcon.setRelativePath("relativePath");
        categoryIcon.setStorable(category);
        categoryIcon.setStorageType(StorageType.LOCAL);
        category.setIcon(categoryIcon);

        return categoryIcon;
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
