package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.ApplicationModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

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

    @Test
    public void searchNoResultsTest() {
        List<ApplicationModel> applicationModels = searchService.searchApplications("", getUser(), ApplicationType.ANDROID);
        assertTrue(applicationModels.size() == 0);
    }

    @Test
    public void searchTest() {
        User user = getUser(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_USER);
        List<ApplicationModel> applicationModels = searchService.searchApplications("Test", user, ApplicationType.ANDROID);
        assertTrue(applicationModels.size() == 2);
    }

    private User getUser(UserRole organizationUserRole, UserRole groupUserRole) {
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

        Application application2 = new Application();
        application2.setName("Test Application2");
        application2.setApplicationType(ApplicationType.ANDROID);
        application2.setCategory(category);

        ApplicationVersion applicationVersion2 = new ApplicationVersion();
        applicationVersion2.setVersionName("1.0.0");
        applicationVersion2.setApplication(application2);
        applicationVersion2.setAppState(AppState.ORGANIZATION_PUBLISH);

        application.getApplicationVersions().add(applicationVersion2);

        group.getOwnedApplications().add(application);
        group.getOwnedApplications().add(application2);

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

        return user;
    }
}
