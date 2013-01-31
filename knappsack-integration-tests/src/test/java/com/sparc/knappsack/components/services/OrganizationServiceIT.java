package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.OrganizationModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrganizationServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private CategoryService categoryService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Test
    public void updateTest() {
        OrganizationModel organizationModel = getOrganizationModel();

        organizationService.createOrganization(organizationModel);
        Organization organization = organizationService.getByName("Test Organization");
        organization.setName("New Organization");
        organizationService.update(organization);
        organization = organizationService.getByName("New Organization");
        assertNotNull(organization);
    }

    @Test
    public void createOrganizationTest() {
        OrganizationModel organizationModel = getOrganizationModel();

        organizationService.createOrganization(organizationModel);
        Organization organization = organizationService.getByName("Test Organization");
        assertNotNull(organization);
    }

    @Test
    public void editOrganizationTest() {
        OrganizationModel organizationModel = getOrganizationModel();

        organizationService.createOrganization(organizationModel);
        Organization organization = organizationService.getByName("Test Organization");

        OrganizationModel newOrganizationModel = new OrganizationModel();
        newOrganizationModel.setName("Test Organization 2");
        newOrganizationModel.setId(organization.getId());
        organizationService.editOrganization(newOrganizationModel);
        Organization updatedOrganization = organizationService.getByName("Test Organization 2");
        assertNotNull(updatedOrganization);
        assertEquals(organization.getId(), updatedOrganization.getId());
    }

    @Test
    public void addOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization Add");
        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);
        orgStorageConfig.setPrefix("add_test");
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        storageConfigurations.add(getStorageConfiguration());
        orgStorageConfig.setStorageConfigurations(storageConfigurations);
        organization.setOrgStorageConfig(orgStorageConfig);
        organizationService.add(organization);
        organizationService.getAll();
        Organization retrievedOrganization = organizationService.getByName("Test Organization Add");
        assertNotNull(retrievedOrganization);
        assertNotNull(retrievedOrganization.getDomainConfiguration());
        assertTrue(retrievedOrganization.getDomainConfiguration().getApplicationLimit() == 2);
        assertTrue(retrievedOrganization.getDomainConfiguration().getApplicationVersionLimit() == 5);
        assertTrue(retrievedOrganization.getDomainConfiguration().getUserLimit() == 10);
        assertTrue(retrievedOrganization.getDomainConfiguration().getMegabyteStorageLimit() == 500);
        assertTrue(retrievedOrganization.getDomainConfiguration().getMegabyteBandwidthLimit() == 2048);
        assertFalse(retrievedOrganization.getDomainConfiguration().isDisabledDomain());
    }

    @Test
    public void deleteOrganization() {
        Organization organization = getOrganization();

        Category category = new Category();
        category.setOrganization(organization);
        category.setDescription("Test Category");
        category.setName("Test Category");
        category.setStorageConfiguration(organization.getOrgStorageConfig().getStorageConfigurations().get(0));

        categoryService.add(category);

        AppFile categoryIcon = new AppFile();
        categoryIcon.setName("Category Icon");
        categoryIcon.setRelativePath("relativePath");
        categoryIcon.setStorable(category);
        category.setIcon(categoryIcon);

        organization.getCategories().add(category);

        categoryService.update(category);

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);

        organization.getGroups().add(group);

        groupService.add(group);

        Application application = new Application();
        application.setApplicationType(ApplicationType.CHROME);
        application.setCategory(category);
        application.setStorageConfiguration(organization.getOrgStorageConfig().getStorageConfigurations().get(0));
        application.setOwnedGroup(group);
        List<Application> applications = new ArrayList<Application>();
        applications.add(application);

        group.setOwnedApplications(applications);

        applicationService.add(application);

        Organization retrievedOrganization = organizationService.getByName("Test Organization");
        assertNotNull(applicationService.get(application.getId()));
        assertNotNull(retrievedOrganization);
        assertNotNull(retrievedOrganization.getId());
        organizationService.delete(retrievedOrganization.getId());
        retrievedOrganization = organizationService.getByName("Test Organization");
        assertNull(retrievedOrganization);
        assertTrue(groupService.getAll().isEmpty());
        assertNull(applicationService.get(application.getId()));
    }

    @Test
    public void modelToEntityMappingTest() {
        OrganizationModel organizationModel = getOrganizationModel();

        organizationService.createOrganization(organizationModel);
        Organization organization = organizationService.getByName("Test Organization");

        OrganizationModel newOrganizationModel = new OrganizationModel();

        organizationService.mapOrgToOrgModel(organization, newOrganizationModel);
        assertEquals(organization.getName(), newOrganizationModel.getName());
    }

    @Test
    public void removeUserFromOrganizationTest() {
        OrganizationModel organizationModel = getOrganizationModel();

        organizationService.createOrganization(organizationModel);
        Organization organization = organizationService.getByName("Test Organization");
        assertNotNull(organization);

        User user = getUser();
        userService.add(user);
        user = userService.getByEmail(user.getEmail());
        assertNotNull(user);
        userService.addUserToOrganization(user, organization.getId(), UserRole.ROLE_ORG_USER);
        List<Organization> organizations = userService.getOrganizations(user);
        assertTrue(organizations.size() == 1);
        organizationService.removeUserFromOrganization(organization.getId(), user.getId());
        organizations = userService.getOrganizations(user);
        assertTrue(organizations.size() == 0);
    }

    private OrganizationModel getOrganizationModel() {
        OrganizationModel organizationModel = new OrganizationModel();
        organizationModel.setStoragePrefix("test_prefix");
        organizationModel.setName("Test Organization");
        organizationModel.setStorageConfigurationId(getStorageConfiguration().getId());

        return organizationModel;
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
