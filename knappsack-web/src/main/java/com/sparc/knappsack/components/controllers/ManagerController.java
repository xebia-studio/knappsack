package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.SortOrder;
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
    private DomainUserRequestService requestService;

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

    @Autowired(required = true)
    private DomainRequestService domainRequestService;

    @RequestMapping(value = "/manager", method = RequestMethod.GET)
    public String manageApps(Model model) {

        User user = userService.getUserFromSecurityContext();

        List<Organization> organizations = userService.getAdministeredOrganizations(user, SortOrder.ASCENDING);
        List<Group> groups = userService.getAdministeredGroups(user, SortOrder.ASCENDING);
        List<GroupModel> groupModels = new ArrayList<GroupModel>();
        for (Group group : groups) {
            groupModels.add(groupService.createGroupModelWithOrganization(group, false, false));
        }

        model.addAttribute("adminGroups", groupModels);

        populatePendingDomainRequests(model, organizations, groups);
        populatePendingDomainUserRequests(model, groups);

        List<UserRole> groupUserRoles = new ArrayList<UserRole>();
        groupUserRoles.add(UserRole.ROLE_GROUP_USER);
        groupUserRoles.add(UserRole.ROLE_GROUP_ADMIN);
        model.addAttribute("userRoles", groupUserRoles);

        model.addAttribute("appStates", AppState.values());

        List<ApplicationVersionPublishRequestModel> applicationVersions = new ArrayList<ApplicationVersionPublishRequestModel>();
        for (Organization adminOrganization : organizations) {
            for (ApplicationVersion applicationVersion : applicationVersionService.getAll(adminOrganization.getId(), AppState.ORG_PUBLISH_REQUEST)) {
                applicationVersions.add(createApplicationVersionPublishRequestModel(applicationVersion));
            }
        }
        model.addAttribute("applicationVersions", applicationVersions);

        return "manager/managerHomeTH";
    }

//    private void populateAdminDomains(User user, List<GroupModel> adminGroupModels, List<OrganizationModel> adminOrganizationModels) {
////        if (user.isSystemAdmin()) {
////            for (Organization organization : organizationService.getAllByOrganizations()) {
////                OrganizationModel model = organizationService.createOrganizationModel(organization, false);
////                if (model != null) {
////                    adminOrganizationModels.add(model);
////                }
////                for (Group group : organization.getGroups()) {
////                    GroupModel groupModel = groupService.createGroupModel(group, true, false);
////                    if (model != null) {
////                        adminGroupModels.add(groupModel);
////                    }
////                }
////            }
////        } else {
//            Set<Organization> adminOrganizations = new HashSet<Organization>();
//            Set<Group> adminGroups = new HashSet<Group>();
//            for (UserDomain userDomain : user.getUserDomains()) {
//                if (DomainType.GROUP.equals(userDomain.getDomain().getDomainType()) && userDomain.getRole().getAuthority().equals(UserRole.ROLE_GROUP_ADMIN.name())) {
//                    adminGroups.add((Group) userDomain.getDomain());
//                } else if (DomainType.ORGANIZATION.equals(userDomain.getDomain().getDomainType()) && userDomain.getRole().getAuthority().equals(UserRole.ROLE_ORG_ADMIN.name())) {
//                    adminOrganizations.add((Organization) userDomain.getDomain());
//                    adminGroups.addAll(((Organization) userDomain.getDomain()).getGroups());
//                }
//            }
//
//            for (Group group : adminGroups) {
//                GroupModel model = groupService.createGroupModel(group, true, false);
//                if (model != null) {
//                    adminGroupModels.add(model);
//                }
//            }
//
//            for (Organization organization : adminOrganizations) {
//                OrganizationModel model = organizationService.createOrganizationModel(organization, false);
//                if (model != null) {
//                    adminOrganizationModels.add(model);
//                }
//            }
////        }
//    }

    private void populatePendingDomainRequests(Model model, List<Organization> adminOrganizations, List<Group> adminGroups) {
        List<DomainRequestSummaryModel> domainRequestSummaryModels = new ArrayList<DomainRequestSummaryModel>();
        for (Organization adminOrganization : adminOrganizations) {
            long count = domainRequestService.countAll(adminOrganization.getId());
            if(count > 0) {
                DomainRequestSummaryModel domainRequestSummaryModel = new DomainRequestSummaryModel();
                domainRequestSummaryModel.setDomainId(adminOrganization.getId());
                domainRequestSummaryModel.setDomainName(adminOrganization.getName());
                domainRequestSummaryModel.setRequestAmount(count);
                domainRequestSummaryModels.add(domainRequestSummaryModel);
            }
        }

        for (Group adminGroup : adminGroups) {
            long count = domainRequestService.countAll(adminGroup.getId());
            if(count > 0) {
                DomainRequestSummaryModel domainRequestSummaryModel = new DomainRequestSummaryModel();
                domainRequestSummaryModel.setDomainId(adminGroup.getId());
                domainRequestSummaryModel.setDomainName(adminGroup.getName());
                domainRequestSummaryModel.setRequestAmount(count);
                domainRequestSummaryModels.add(domainRequestSummaryModel);
            }
        }

        model.addAttribute("domainRequestSummaryModels", domainRequestSummaryModels);
    }

    private void populatePendingDomainUserRequests(Model model, List<Group> adminGroups) {
        List<DomainUserRequestModel> pendingRequests = new ArrayList<DomainUserRequestModel>();
        for (Group adminGroup : adminGroups) {
            List<DomainUserRequest> requests = requestService.getAll(adminGroup, Status.PENDING);
            if (requests != null) {
                for (DomainUserRequest request : requests) {
                    DomainUserRequestModel requestModel = new DomainUserRequestModel();
                    requestModel.setId(request.getId());

                    UserModel userModel = new UserModel();
                    userModel.setEmail(request.getUser().getEmail());
                    userModel.setUserName(request.getUser().getUsername());
                    userModel.setFirstName(request.getUser().getFirstName());
                    userModel.setLastName(request.getUser().getLastName());
                    userModel.setId(request.getUser().getId());
                    requestModel.setUser(userModel);
                    requestModel.setDomain(groupService.createGroupModelWithOrganization(adminGroup, false, false));

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
                model.setApplication(applicationService.createApplicationModel(application, false));
                Group group = application.getOwnedGroup();
                model.setGroupId(group.getId());
                model.setGroupName(group.getName());
            }
            model.setApplicationVersion(applicationVersionService.createApplicationVersionModel(applicationVersion, false));
            Organization organization = getOrganizationForApplicationVersion(applicationVersion);
            if (organization != null) {
                model.setOrganization(organizationService.createOrganizationModel(organization, false));
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
