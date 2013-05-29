package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired(required = true)
    private ApplicationVersionUserStatisticService applicationVersionUserStatisticService;

    @Test
    public void addTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        applicationVersion.setAppState(AppState.DISABLED);
        applicationVersionService.update(applicationVersion);
        List<ApplicationVersion> applicationVersionList = applicationVersionService.getAll(applicationVersion.getApplication(), AppState.DISABLED);
        assertTrue(applicationVersionList.size() == 1);
        assertEquals(applicationVersion, applicationVersionList.get(0));
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
        ApplicationVersionForm applicationVersionForm = new ApplicationVersionForm();
        applicationVersionForm.setAppState(AppState.GROUP_PUBLISH);
//        applicationVersionForm.setGroupId(applicationVersion.getApplication().getCategory().getOrganization().getGroups().get(0).getId());
//        applicationVersionForm.setStorageConfigurationId(applicationVersion.getStorageConfiguration().getId());
        applicationVersionForm.setEditing(false);
        applicationVersionForm.setVersionName("1.1.1");
        applicationVersionForm.setParentId(applicationVersion.getApplication().getId());
        ApplicationVersion newApplicationVersion = applicationVersionService.saveApplicationVersion(applicationVersionForm);
        assertNotNull(newApplicationVersion);
        assertTrue(newApplicationVersion.getAppState().equals(AppState.GROUP_PUBLISH));
        assertTrue(newApplicationVersion.getVersionName().equals("1.1.1"));
    }

    @Test
    public void getAllTest() {
        ApplicationVersion applicationVersion = getApplicationVersion();
        applicationVersionService.add(applicationVersion);
        List<ApplicationVersion> applicationVersions = applicationVersionService.getAll(applicationVersion.getApplication().getCategory().getOrganization().getId());
        assertTrue(applicationVersions.size() == 1);
    }

    private ApplicationVersion getApplicationVersion() {
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

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        organizationService.getAll();


        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.add(group);

        Application application = new Application();
        application.setName("Test Application");
        application.setDescription("This is a description.");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setStorageConfiguration(localStorageConfiguration);
        application.setOwnedGroup(group);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);
        applicationVersion.setStorageConfiguration(application.getStorageConfiguration());

        application.getApplicationVersions().add(applicationVersion);
        applicationService.add(application);

        ApplicationVersionUserStatistic statistic = new ApplicationVersionUserStatistic();
        statistic.setApplicationVersion(applicationVersion);
        statistic.setRemoteAddress("127.0.0.1");
        statistic.setUser(getUserWithSecurityContext());
        statistic.setUserAgent("UserAgent");

        applicationVersionUserStatisticService.add(statistic);

        group.getOwnedApplications().add(application);
        groupService.save(group);
        application.setOwnedGroup(group);

        Group group2 = new Group();
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
