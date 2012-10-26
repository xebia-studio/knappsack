package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ManagerController extends AbstractController {

    @Autowired(required = true)
    private GroupUserRequestService requestService;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private UserService userService;

    @RequestMapping(value = "/manager", method = RequestMethod.GET)
    public String manageApps(Model model) {

        User user = userService.getUserFromSecurityContext();
        List<GroupModel> adminGroups = new ArrayList<GroupModel>();
        List<OrganizationModel> adminOrganizations = new ArrayList<OrganizationModel>();
        populateAdminDomains(user, adminGroups, adminOrganizations);

        model.addAttribute("adminGroups", adminGroups);

        populatePendingRequests(model, adminGroups);

        List<UserRole> groupUserRoles = new ArrayList<UserRole>();
        groupUserRoles.add(UserRole.ROLE_GROUP_USER);
        groupUserRoles.add(UserRole.ROLE_GROUP_ADMIN);
        model.addAttribute("userRoles", groupUserRoles);

        model.addAttribute("appStates", AppState.values());

        List<ApplicationVersionPublishRequestModel> applicationVersions = new ArrayList<ApplicationVersionPublishRequestModel>();
        for (OrganizationModel adminOrganization : adminOrganizations) {
            for (ApplicationVersion applicationVersion : applicationVersionService.getAll(adminOrganization.getId(), AppState.ORG_PUBLISH_REQUEST)) {
                applicationVersions.add(createApplicationVersionPublishRequestModel(applicationVersion));
            }
        }
        model.addAttribute("applicationVersions", applicationVersions);

        return "manager/managerHomeTH";
    }

    private void populateAdminDomains(User user, List<GroupModel> adminGroups, List<OrganizationModel> adminOrganizations) {
        if (user.isSystemAdmin()) {
            for (Organization organization : organizationService.getAll()) {
                OrganizationModel model = organizationService.createOrganizationModel(organization.getId());
                if (model != null) {
                    adminOrganizations.add(model);
                }
            }
            for (Group group : groupService.getAll()) {
                GroupModel model = groupService.createGroupModel(group.getId(), true);
                if (model != null) {
                    adminGroups.add(model);
                }
            }
        } else {
            for (UserDomain userDomain : user.getUserDomains()) {
                if (DomainType.GROUP.equals(userDomain.getDomainType()) && userDomain.getRole().getAuthority().equals(UserRole.ROLE_GROUP_ADMIN.name())) {
                    GroupModel model = groupService.createGroupModel(userDomain.getDomainId(), true);
                    if (model != null) {
                        adminGroups.add(model);
                    }
                } else if (DomainType.ORGANIZATION.equals(userDomain.getDomainType()) && userDomain.getRole().getAuthority().equals(UserRole.ROLE_ORG_ADMIN.name())) {
                    OrganizationModel model = organizationService.createOrganizationModel(userDomain.getDomainId());
                    if (model != null) {
                        adminOrganizations.add(model);
                    }
                }
            }
        }
    }


    private void populatePendingRequests(Model model, List<GroupModel> adminGroups) {
        List<GroupUserRequestModel> pendingRequests = new ArrayList<GroupUserRequestModel>();
        for (GroupModel adminGroup : adminGroups) {
            List<GroupUserRequest> requests = requestService.getAll(adminGroup.getId(), Status.PENDING);
            if (requests != null) {
                for (GroupUserRequest request : requests) {
                    GroupUserRequestModel requestModel = new GroupUserRequestModel();
                    requestModel.setId(request.getId());

                    UserModel userModel = new UserModel();
                    userModel.setEmail(request.getUser().getEmail());
                    userModel.setUserName(request.getUser().getUsername());
                    userModel.setFirstName(request.getUser().getFirstName());
                    userModel.setLastName(request.getUser().getLastName());
                    userModel.setId(request.getUser().getId());
                    requestModel.setUser(userModel);

                    Group group = request.getGroup();
                    if (group != null) {
                        requestModel.setGroup(groupService.createGroupModel(group.getId(), true));
                    }

                    pendingRequests.add(requestModel);
                }
            }
        }
        model.addAttribute("pendingRequests", pendingRequests);

        if (!pendingRequests.isEmpty()) {
            model.addAttribute("hasPendingRequests", true);
        }
    }

    private ApplicationVersionPublishRequestModel createApplicationVersionPublishRequestModel(ApplicationVersion applicationVersion) {
        ApplicationVersionPublishRequestModel model = null;
        if (applicationVersion != null ) {
            model = new ApplicationVersionPublishRequestModel();
            Application application = applicationVersion.getApplication();
            if (application != null) {
                model.setApplication(applicationService.createApplicationModel(application.getId()));
            }
            model.setApplicationVersion(applicationVersionService.createApplicationVersionModel(applicationVersion.getId()));
            Organization organization = getOrganizationForApplicationVersion(applicationVersion);
            if (organization != null) {
                model.setOrganization(organizationService.createOrganizationModel(organization.getId()));
            }
        }
        return model;
    }

    private Organization getOrganizationForApplicationVersion(ApplicationVersion applicationVersion) {
        Organization organization = null;
        if (applicationVersion != null && applicationVersion.getApplication() != null && applicationVersion.getApplication().getCategory() != null) {
            organization = applicationVersion.getApplication().getCategory().getOrganization();
        }
        return organization;
    }
}
