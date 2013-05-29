package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.ApplicationModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.assertTrue;

public class SearchServiceIT extends AbstractServiceTests {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired(required = true)
    private CategoryService categoryService;

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Test
    public void searchNoResultsTest() {
        List<ApplicationModel> applicationModels = searchService.searchApplications("", getUserWithSecurityContext(), ApplicationType.ANDROID);
        assertTrue(applicationModels.size() == 0);
    }

    @Test
    public void searchTest() {
        User user = getUser(getUserWithSecurityContext(), UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
        List<ApplicationModel> applicationModels = searchService.searchApplications("Test", user, ApplicationType.ANDROID);
        assertTrue(applicationModels.size() == 2);
    }

    private User getUser(User user, UserRole organizationUserRole, UserRole groupUserRole) {
        Role groupRole = roleService.getRoleByAuthority(groupUserRole.name());
        Role orgRole = roleService.getRoleByAuthority(organizationUserRole.name());

        user.getRoles().add(groupRole);
        user.getRoles().add(orgRole);

        Organization organization = getOrganization();

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);
        categoryService.add(category);

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.save(group);

        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setOwnedGroup(group);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);

        applicationService.add(application);

        Application application2 = new Application();
        application2.setName("Test Application2");
        application2.setApplicationType(ApplicationType.ANDROID);
        application2.setCategory(category);
        application2.setOwnedGroup(group);

        ApplicationVersion applicationVersion2 = new ApplicationVersion();
        applicationVersion2.setVersionName("1.0.0");
        applicationVersion2.setApplication(application2);
        applicationVersion2.setAppState(AppState.ORGANIZATION_PUBLISH);

        application.getApplicationVersions().add(applicationVersion2);

        applicationService.add(application2);

        group.getOwnedApplications().add(application);
        group.getOwnedApplications().add(application2);

        organization.getGroups().add(group);

        userService.save(user);

        organizationService.getAll();

        UserDomain userDomainGroup = new UserDomain();
        userDomainGroup.setUser(user);
        userDomainGroup.setDomain(group);
        userDomainGroup.setRole(groupRole);

        UserDomain userDomainOrg = new UserDomain();
        userDomainOrg.setUser(user);
        userDomainOrg.setDomain(organization);
        userDomainOrg.setRole(orgRole);

        user.getUserDomains().add(userDomainGroup);
        user.getUserDomains().add(userDomainOrg);

        userService.save(user);

        return user;
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
