package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class ApplicationVersionServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Test
    public void addTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        ApplicationVersion newApplicationVersion = new ApplicationVersion();
        newApplicationVersion.setAppState(AppState.DISABLED);
        newApplicationVersion.setVersionName("1.0.1");
        newApplicationVersion.setApplication(applicationVersion.getApplication());
        newApplicationVersion.setStorageConfiguration(newApplicationVersion.getStorageConfiguration());
        applicationVersion.getApplication().getApplicationVersions().add(newApplicationVersion);
        applicationVersionService.add(newApplicationVersion);
        List<ApplicationVersion> applicationVersionList = applicationVersionService.getAll(applicationVersion.getApplication(), AppState.DISABLED);
        assertTrue(applicationVersionList.size() == 1);
    }

    @Test
    public void updateTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        applicationVersion.setVersionName("2.0.0");
        applicationVersionService.update(applicationVersion);
        applicationVersion = applicationVersionService.get(applicationVersion.getId());
        assertTrue(applicationVersion.getVersionName().equals("2.0.0"));
    }

    @Test
    public void deleteTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        applicationVersionService.delete(applicationVersion.getId());
        applicationVersion = applicationVersionService.get(applicationVersion.getId());
        assertNull(applicationVersion);
    }

    @Test
    public void updateAppStateTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        applicationVersionService.updateAppState(applicationVersion.getId(), AppState.ORGANIZATION_PUBLISH);
        applicationVersion = applicationVersionService.get(applicationVersion.getId());
        assertTrue(applicationVersion.getAppState().equals(AppState.ORGANIZATION_PUBLISH));
    }

    @Test
    public void saveApplicationVersionTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        UploadApplicationVersion uploadApplicationVersion = new UploadApplicationVersion();
        uploadApplicationVersion.setAppState(AppState.GROUP_PUBLISH);
        uploadApplicationVersion.setGroupId(applicationVersion.getApplication().getCategory().getOrganization().getGroups().get(0).getId());
        uploadApplicationVersion.setStorageConfigurationId(applicationVersion.getStorageConfiguration().getId());
        uploadApplicationVersion.setEditing(false);
        uploadApplicationVersion.setVersionName("1.1.1");
        uploadApplicationVersion.setParentId(applicationVersion.getApplication().getId());
        ApplicationVersion newApplicationVersion = applicationVersionService.saveApplicationVersion(uploadApplicationVersion);
        assertNotNull(newApplicationVersion);
        assertTrue(newApplicationVersion.getAppState().equals(AppState.GROUP_PUBLISH));
        assertTrue(newApplicationVersion.getVersionName().equals("1.1.1"));
    }

    @Test
    public void getAllTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        List<ApplicationVersion> applicationVersions = applicationVersionService.getAll(applicationVersion.getApplication().getCategory().getOrganization().getId());
        assertTrue(applicationVersions.size() == 1);
    }

    private ApplicationVersion getApplicationVersion() {
        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(localStorageConfiguration);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);
        orgStorageConfig.setPrefix("test_org");
        orgStorageConfig.getStorageConfigurations().add(localStorageConfiguration);
        organization.setOrgStorageConfig(orgStorageConfig);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        organizationService.getAll();

        Application application = new Application();
        application.setName("Test Application");
        application.setDescription("This is a description.");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setStorageConfiguration(localStorageConfiguration);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);
        applicationVersion.setStorageConfiguration(localStorageConfiguration);

        application.getApplicationVersions().add(applicationVersion);
        applicationService.add(application);

        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("Test Group");
        group.setOrganization(organization);
        group.setOwnedApplications(new ArrayList<Application>());
        group.getOwnedApplications().add(application);
        groupService.save(group);

        Group group2 = new Group();
        group2.setAccessCode(UUID.randomUUID().toString());
        group2.setName("Test Group 2");
        group2.setOrganization(organization);
        group2.setGuestApplicationVersions(new ArrayList<ApplicationVersion>());
        group2.getGuestApplicationVersions().add(applicationVersion);
        groupService.save(group2);

        organization.getGroups().add(group);
        organization.getGroups().add(group2);

        organizationService.getAll();

        return applicationVersion;
    }
}
