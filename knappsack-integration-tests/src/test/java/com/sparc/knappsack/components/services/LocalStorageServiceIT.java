package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.StorageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalStorageServiceIT extends AbstractServiceTests {

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private AppFileService appFileService;

    @Test
    public void saveAndDeleteAppFileTest() {
        Organization organization = getOrganization();
        String content = "Test Content";
        String uuid = UUID.randomUUID().toString();
        MultipartFile multipartFile = new MockMultipartFile("TestFile", "TestFile.txt", "text/plain", content.getBytes());

        StorageService storageService = storageServiceFactory.getStorageService(StorageType.LOCAL);
        AppFile appFile = storageService.save(multipartFile, "test", organization.getOrgStorageConfig().getId(), organization.getOrgStorageConfig().getStorageConfigurations().get(0).getId(), uuid);
        appFile.setStorable(organization.getCategories().get(0));
        appFileService.add(appFile);
        List<AppFile> appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 1);
        appFileService.delete(appFile);
        appFile = appFileService.get(appFile.getId());
        assertNull(appFile);
    }

    @Test
    public void saveAndDeleteIconTest() {
        Organization organization = getOrganization();
        String content = "Test Content";
        String uuid = UUID.randomUUID().toString();
        MultipartFile multipartFile = new MockMultipartFile("TestFile", "TestFile.txt", "text/plain", content.getBytes());

        StorageService storageService = storageServiceFactory.getStorageService(StorageType.LOCAL);
        AppFile appFile = storageService.save(multipartFile, AppFileType.ICON.getPathName(), organization.getOrgStorageConfig().getId(), organization.getOrgStorageConfig().getStorageConfigurations().get(0).getId(), uuid);
        appFile.setStorable(organization.getCategories().get(0));
        appFileService.add(appFile);
        List<AppFile> appFiles = appFileService.getAll();
        assertTrue(appFiles.size() == 1);
        appFileService.delete(appFile);
        appFile = appFileService.get(appFile.getId());
        assertNull(appFile);
    }

    private Organization getOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization Add");
        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);
        orgStorageConfig.setPrefix("add_test");
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        storageConfigurations.add(getStorageConfiguration());
        orgStorageConfig.setStorageConfigurations(storageConfigurations);
        organization.setOrgStorageConfig(orgStorageConfig);
        Category category = new Category();
        category.setStorageConfiguration(storageConfigurations.get(0));
        category.setDescription("Test Category");
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);
        organizationService.add(organization);
        organizationService.getAll();
        Organization retrievedOrganization = organizationService.getByName("Test Organization Add");
        assertNotNull(retrievedOrganization);
        return retrievedOrganization;
    }

    private StorageConfiguration getStorageConfiguration() {
        StorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        File file = new File(ClassLoader.getSystemResource(".").getPath());
        String baseLocation = file.getAbsolutePath();

        storageConfiguration.setBaseLocation(baseLocation);
        storageConfiguration.setName("Local Storage Config");
        storageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(storageConfiguration);
        List<StorageConfiguration> storageConfigurations = storageConfigurationService.getAll();
        return storageConfigurations.get(0);
    }

}
