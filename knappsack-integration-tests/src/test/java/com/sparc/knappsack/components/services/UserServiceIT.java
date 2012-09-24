package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

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
    private GroupUserRequestService requestService;

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
        List<UserDomain> userGroupDomains = userDomainService.getAll(groups.get(0).getId(), DomainType.GROUP);
        assertTrue(userGroupDomains.size() == 1);
        userService.delete(user.getId());
        userGroupDomains = userDomainService.getAll(groups.get(0).getId(), DomainType.GROUP);
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
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        Group newGroup = new Group();
        newGroup.setName("New Group");
        groupService.add(newGroup);

        groupService.getAll();

        userService.addUserToGroup(user, newGroup.getId(), UserRole.ROLE_GROUP_USER);
        List<Group> groups = userService.getGroups(user);
        assertTrue(groups.size() == 2);
        assertTrue(userService.isUserInGroup(user, newGroup));
        assertTrue(userService.isUserInGroup(user, newGroup, UserRole.ROLE_GROUP_USER));
    }

    @Test
    public void addUserToOrganizationTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        Organization newOrganization = new Organization();
        newOrganization.setName("New Organization");
        organizationService.add(newOrganization);

        organizationService.getAll();

        userService.addUserToOrganization(user, newOrganization.getId(), UserRole.ROLE_ORG_USER);
        List<Organization> organizations = userService.getOrganizations(user);
        assertTrue(organizations.size() == 2);
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
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        Organization newOrganization = new Organization();
        newOrganization.setName("New Organization");
        organizationService.add(newOrganization);

        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("New Group");
        group.setOrganization(newOrganization);
        groupService.save(group);
        groupService.getAll();

        GroupUserRequest groupUserRequest = requestService.createGroupUserRequest(user, group.getAccessCode());
        requestService.getAll(group.getId());

        boolean isAdded = userService.addUserToGroup(groupUserRequest.getUser(), groupUserRequest.getGroup().getId(), UserRole.ROLE_GROUP_USER);
        assertTrue(isAdded);
    }

    @Test
    public void addUserAdminToGroup() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        Organization newOrganization = new Organization();
        newOrganization.setName("New Organization");
        organizationService.add(newOrganization);

        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("New Group");
        group.setOrganization(newOrganization);
        groupService.save(group);
        groupService.getAll();

        GroupUserRequest groupUserRequest = requestService.createGroupUserRequest(user, group.getAccessCode());
        requestService.getAll(group.getId());

        boolean isAdded = userService.addUserToGroup(groupUserRequest.getUser(), groupUserRequest.getGroup().getId(), UserRole.ROLE_GROUP_ADMIN);
        assertTrue(isAdded);
    }

    @Test
    public void isUserInDomainTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);

        List<Group> groups = userService.getGroups(user);
        for (Group group : groups) {
            boolean isInDomain = userService.isUserInDomain(user, group.getId(), DomainType.GROUP, UserRole.ROLE_GROUP_USER);
            assertTrue(isInDomain);
        }

        List<Organization> organizations = userService.getOrganizations(user);
        for (Organization organization : organizations) {
            boolean isInDomain = userService.isUserInDomain(user, organization.getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_USER);
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
        Application application = createApplication(category, "Test Application", AppState.GROUP_PUBLISH);
        Group group = createGroup(organization);
        group.getOwnedApplications().add(application);

        entityManager.flush();

        createUserDomain(user, group.getId(), DomainType.GROUP, UserRole.ROLE_GROUP_ADMIN);
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
        createUserDomain(user, organization.getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
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
        createUserDomain(user, organization.getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_USER);
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

        Application application = createApplication(organization.getCategories().get(0), "Test Application", AppState.GROUP_PUBLISH);
        Application application2 = createApplication(organization.getCategories().get(0), "Test Application 2", AppState.ORGANIZATION_PUBLISH);

        Group group = createGroup(organization);


        group.getOwnedApplications().add(application);
        group.getOwnedApplications().add(application2);

        organization.getGroups().add(group);

        userService.save(user);

        organizationService.getAll();

        if (groupRole != null) {
            UserDomain userDomainGroup = new UserDomain();
            userDomainGroup.setUser(user);
            userDomainGroup.setDomainId(group.getId());
            userDomainGroup.setDomainType(DomainType.GROUP);
            userDomainGroup.setRole(groupRole);
            userDomainGroup.setDomainId(group.getId());

            user.getUserDomains().add(userDomainGroup);
        }

        if (orgRole != null) {
            UserDomain userDomainOrg = new UserDomain();
            userDomainOrg.setUser(user);
            userDomainOrg.setDomainId(group.getId());
            userDomainOrg.setDomainType(DomainType.ORGANIZATION);
            userDomainOrg.setRole(orgRole);
            userDomainOrg.setDomainId(organization.getId());

            user.getUserDomains().add(userDomainOrg);
        }

        userService.save(user);

        entityManager.flush();

        return user;
    }

    private Application createApplication(Category category, String applicationName, AppState appState) {
        Application application = new Application();
        application.setName(applicationName);
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);

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

        return category;
    }

    private Organization createOrganization() {
        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization 2");
        organization.setDomainConfiguration(new DomainConfiguration());
        organization.getCategories().add(createCategory(organization));
        organizationService.add(organization);

        return organization;
    }

    private Group createGroup(Organization organization) {
        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.save(group);

        return group;
    }

    private UserDomain createUserDomain(User user, Long domainId, DomainType domainType, UserRole userRole) {
        UserDomain userDomain = new UserDomain();
        userDomain.setUser(user);
        userDomain.setDomainId(domainId);
        userDomain.setDomainType(domainType);
        userDomain.setRole(roleService.getRoleByAuthority(userRole.name()));

        user.getUserDomains().add(userDomain);

        return userDomain;
    }
}
