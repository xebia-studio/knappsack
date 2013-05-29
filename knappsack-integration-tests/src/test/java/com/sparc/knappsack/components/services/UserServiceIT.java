package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
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

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    private StorageConfiguration storageConfiguration;

    @Before
    public void setup() {
        super.setup();
        storageConfiguration = createStorageConfiguration();
    }

    @Test
    public void addTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);

        userService.add(user);
        user = userService.getByEmail(user.getEmail());
        assertNotNull(user);
    }

    @Test
    public void getByEmailTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);

        user = userService.getByEmail(user.getEmail());
        assertNotNull(user);
    }

    @Test
    public void updateTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);
        user.setLastName("Tester");

        user = userService.getByEmail(user.getEmail());
        userService.update(user);
        assertEquals(user.getLastName(), "Tester");
    }

    @Test
    public void deleteTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);

        List<Group> groups = userService.getGroups(user, SortOrder.ASCENDING);
        assertTrue(groups.size() == 1);
        List<UserDomain> userGroupDomains = userDomainService.getAll(groups.get(0).getId());
        assertTrue(userGroupDomains.size() == 1);
        userService.delete(user.getId());
        userGroupDomains = userDomainService.getAll(groups.get(0).getId());
        assertTrue(userGroupDomains.size() == 0);
    }

    @Test
    public void getGroupsForOrgAdminTest() {
        User user = getUser(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);
        user = userService.getByEmail(user.getEmail());

        List<Group> groups = userService.getGroups(user, SortOrder.ASCENDING);
        assertTrue(groups.size() == 1);
    }

    @Test
    public void getOrganizationsTest() {
        User user = getUser(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);
        user = userService.getByEmail(user.getEmail());

        List<Organization> organizations = userService.getOrganizations(user, SortOrder.ASCENDING);
        assertTrue(organizations.size() == 1);
    }

    @Test
    public void activationTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", false, false);
        user = userService.getByEmail(user.getEmail());
        assertFalse(user.isActivated());
        boolean activated = userService.activate(user.getId(), user.getActivationCode());
        assertTrue(activated);
        assertTrue(user.isActivated());
    }

    @Test
    public void getApplicationsByUserApplicationType() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);
        user = userService.getByEmail(user.getEmail());
        List<Application> applications = userService.getApplicationsForUser(user, ApplicationType.ANDROID);
        assertTrue(applications.size() == 1);
    }

    @Test
    public void getApplicationsByUserApplicationTypeCategoryTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);
        user = userService.getByEmail(user.getEmail());
        Organization organization = organizationService.getAll().get(0);
        Category category = organization.getCategories().get(0);
        List<Application> applications = userService.getApplicationsForUser(user, ApplicationType.ANDROID, category.getId());
        assertTrue(applications.size() == 1);
    }

    @Test
    public void addUserToGroupTest() {
        User user = createUser("user1@knappsack.com", true, false);

        Group group = createGroup(createOrganization("test organization"), "test group");

        userService.addUserToGroup(user, group.getId(), UserRole.ROLE_GROUP_USER);
        List<Group> groups = userService.getGroups(user, SortOrder.ASCENDING);
        assertTrue(groups.size() == 1);
        assertNotNull(userDomainService.get(user, group.getId()));
        assertNotNull(userDomainService.get(user, group.getId(), UserRole.ROLE_GROUP_USER));
    }

    @Test
    public void addUserToOrganizationTest() {
        User user = createUser("user1@knappsack.com", true, false);

        Organization newOrganization = createOrganization("test organization");

        userService.addUserToOrganization(user, newOrganization.getId(), UserRole.ROLE_ORG_USER);
        List<Organization> organizations = userService.getOrganizations(user, SortOrder.ASCENDING);
        assertTrue(organizations.size() == 1);
        assertNotNull(userDomainService.get(user, newOrganization.getId(), UserRole.ROLE_ORG_USER));
    }

    @Test
    public void changePasswordTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);
//        user.setPasswordExpired(true);

        boolean isChanged = userService.changePassword(user, "password", true);
        assertTrue(isChanged);
    }


    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUser_VersionOrgPublishAppState_Test() {
        Organization organization = createOrganization("first organization");
        Organization organization2 = createOrganization("second organization");
        Group group = createGroup(organization, "test group");
        Group group2 = createGroup(organization, "second group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion = createApplicationVersion(application, "1.0", AppState.ORGANIZATION_PUBLISH);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        setActiveOrganizationOnUser(userOrgAdmin, organization);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userOrgMember, organization);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userGroupMember, organization);
        User userSecondGroupMember = createUser("secondgroupmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userSecondGroupMember, organization);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Group Member for group which is NOT Application parent group
        UserDomain secondGroupMemberUserDomain = userService.addUserToDomain(userSecondGroupMember, group2, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(secondGroupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(secondGroupMemberUserDomain.getId()));

        // Org admin should see application and all versions
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(userOrgAdmin, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Org user should see application and all versions
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        applicationVersions = userService.getApplicationVersions(userOrgMember, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Group Member for application parent group should see application and all versions
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        applicationVersions = userService.getApplicationVersions(userGroupMember, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Group Member for group which is NOT Application parent group should not see application nor any versions for it
        applications = userService.getApplicationsForUser(userSecondGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userSecondGroupMember, application.getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);
        setActiveOrganizationOnUser(userSecondGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userSecondGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userSecondGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUser_VersionRequestPublishAppState_Test() {
        Organization organization = createOrganization("test organization");
        Organization organization2 = createOrganization("second organization");
        Group group = createGroup(organization, "test group");
        Group guestGroup = createGroup(organization, "guest group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion = createApplicationVersion(application, "1.0", AppState.ORG_PUBLISH_REQUEST, guestGroup);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        setActiveOrganizationOnUser(userOrgAdmin, organization);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userOrgMember, organization);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userGroupMember, organization);
        User userGuestGroupMember = createUser("guestgroupmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userGuestGroupMember, organization);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Group Member of group which has guest access applicationVersion shared to it
        UserDomain guestGroupMemberUserDomain = userService.addUserToDomain(userGuestGroupMember, guestGroup, UserRole.ROLE_GROUP_USER);
        assertNotNull(guestGroupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(guestGroupMemberUserDomain.getId()));

        // Org admin should see application
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(userOrgAdmin, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Org user should not see application
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application parent group member should see application
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        applicationVersions = userService.getApplicationVersions(userGroupMember, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Application version guest group member should see application
        applications = userService.getApplicationsForUser(userGuestGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        applicationVersions = userService.getApplicationVersions(userGuestGroupMember, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);
        setActiveOrganizationOnUser(userGuestGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGuestGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGuestGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUser_VersionGroupPublishAppState_Test() {
        Organization organization = createOrganization("test organization");
        Organization organization2 = createOrganization("second organization");
        Group group = createGroup(organization, "test group");
        Group guestGroup = createGroup(organization, "guest group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion = createApplicationVersion(application, "1.0", AppState.GROUP_PUBLISH, guestGroup);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        setActiveOrganizationOnUser(userOrgAdmin, organization);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userOrgMember, organization);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userGroupMember, organization);
        User userGuestGroupMember = createUser("guestgroupmember@knappsack.com", true, false);
        setActiveOrganizationOnUser(userGuestGroupMember, organization);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Group Member of group which has guest access applicationVersion shared to it
        UserDomain guestGroupMemberUserDomain = userService.addUserToDomain(userGuestGroupMember, guestGroup, UserRole.ROLE_GROUP_USER);
        assertNotNull(guestGroupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(guestGroupMemberUserDomain.getId()));

        // Org admin should see application
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(userOrgAdmin, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Org user should not see application
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application parent group member should see application
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        applicationVersions = userService.getApplicationVersions(userGroupMember, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        applications = null;
        applicationVersions = null;

        // Application version guest group member should see application
        applications = userService.getApplicationsForUser(userGuestGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        assertEquals(applications.get(0), application);
        applicationVersions = userService.getApplicationVersions(userGuestGroupMember, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);
        setActiveOrganizationOnUser(userGuestGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGuestGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGuestGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void getGroupModelsForActiveOrganizationTest() {
        User user = createUser("user1@knappsack.com", true, false);
        Organization organization = createOrganization("test organization");
        Group group = createGroup(organization, "test group");

        user.setActiveOrganization(organization);

        userService.addUserToGroup(user, group.getId(), UserRole.ROLE_GROUP_USER);
        List<Group> groups = userService.getGroups(user, SortOrder.ASCENDING);
        assertTrue(groups.size() == 1);
        assertNotNull(userDomainService.get(user, group.getId()));
        assertNotNull(userDomainService.get(user, group.getId(), UserRole.ROLE_GROUP_USER));

        List<com.sparc.knappsack.models.api.v1.Group> groupModels = userService.getGroupModelsForActiveOrganization(user, com.sparc.knappsack.models.api.v1.Group.class, SortOrder.ASCENDING);
        com.sparc.knappsack.models.api.v1.Group groupModel = groupModels.get(0);
        assertEquals(group.getName(), groupModel.getName());
        assertEquals(group.getId(), groupModel.getId());
    }

    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUser_VersionsEmpty_Test() {
        Organization organization = createOrganization("test organization");
        Organization organization2 = createOrganization("second Organization");
        Group group = createGroup(organization, "test group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Org admin should not see application or any versions (since there aren't any)
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        List<ApplicationVersion> applicationVersions = applicationVersions = userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Org user should not see application or any versions (since there aren't any)
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = applicationVersions = userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application parent group member should not see application or any versions (since there aren't any)
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        applicationVersions = applicationVersions = userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);
        assertEquals(applications.size(), 0);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUser_VersionsAllDisabled_Test() {
        Organization organization = createOrganization("test organization");
        Organization organization2 = createOrganization("second organization");
        Group group = createGroup(organization, "test group");
        Group guestGroup = createGroup(organization, "guest group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion1 = createApplicationVersion(application, "1.0", AppState.DISABLED);
        ApplicationVersion applicationVersion2 = createApplicationVersion(application, "1.0", AppState.DISABLED, guestGroup);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);
        User userGuestGroupMember = createUser("guestgroupmember@knappsack.com", true, false);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Group Member of group which has guest access to applicationVersion2
        UserDomain guestGroupMemberUserDomain = userService.addUserToDomain(userGuestGroupMember, guestGroup, UserRole.ROLE_GROUP_USER);
        assertNotNull(guestGroupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(guestGroupMemberUserDomain.getId()));

        // Org admin should not see applications since all versions are disabled
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(userOrgAdmin, application.getId(), null);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Org user should not see applications since all versions are disabled
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userOrgMember, application.getId(), null);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application parent group member should not see applications since all versions are disabled
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userGroupMember, application.getId(), null);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application version guest group member should not see application since all versions are disabled
        applications = userService.getApplicationsForUser(userGuestGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userGuestGroupMember, application.getId(), null);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);
        setActiveOrganizationOnUser(userGuestGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGuestGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGuestGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUser_TwoVersions_SecondSharedToGuestGroup_Test() {
        Organization organization = createOrganization("test organization");
        Organization organization2 = createOrganization("second organization");
        Group group = createGroup(organization, "test group");
        Group guestGroup = createGroup(organization, "guest group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion1 = createApplicationVersion(application, "1.0", AppState.GROUP_PUBLISH);
        ApplicationVersion applicationVersion2 = createApplicationVersion(application, "2.0", AppState.GROUP_PUBLISH, guestGroup);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);
        User userGuestGroupMember = createUser("guestgroupmember@knappsack.com", true, false);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Group Member of group which has guest access to applicationVersion2
        UserDomain guestGroupMemberUserDomain = userService.addUserToDomain(userGuestGroupMember, guestGroup, UserRole.ROLE_GROUP_USER);
        assertNotNull(guestGroupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(guestGroupMemberUserDomain.getId()));

        // Org admin should not see applications
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        // Org admin should see all application versions
        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(userOrgAdmin, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 2);
        assertEquals(applicationVersions.get(0), applicationVersion2);
        assertEquals(applicationVersions.get(1), applicationVersion1);

        applications = null;
        applicationVersions = null;

        // Org user should not see applications
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        // Org user should not see any versions
        applicationVersions = userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application parent group member should see applications
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        // Application parent group member should see both application versions
        applicationVersions = userService.getApplicationVersions(userGroupMember, applications.get(0).getId(), SortOrder.ASCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 2);
        assertEquals(applicationVersions.get(0), applicationVersion1);
        assertEquals(applicationVersions.get(1), applicationVersion2);

        applications = null;
        applicationVersions = null;

        // Guest Group Member should should application due to applicationVersion2 being shared with group
        applications = userService.getApplicationsForUser(userGuestGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        // Only applicationVersion2 should be available
        applicationVersions = userService.getApplicationVersions(userGuestGroupMember, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion2);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);
        setActiveOrganizationOnUser(userGuestGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGuestGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGuestGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void getApplicationsForUser_And_getApplicaionVersionsForUservv_TwoVersions_SecondSharedToGuestGroupButDisabled_Test() {
        Organization organization = createOrganization("test organization");
        Organization organization2 = createOrganization("second organization");
        Group group = createGroup(organization, "test group");
        Group guestGroup = createGroup(organization, "guest group");
        Category category = createCategory(organization);

        Application application = createApplication(group, category, "test app", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion1 = createApplicationVersion(application, "1.0", AppState.GROUP_PUBLISH);
        ApplicationVersion applicationVersion2 = createApplicationVersion(application, "2.0", AppState.DISABLED, guestGroup);

        User userOrgAdmin = createUser("orgadmin@knappsack.com", true, false);
        User userOrgMember = createUser("orgmember@knappsack.com", true, false);
        User userGroupMember = createUser("groupmember@knappsack.com", true, false);
        User userGuestGroupMember = createUser("guestgroupmember@knappsack.com", true, false);

        // Org Admin
        UserDomain orgAdminUserDomain = userService.addUserToDomain(userOrgAdmin, organization, UserRole.ROLE_ORG_ADMIN);
        assertNotNull(orgAdminUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgAdminUserDomain.getId()));

        // Org User
        UserDomain orgMemberUserDomain = userService.addUserToDomain(userOrgMember, organization, UserRole.ROLE_ORG_USER);
        assertNotNull(orgMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(orgMemberUserDomain.getId()));

        // Group Member for application parent group
        UserDomain groupMemberUserDomain = userService.addUserToDomain(userGroupMember, group, UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(groupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(groupMemberUserDomain.getId()));

        // Group Member of group which has guest access to applicationVersion2
        UserDomain guestGroupMemberUserDomain = userService.addUserToDomain(userGuestGroupMember, guestGroup, UserRole.ROLE_GROUP_USER);
        assertNotNull(guestGroupMemberUserDomain);
        assertTrue(userDomainService.doesEntityExist(guestGroupMemberUserDomain.getId()));

        // Org admin should see application and only first version since applicationVersion2 is disabled
        List<Application> applications = userService.getApplicationsForUser(userOrgAdmin);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(userOrgAdmin, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion1);

        applications = null;
        applicationVersions = null;

        // Org user should not see applications or versions since all versions are published to group or disabled
        applications = userService.getApplicationsForUser(userOrgMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        applications = null;
        applicationVersions = null;

        // Application parent group member should see applications and only applicationVersion1 since it is published to the group and applicationVersion2 is disabled
        applications = userService.getApplicationsForUser(userGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 1);
        applicationVersions = userService.getApplicationVersions(userGroupMember, applications.get(0).getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 1);
        assertEquals(applicationVersions.get(0), applicationVersion1);

        applications = null;
        applicationVersions = null;

        // Application version guest group member should not see application or version since the version that is shared is disabled
        applications = userService.getApplicationsForUser(userGuestGroupMember);
        assertNotNull(applications);
        assertEquals(applications.size(), 0);
        applicationVersions = userService.getApplicationVersions(userGuestGroupMember, application.getId(), SortOrder.DESCENDING);
        assertNotNull(applicationVersions);
        assertEquals(applicationVersions.size(), 0);

        // Change activeOrganization for all users.  All should not have any access
        setActiveOrganizationOnUser(userOrgAdmin, organization2);
        setActiveOrganizationOnUser(userOrgMember, organization2);
        setActiveOrganizationOnUser(userGroupMember, organization2);
        setActiveOrganizationOnUser(userGuestGroupMember, organization2);

        assertEquals(userService.getApplicationsForUser(userOrgAdmin).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgAdmin, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userOrgMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userOrgMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
        assertEquals(userService.getApplicationsForUser(userGuestGroupMember).size(), 0);
        assertEquals(userService.getApplicationVersions(userGuestGroupMember, application.getId(), SortOrder.DESCENDING).size(), 0);
    }

    @Test
    public void addUserToGroup() {
        User user = getUserWithSecurityContext();

        Group group = createGroup(createOrganization("test organization"), "test group");

        DomainUserRequest domainUserRequest = domainUserRequestService.createDomainUserRequest(user, group.getUuid());
        requestService.getAll(group.getId());

        UserDomain userDomain = userService.addUserToGroup(domainUserRequest.getUser(), domainUserRequest.getDomain().getId(), UserRole.ROLE_GROUP_USER);
        assertNotNull(userDomain);
    }

    @Test
    public void addUserAdminToGroup() {
        User user = getUserWithSecurityContext();

        Group group = createGroup(createOrganization("test organization"), "test group");

        DomainUserRequest domainUserRequest = domainUserRequestService.createDomainUserRequest(user, group.getUuid());
        requestService.getAll(group.getId());

        UserDomain userDomain = userService.addUserToGroup(domainUserRequest.getUser(), domainUserRequest.getDomain().getId(), UserRole.ROLE_GROUP_ADMIN);
        assertNotNull(userDomain);
    }

    @Test
    public void isUserInDomainTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);

        List<Group> groups = userService.getGroups(user, SortOrder.ASCENDING);
        for (Group group : groups) {
            UserDomain userDomain = userDomainService.get(user, group.getId(), UserRole.ROLE_GROUP_USER);
            assertNotNull(userDomain);
        }

        List<Organization> organizations = userService.getOrganizations(user, SortOrder.ASCENDING);
        for (Organization organization : organizations) {
            UserDomain userDomain = userDomainService.get(user, organization.getId(), UserRole.ROLE_ORG_USER);
            assertNotNull(userDomain);
        }
    }

    @Test
    public void getCategoriesForUsersTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER, "user1@knappsack.com", true, false);

        List<Category> categories = userService.getCategoriesForUser(user, ApplicationType.ANDROID, SortOrder.ASCENDING);
        assertTrue(categories.size() == 1);
    }

    @Test
    public void updateSecurityContextTest() {
        User user = getUserWithSecurityContext();

        boolean isUpdated = userService.updateSecurityContext(user);
        assertTrue(isUpdated);
    }

    @Test
    public void canUserEditApplicationTest() {
        //Initial setup
        User user = getUserWithSecurityContext();
        userService.add(user);
        Organization organization = createOrganization("test organization");
        Category category = createCategory(organization);
        organization.getCategories().add(category);
        Group group = createGroup(organization, "Test Group");
        Application application = createApplication(group, category, "Test Application", ApplicationType.ANDROID);

        ApplicationVersion applicationVersion = createApplicationVersion(application, "1.0", AppState.GROUP_PUBLISH);

        entityManager.flush();

        createUserDomain(user, group, UserRole.ROLE_GROUP_ADMIN);
        entityManager.flush();

        assertTrue(userService.canUserEditApplication(user.getId(), application.getId()));

        //Reset
        userService.delete(user.getId());
        ReflectionTestUtils.setField(this, "user", null);
        entityManager.flush();

        //Test if org admin can edit application
        user = getUserWithSecurityContext();
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
        user = getUserWithSecurityContext();
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
        user = getUserWithSecurityContext();
        userService.add(user);
        entityManager.flush();
        assertFalse(userService.canUserEditApplication(user.getId(), application.getId()));
    }

    private User getUser(UserRole organizationUserRole, UserRole groupUserRole, String email, boolean activated, boolean passwordExpired) {
        User user = createUser(email, activated, passwordExpired);

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

        Organization organization = createOrganization("test organization");

        Category category = createCategory(organization);
        Category category2 = createCategory(organization);

        Group group = createGroup(organization, "test group");

        Application application = createApplication(group, organization.getCategories().get(0), "Test Application", ApplicationType.ANDROID);
        ApplicationVersion applicationVersion1 = createApplicationVersion(application, "1.0", AppState.GROUP_PUBLISH);

        Application application2 = createApplication(group, organization.getCategories().get(1), "Test Application 2", ApplicationType.IOS);
        ApplicationVersion applicationVersion2 = createApplicationVersion(application2, "1.0", AppState.ORGANIZATION_PUBLISH);

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

    private Application createApplication(Group group, Category category, String applicationName, ApplicationType applicationType) {
        Application application = new Application();
        application.setName(applicationName);
        application.setApplicationType(applicationType);
        application.setCategory(category);
        application.setOwnedGroup(group);

        group.getOwnedApplications().add(application);

        applicationService.add(application);
        assertTrue(applicationService.doesEntityExist(application.getId()));

        return application;
    }

    private ApplicationVersion createApplicationVersion(Application parentApplication, String versionName, AppState appState, Group... guestGroups) {
        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName(versionName);
        applicationVersion.setAppState(appState);
        applicationVersion.setApplication(parentApplication);
        if (guestGroups != null && guestGroups.length > 0) {
            applicationVersion.getGuestGroups().addAll(Arrays.asList(guestGroups));
        }
        parentApplication.getApplicationVersions().add(applicationVersion);
        applicationVersionService.add(applicationVersion);

        assertTrue(applicationVersionService.doesEntityExist(applicationVersion.getId()));

        return applicationVersion;
    }

    private Category createCategory(Organization organization) {
        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);

        organization.getCategories().add(category);

        categoryService.add(category);

        return category;
    }

    private StorageConfiguration createStorageConfiguration() {
        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(localStorageConfiguration);
        assertTrue(storageConfigurationService.doesEntityExist(localStorageConfiguration.getId()));

        return storageConfiguration;
    }

    private Organization createOrganization(String name) {
        Organization organization = new Organization();
        organization.setName(name);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.getStorageConfigurations().add(storageConfiguration);
        orgStorageConfig.setPrefix(StringUtils.trimAllWhitespace(name));
        orgStorageConfig.setOrganization(organization);
        organization.setOrgStorageConfig(orgStorageConfig);

        organizationService.add(organization);

        return organization;
    }

    private Group createGroup(Organization organization, String groupName) {
        Group group = new Group();
        group.setName(groupName);
        group.setOrganization(organization);

        organization.getGroups().add(group);

        groupService.save(group);
        assertTrue(groupService.doesEntityExist(group.getId()));

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
