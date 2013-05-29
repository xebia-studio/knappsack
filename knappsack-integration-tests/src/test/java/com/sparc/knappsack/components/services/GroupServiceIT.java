package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.forms.GroupForm;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class GroupServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private DomainUserRequestService requestService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private DomainUserRequestService domainUserRequestService;

    @Test
    public void addTest() {
        Group group = getGroup();

        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        assertEquals(group, groups.get(0));
    }

    @Test
    public void updateTest() {
        Group group = getGroup();
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        assertEquals(group, groups.get(0));

        group = groups.get(0);
        group.setName("New Group");
        groupService.save(group);
        groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        group = groups.get(0);
        assertTrue(group.getName().equals("New Group"));
    }

    @Test
    public void deleteTest() {
        Group group = getGroup();
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        assertEquals(group, groups.get(0));

        groupService.delete(group.getId());
        groups = groupService.getAll();
        assertTrue(groups.size() == 0);
    }

    @Test
    public void getByAccessCodeTest() {
        Group group = getGroup();

        Group foundGroup = groupService.getByAccessCode(group.getUuid());
        assertNotNull(foundGroup);
        assertEquals(group, foundGroup);
    }

    @Test
    public void doesRequestExistTest() {
        User user = getUserWithSecurityContext();
        userService.add(user);

        Group group = getGroup();

        DomainUserRequest domainUserRequest = new DomainUserRequest();
        domainUserRequest.setDomain(group);
        domainUserRequest.setStatus(Status.PENDING);
        domainUserRequest.setUser(user);

        requestService.add(domainUserRequest);

        List<DomainUserRequest> domainUserRequests = requestService.getAll(group.getId());
        assertTrue(domainUserRequests.size() == 1);
        boolean isRequest = domainUserRequestService.doesRequestExist(user, group, Status.PENDING);
        assertTrue(isRequest);
    }

    @Test
    public void createGroupTest() {
        Organization organization = getOrganization();
        setActiveOrganizationOnUserInSecurityContext(organization);
        GroupForm groupForm = getGroupForm();
        groupService.createGroup(groupForm);

        List<Group> groups = groupService.getAll();
        assertEquals(groups.size(), 1);
        assertEquals(groups.get(0).getName(), groupForm.getName());
        assertEquals(groups.get(0).getOrganization(), organization);
    }

    @Test
    public void mapGroupToGroupFormTest() {
        Group group = getGroup();

        setActiveOrganizationOnUserInSecurityContext(group.getOrganization());

        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        assertEquals(group, groups.get(0));

        GroupForm groupForm = new GroupForm();
        groupService.mapGroupToGroupForm(group, groupForm);
        assertTrue(group.getId().equals(groupForm.getId()));
        assertTrue(group.getName().equals(groupForm.getName()));
        assertEquals(group.getOrganization(), getUserWithSecurityContext().getActiveOrganization());
    }

    @Test
    public void editGroupTest() {
        Group group = getGroup();
        setActiveOrganizationOnUserInSecurityContext(group.getOrganization());

        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        assertEquals(group, groups.get(0));

        GroupForm groupForm = new GroupForm();
        groupForm.setName("New Group");
        groupForm.setId(group.getId());
        groupService.editGroup(groupForm);
        group = groupService.get(group.getName(), group.getOrganization().getId());
        assertNotNull(group);
        assertTrue(group.getName().equals(groupForm.getName()));
    }

    @Test
    public void removeUserFromGroupTest() {
        User user = getUserWithSecurityContext();
        userService.add(user);

        Group group = getGroup();

        userService.addUserToGroup(user, group.getId(), UserRole.ROLE_GROUP_USER);
        List<Group> groups = userService.getGroups(user, SortOrder.ASCENDING);
        assertTrue(groups.size() == 1);
        groupService.removeUserFromGroup(group.getId(), user.getId());
        groups = userService.getGroups(user, SortOrder.ASCENDING);
        assertTrue(groups.size() == 0);
    }

    @Test
    public void getGuestGroupsTest(){
        Group group = getGroup();

        Group group2 = new Group();
        group2.setName("Test Group 2");
        group2.setOrganization(group.getOrganization());
        group2.setGuestApplicationVersions(new ArrayList<ApplicationVersion>());
        group2.getGuestApplicationVersions().add(group.getOwnedApplications().get(0).getApplicationVersions().get(0));
        group.getOwnedApplications().get(0).getApplicationVersions().get(0).getGuestGroups().add(group2);
        groupService.save(group2);

        ApplicationVersion applicationVersion = group.getOwnedApplications().get(0).getApplicationVersions().get(0);
        List<Group> guestGroups = groupService.getGuestGroups(applicationVersion);
        assertTrue(guestGroups.size() == 1);
    }

    @Test
    public void getOwnedGroupTest() {
        Group group = getGroup();
        Application application = group.getOwnedApplications().get(0);
        Group ownedGroup = groupService.getOwnedGroup(application);
        assertTrue(group.equals(ownedGroup));
    }

    private GroupForm getGroupForm() {
        GroupForm groupForm = new GroupForm();
        groupForm.setName("Test Group");

        return groupForm;
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
        assertTrue(organizationService.doesEntityExist(organization.getId()));

        return organization;
    }

    private Group getGroup() {
        Organization organization = getOrganization();

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
        application.setStorageConfiguration(organization.getStorageConfigurations().get(0));
        application.setOwnedGroup(group);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);
        applicationService.add(application);

        group.getOwnedApplications().add(application);
        groupService.save(group);
        application.setOwnedGroup(group);

        organization.getGroups().add(group);

        organizationService.getAll();

        return group;
    }
}

