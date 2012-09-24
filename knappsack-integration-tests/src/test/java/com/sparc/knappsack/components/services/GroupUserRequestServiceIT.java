package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: xosis
 * Date: 7/7/12
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class GroupUserRequestServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private GroupUserRequestService groupUserRequestService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private RoleService roleService;

    @Test
    public void updateTest() {

        User user = getUser();
        Group group = getGroup(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_ADMIN);

        GroupUserRequest groupUserRequest = new GroupUserRequest();
        groupUserRequest.setGroup(group);
        groupUserRequest.setUser(user);
        groupUserRequest.setStatus(Status.PENDING);
        groupUserRequestService.add(groupUserRequest);
        List<GroupUserRequest> groupUserRequestList = groupUserRequestService.getAll(group.getId());
        assertTrue(groupUserRequestList.size() == 1);
        groupUserRequest = groupUserRequestList.get(0);
        groupUserRequest.setStatus(Status.ACCEPTED);
        groupUserRequestService.update(groupUserRequest);
        groupUserRequestList = groupUserRequestService.getAll(group.getId(), Status.ACCEPTED);
        assertTrue(groupUserRequestList.size() == 1);
    }

    private Group getGroup(UserRole organizationUserRole, UserRole groupUserRole) {
        User user = getUser();

        Role groupRole = roleService.getRoleByAuthority(groupUserRole.name());
        Role orgRole = roleService.getRoleByAuthority(organizationUserRole.name());

        user.getRoles().add(groupRole);
        user.getRoles().add(orgRole);

        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization 2");
        organizationService.add(organization);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.save(group);

        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);

        group.getOwnedApplications().add(application);

        organization.getGroups().add(group);

        userService.save(user);

        organizationService.getAll();

        UserDomain userDomainGroup = new UserDomain();
        userDomainGroup.setUser(user);
        userDomainGroup.setDomainId(group.getId());
        userDomainGroup.setDomainType(DomainType.GROUP);
        userDomainGroup.setRole(groupRole);
        userDomainGroup.setDomainId(group.getId());

        UserDomain userDomainOrg = new UserDomain();
        userDomainOrg.setUser(user);
        userDomainOrg.setDomainId(group.getId());
        userDomainOrg.setDomainType(DomainType.ORGANIZATION);
        userDomainOrg.setRole(orgRole);
        userDomainOrg.setDomainId(organization.getId());

        user.getUserDomains().add(userDomainGroup);
        user.getUserDomains().add(userDomainOrg);

        userService.save(user);

        return group;
    }
}
