package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class DomainUserRequestServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private DomainUserRequestService domainUserRequestService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private RoleService roleService;

    @Autowired(required = true)
    private CategoryService categoryService;

    @Test
    public void updateTest() {

        User user = getUser();
        Group group = getGroup(user, UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_ADMIN);

        DomainUserRequest domainUserRequest = new DomainUserRequest();
        domainUserRequest.setDomain(group);
        domainUserRequest.setUser(user);
        domainUserRequest.setStatus(Status.PENDING);
        domainUserRequestService.add(domainUserRequest);
        List<DomainUserRequest> domainUserRequestList = domainUserRequestService.getAll(group.getId());
        assertTrue(domainUserRequestList.size() == 1);
        domainUserRequest = domainUserRequestList.get(0);
        domainUserRequest.setStatus(Status.ACCEPTED);
        domainUserRequestService.update(domainUserRequest);
        domainUserRequestList = domainUserRequestService.getAll(group.getId(), Status.ACCEPTED);
        assertTrue(domainUserRequestList.size() == 1);
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

    private Group getGroup(User user, UserRole organizationUserRole, UserRole groupUserRole) {
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

        group.getOwnedApplications().add(application);

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

        return group;
    }
}
