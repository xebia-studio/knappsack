package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.OrganizationModel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class OrganizationServiceIT extends AbstractServiceTests {

    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceIT.class);

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired
    private StorageConfigurationService storageConfigurationService;

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
        organization.setAccessCode(UUID.randomUUID().toString());
        organizationService.add(organization);
        organizationService.getAll();
        Organization retrievedOrganization = organizationService.getByName("Test Organization Add");
        assertNotNull(retrievedOrganization);
        assertNotNull(retrievedOrganization.getDomainConfiguration());
        assertTrue(retrievedOrganization.getDomainConfiguration().getApplicationLimit() == 2);
        assertTrue(retrievedOrganization.getDomainConfiguration().getApplicationVersionLimit() == 5);
        assertTrue(retrievedOrganization.getDomainConfiguration().getUserLimit() == 10);
        assertTrue(retrievedOrganization.getDomainConfiguration().getMegabyteStorageLimit() == 500);
        assertFalse(retrievedOrganization.getDomainConfiguration().isDisabledDomain());
    }

    @Test
    public void deleteOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization Add");
        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);
        orgStorageConfig.setPrefix("add_test");
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        storageConfigurations.add(getStorageConfiguration());
        orgStorageConfig.setStorageConfigurations(storageConfigurations);
        organization.setOrgStorageConfig(orgStorageConfig);
        organization.setAccessCode(UUID.randomUUID().toString());
        organizationService.add(organization);

        Category category = new Category();
        category.setOrganization(organization);
        category.setDescription("Test Category");
        category.setName("Test Category");
        category.setStorageConfiguration(organization.getOrgStorageConfig().getStorageConfigurations().get(0));
        AppFile categoryIcon = new AppFile();
        categoryIcon.setName("Category Icon");
        categoryIcon.setRelativePath("relativePath");
        categoryIcon.setStorable(category);
        category.setIcon(categoryIcon);


        organization.getCategories().add(category);

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        group.setAccessCode(UUID.randomUUID().toString());

        Application application = new Application();
        application.setApplicationType(ApplicationType.CHROME);
        application.setCategory(category);
        application.setStorageConfiguration(organization.getOrgStorageConfig().getStorageConfigurations().get(0));
        List<Application> applications = new ArrayList<Application>();
        applications.add(application);

        group.setOwnedApplications(applications);

        List<Group> groups = new ArrayList<Group>();
        groups.add(group);
        organization.setGroups(groups);

        organizationService.getAll();
        Organization retrievedOrganization = organizationService.getByName("Test Organization Add");
        assertNotNull(applicationService.get(application.getId()));
        assertNotNull(retrievedOrganization);
        assertNotNull(retrievedOrganization.getId());
        organizationService.delete(retrievedOrganization.getId());
        retrievedOrganization = organizationService.getByName("Test Organization Add");
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

}
