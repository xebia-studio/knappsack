package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.UploadApplication;
import com.sparc.knappsack.models.ApplicationModel;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class ApplicationServiceIT extends AbstractServiceTests {

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
        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setDescription("This is a description.");
        applicationService.add(application);
        List<Application> applications = applicationService.getAll();
        assertTrue(applications.size() == 1);
    }

    @Test
    public void updateTest() {
        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setDescription("This is a description.");
        applicationService.add(application);
        List<Application> applications = applicationService.getAll();
        assertTrue(applications.size() == 1);

        application.setName("New Application");
        applicationService.update(application);
        application = applicationService.get(application.getId());
        assertTrue(application.getName().equals("New Application"));
    }

    @Test
    public void deleteTest() {
        Application application = getApplication();
        List<Application> applications = applicationService.getAll();
        assertTrue(applications.size() == 1);

        applicationService.delete(application.getId());
        applications = applicationService.getAll();
        assertTrue(applications.size() == 0);
    }

    @Test
    public void getAllByApplicationType() {
        Application application = getApplication();
        List<Application> applications = applicationService.getAll(ApplicationType.ANDROID);
        assertTrue(applications.size() == 1);
    }

    @Test
    public void getAllByCategory() {
        Application application = getApplication();
        List<Application> applications = applicationService.getAll(application.getCategory());
        assertTrue(applications.size() == 1);
    }

    @Test
    public void getAllByCategoryApplicationType() {
        Application application = getApplication();
        List<ApplicationModel> applications = applicationService.getAll(application.getCategory(), ApplicationType.ANDROID);
        assertTrue(applications.size() == 1);
    }

    @Test
    public void determineApplicationVisibilityTest() {
        Application application = getApplication();
        boolean isVisible = applicationService.determineApplicationVisibility(application, ApplicationType.ANDROID);
        assertTrue(isVisible);
        isVisible = applicationService.determineApplicationVisibility(application, ApplicationType.IPHONE);
        assertFalse(isVisible);
    }

    @Test
    public void createApplicationModelTest() {
        Application application = getApplication();
        ApplicationModel applicationModel = applicationService.createApplicationModel(application.getId());
        assertNotNull(applicationModel);
        assertTrue(applicationModel.getName().equals("Test Application"));
        assertTrue(applicationModel.getApplicationType().equals(application.getApplicationType()));
        assertTrue(applicationModel.getDescription().equals(application.getDescription()));
    }

    @Test
    public void saveApplicationTest() {
        Application application = getApplication();
        UploadApplication uploadApplication = new UploadApplication();
        uploadApplication.setApplicationType(application.getApplicationType());
        uploadApplication.setCategoryId(application.getCategory().getId());
        uploadApplication.setStorageConfigurationId(application.getStorageConfiguration().getId());
        uploadApplication.setName("New Application");
        uploadApplication.setDescription(application.getDescription());
        uploadApplication.setGroupId(application.getCategory().getOrganization().getGroups().get(0).getId());
        Application newApplication = applicationService.saveApplication(uploadApplication);
        Assert.assertNotNull(newApplication);
    }

    private Application getApplication() {
        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(localStorageConfiguration);

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

        return application;
    }
}
