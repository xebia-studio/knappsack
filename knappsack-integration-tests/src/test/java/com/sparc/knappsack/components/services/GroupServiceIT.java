package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.GroupForm;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class GroupServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private GroupUserRequestService requestService;

    @Autowired(required = true)
    private UserService userService;

    @Test
    public void addTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        organization.getGroups().add(group);

        groupService.save(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
    }

    @Test
    public void updateTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        organization.getGroups().add(group);

        groupService.add(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);

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
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        organization.getGroups().add(group);

        groupService.add(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);

        groupService.delete(group.getId());
        groups = groupService.getAll();
        assertTrue(groups.size() == 0);
    }

    @Test
    public void getByAccessCodeTest() {
        Group group = new Group();
        group.setName("Test Group");
        group.setAccessCode(UUID.randomUUID().toString());
        groupService.add(group);

        group = groupService.getByAccessCode(group.getAccessCode());
        assertNotNull(group);
    }

    @Test
    public void doesRequestExistTest() {
        User user = getUser();
        userService.add(user);

        Group group = getGroup();

        GroupUserRequest groupUserRequest = new GroupUserRequest();
        groupUserRequest.setGroup(group);
        groupUserRequest.setStatus(Status.PENDING);
        groupUserRequest.setUser(user);

        requestService.add(groupUserRequest);

        List<GroupUserRequest> groupUserRequests = requestService.getAll(group.getId());
        assertTrue(groupUserRequests.size() == 1);
        boolean isRequest = groupService.doesRequestExist(user, group, Status.PENDING);
        assertTrue(isRequest);
    }

    @Test
    public void createGroupTest() {
        GroupForm groupForm = getGroupForm();
        groupService.createGroup(groupForm);

        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);
        assertTrue(groups.get(0).getName().equals(groupForm.getName()));
    }

    @Test
    public void mapGroupToGroupFormTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        organization.getGroups().add(group);

        groupService.add(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);

        GroupForm groupForm = new GroupForm();
        groupService.mapGroupToGroupForm(group, groupForm);
        assertTrue(group.getId().equals(groupForm.getId()));
        assertTrue(group.getName().equals(groupForm.getName()));
        assertTrue(group.getOrganization().getId().equals(groupForm.getOrganizationId()));
    }

    @Test
    public void editGroupTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        organization.getGroups().add(group);

        groupService.add(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);

        GroupForm groupForm = new GroupForm();
        groupForm.setName("New Group");
        groupForm.setId(group.getId());
        groupForm.setOrganizationId(group.getOrganization().getId());
        groupService.editGroup(groupForm);
        group = groupService.get(group.getName(), organization.getId());
        assertNotNull(group);
        assertTrue(group.getName().equals(groupForm.getName()));
    }

    @Test
    public void removeUserFromGroupTest() {
        User user = getUser();
        userService.add(user);

        Group group = new Group();
        group.setName("Test Group");
        group.setAccessCode(UUID.randomUUID().toString());
        groupService.add(group);
        groupService.getAll();

        userService.addUserToGroup(user, group.getId(), UserRole.ROLE_GROUP_USER);
        List<Group> groups = userService.getGroups(user);
        assertTrue(groups.size() == 1);
        groupService.removeUserFromGroup(group.getId(), user.getId());
        groups = userService.getGroups(user);
        assertTrue(groups.size() == 0);
    }

    @Test
    public void getGuestGroupsTest(){
        Group group = getGroup();
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
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        GroupForm groupForm = new GroupForm();
        groupForm.setName("Test Group");
        groupForm.setOrganizationId(organization.getId());

        return groupForm;
    }

    private Group getGroup() {
        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization 2");
        organizationService.add(organization);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);

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

        return group;
    }
}

