package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static junit.framework.Assert.*;

public class UserServiceIT extends AbstractServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDomainService userDomainService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired(required = true)
    private DomainUserRequestService requestService;

    @Autowired(required = true)
    private CategoryService categoryService;

    @Autowired(required = true)
    private DomainUserRequestService domainUserRequestService;

    @Test
    public void addTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        userService.add(user);
        user = userService.getByEmail(user.getEmail());
        assertNotNull(user);
    }

    @Test
    public void getByEmailTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        user = userService.getByEmail(user.getEmail());
        assertNotNull(user);
    }

    @Test
    public void updateTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
        user.setLastName("Tester");

        user = userService.getByEmail(user.getEmail());
        userService.update(user);
        assertEquals(user.getLastName(), "Tester");
    }

    @Test
    public void deleteTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        List<Group> groups = userService.getGroups(user);
        assertTrue(groups.size() == 1);
        List<UserDomain> userGroupDomains = userDomainService.getAll(groups.get(0).getId());
        assertTrue(userGroupDomains.size() == 1);
        userService.delete(user.getId());
        userGroupDomains = userDomainService.getAll(groups.get(0).getId());
        assertTrue(userGroupDomains.size() == 0);
    }

    @Test
    public void getGroupsForOrgAdminTest() {
        User user = getUser(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());

        List<Group> groups = userService.getGroups(user);
        assertTrue(groups.size() == 1);
    }

    @Test
    public void getOrganizationsTest() {
        User user = getUser(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());

        List<Organization> organizations = userService.getOrganizations(user);
        assertTrue(organizations.size() == 1);
    }

    @Test
    public void getApplicationVersionsByUserTest() {
        User user = getUser(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());

        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(user);
        assertTrue(applicationVersions.size() == 2);
    }

    @Test
    public void getApplicationVersionByUserApplicationAppState() {
        User user = getUser(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());

        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(user);
        for (ApplicationVersion applicationVersion : applicationVersions) {
            applicationVersions = userService.getApplicationVersions(user, applicationVersion.getApplication().getId(), SortOrder.ASCENDING, AppState.ORGANIZATION_PUBLISH, AppState.GROUP_PUBLISH);
            assertTrue(applicationVersions.size() == 1);
        }
    }

    @Test
    public void activationTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());
        assertFalse(user.isActivated());
        boolean activated = userService.activate(user.getId(), user.getActivationCode());
        assertTrue(activated);
        assertTrue(user.isActivated());
    }

    @Test
    public void getApplicationsByUserApplicationTypeAppStates() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());
        List<Application> applications = userService.getApplicationsForUser(user, ApplicationType.ANDROID, AppState.GROUP_PUBLISH);
        assertTrue(applications.size() == 1);
    }

    @Test
    public void getApplicationsByUserApplicationTypeCategoryAppStatesTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
        user = userService.getByEmail(user.getEmail());
        Organization organization = organizationService.getAll().get(0);
        Category category = organization.getCategories().get(0);
        List<Application> applications = userService.getApplicationsForUser(user, ApplicationType.ANDROID, category.getId(), AppState.GROUP_PUBLISH);
        assertTrue(applications.size() == 1);
    }

    @Test
    public void addUserToGroupTest() {
        User user = getUser();

        Group group = createGroup(createOrganization());

        userService.addUserToGroup(user, group.getId(), UserRole.ROLE_GROUP_USER);
        List<Group> groups = userService.getGroups(user);
        assertTrue(groups.size() == 1);
        assertTrue(userService.isUserInGroup(user, group));
        assertTrue(userService.isUserInGroup(user, group, UserRole.ROLE_GROUP_USER));
    }

    @Test
    public void addUserToOrganizationTest() {
        User user = getUser();

        Organization newOrganization = createOrganization();

        userService.addUserToOrganization(user, newOrganization.getId(), UserRole.ROLE_ORG_USER);
        List<Organization> organizations = userService.getOrganizations(user);
        assertTrue(organizations.size() == 1);
        assertTrue(userService.isUserInOrganization(user, newOrganization, UserRole.ROLE_ORG_USER));
    }

    @Test
    public void changePasswordTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
//        user.setPasswordExpired(true);

        boolean isChanged = userService.changePassword(user, "password", true);
        assertTrue(isChanged);
    }

//    @Test
//    public void forgotPasswordTest() {
//        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
//
//        String newPassword = userService.forgotPassword(user);
//        assertNotNull(newPassword);
//        assertTrue(!"".equals(newPassword));
//    }

    @Test
    public void addUserToGroup() {
        User user = getUser();

        Group group = createGroup(createOrganization());

        DomainUserRequest domainUserRequest = domainUserRequestService.createDomainUserRequest(user, group.getUuid());
        requestService.getAll(group.getId());

        boolean isAdded = userService.addUserToGroup(domainUserRequest.getUser(), domainUserRequest.getDomain().getId(), UserRole.ROLE_GROUP_USER);
        assertTrue(isAdded);
    }

    @Test
    public void addUserAdminToGroup() {
        User user = getUser();

        Group group = createGroup(createOrganization());

        DomainUserRequest domainUserRequest = domainUserRequestService.createDomainUserRequest(user, group.getUuid());
        requestService.getAll(group.getId());

        boolean isAdded = userService.addUserToGroup(domainUserRequest.getUser(), domainUserRequest.getDomain().getId(), UserRole.ROLE_GROUP_ADMIN);
        assertTrue(isAdded);
    }

    @Test
    public void isUserInDomainTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        List<Group> groups = userService.getGroups(user);
        for (Group group : groups) {
            boolean isInDomain = userService.isUserInDomain(user, group.getId(), UserRole.ROLE_GROUP_USER);
            assertTrue(isInDomain);
        }

        List<Organization> organizations = userService.getOrganizations(user);
        for (Organization organization : organizations) {
            boolean isInDomain = userService.isUserInDomain(user, organization.getId(), UserRole.ROLE_ORG_USER);
            assertTrue(isInDomain);
        }
    }

    @Test
    public void getCategoriesForUsersTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        List<Category> categories = userService.getCategoriesForUser(user, ApplicationType.ANDROID);
        assertTrue(categories.size() == 1);
    }

    @Test
    public void updateSecurityContextTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        boolean isUpdated = userService.updateSecurityContext(user);
        assertTrue(isUpdated);
    }

    @Test
    public void canUserEditApplicationTest() {
        //Initial setup
        User user = getUser();
        userService.add(user);
        Organization organization = createOrganization();
        Category category = createCategory(organization);
        organization.getCategories().add(category);
        Group group = createGroup(organization);
        Application application = createApplication(group, category, "Test Application", AppState.GROUP_PUBLISH);
        group.getOwnedApplications().add(application);
        application.setOwnedGroup(group);

        entityManager.flush();

        createUserDomain(user, group, UserRole.ROLE_GROUP_ADMIN);
        entityManager.flush();

        assertTrue(userService.canUserEditApplication(user.getId(), application.getId()));

        //Reset
        userService.delete(user.getId());
        ReflectionTestUtils.setField(this, "user", null);
        entityManager.flush();

        //Test if org admin can edit application
        user = getUser();
        userService.add(user);
        entityManager.flush();
        createUserDomain(user, organization, UserRole.ROLE_ORG_ADMIN);
        entityManager.flush();
        assertTrue(userService.canUserEditApplication(user.getId(), application.getId()));

        //Reset
        userService.delete(user.getId());
        ReflectionTestUtils.setField(this, "user", null);
        entityManager.flush();

        //Test user is org user
        user = getUser();
        userService.add(user);
        entityManager.flush();
        createUserDomain(user, organization, UserRole.ROLE_ORG_USER);
        entityManager.flush();
        assertFalse(userService.canUserEditApplication(user.getId(), application.getId()));

        //Reset
        userService.delete(user.getId());
        ReflectionTestUtils.setField(this, "user", null);
        entityManager.flush();

        //Test user is not part of organization and not group admin
        user = getUser();
        userService.add(user);
        entityManager.flush();
        assertFalse(userService.canUserEditApplication(user.getId(), application.getId()));
    }

    private User getUser(UserRole organizationUserRole, UserRole groupUserRole) {
        User user = getUser();

        Role orgRole = null;
        if (organizationUserRole != null) {
            orgRole = roleService.getRoleByAuthority(organizationUserRole.name());
            user.getRoles().add(orgRole);
        }
        Role groupRole = null;
        if (groupUserRole != null) {
            groupRole = roleService.getRoleByAuthority(groupUserRole.name());
            user.getRoles().add(groupRole);
        }

        Organization organization = createOrganization();

        Category category = createCategory(organization);

        Group group = createGroup(organization);

        Application application = createApplication(group, organization.getCategories().get(0), "Test Application", AppState.GROUP_PUBLISH);
        Application application2 = createApplication(group, organization.getCategories().get(0), "Test Application 2", AppState.ORGANIZATION_PUBLISH);



        group.getOwnedApplications().add(application);
        group.getOwnedApplications().add(application2);

        organization.getGroups().add(group);

        userService.save(user);

        organizationService.getAll();

        if (groupRole != null) {
            UserDomain userDomainGroup = new UserDomain();
            userDomainGroup.setUser(user);
            userDomainGroup.setDomain(group);
            userDomainGroup.setRole(groupRole);

            user.getUserDomains().add(userDomainGroup);
        }

        if (orgRole != null) {
            UserDomain userDomainOrg = new UserDomain();
            userDomainOrg.setUser(user);
            userDomainOrg.setDomain(organization);
            userDomainOrg.setRole(orgRole);

            user.getUserDomains().add(userDomainOrg);
        }

        userService.save(user);

        entityManager.flush();

        return user;
    }

    private Application createApplication(Group group, Category category, String applicationName, AppState appState) {
        Application application = new Application();
        application.setName(applicationName);
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setOwnedGroup(group);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(appState);

        application.getApplicationVersions().add(applicationVersion);

        entityManager.flush();

        return application;
    }

    private Category createCategory(Organization organization) {
        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);

        organization.getCategories().add(category);

        categoryService.add(category);

        return category;
    }

    private Organization createOrganization() {
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

    private Group createGroup(Organization organization) {
        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.save(group);

        return group;
    }

    private UserDomain createUserDomain(User user, Domain domain, UserRole userRole) {
        UserDomain userDomain = new UserDomain();
        userDomain.setUser(user);
        userDomain.setDomain(domain);
        userDomain.setRole(roleService.getRoleByAuthority(userRole.name()));

        user.getUserDomains().add(userDomain);

        return userDomain;
    }
}
