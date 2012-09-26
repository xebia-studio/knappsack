package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.GroupUserRequestService;
import com.sparc.knappsack.components.services.UserDomainService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.validators.GroupValidator;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.GroupForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.DomainStatisticsModel;
import com.sparc.knappsack.models.GroupUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GroupController extends AbstractController{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private GroupUserRequestService requestService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("groupValidator")
    @Autowired(required = true)
    private GroupValidator groupValidator;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @InitBinder("group")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(groupValidator);
    }

    @RequestMapping(value = "/group/requestAccess", method = RequestMethod.GET)
    public String showRequestAccessPage() {
        return "group_accessTH";
    }

    @RequestMapping(value = "/group/requestAccess/{accessCode}", method = RequestMethod.POST)
    public String requestAccess(Model model, @PathVariable String accessCode) {
        boolean success = false;

        User user = userService.getUserFromSecurityContext();
        GroupUserRequest groupUserRequest = requestService.createGroupUserRequest(user, accessCode);

        if (groupUserRequest != null && groupUserRequest.getId() != null && groupUserRequest.getId() > 0) {
            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.GROUP_ACCESS_REQUEST);
            if (deliveryMechanism != null) {
                success = deliveryMechanism.sendNotifications(groupUserRequest);
            }
            if (!success) {
                log.info("Error sending GroupAccessRequest email.", groupUserRequest);
                requestService.delete(groupUserRequest.getId());
            }
        }

        model.addAttribute("success", success);

        return showRequestAccessPage();
    }

    @RequestMapping(value = "/manager/userRequest", method = RequestMethod.POST)
    public
    @ResponseBody
    Result userRequest(@RequestParam Long requestId, @RequestParam boolean status, @RequestParam(required = false) UserRole userRole) {
        Result result = new Result();
        result.setResult(false);

        boolean success = false;
        GroupUserRequest groupUserRequest = requestService.get(requestId);
        User user = userService.getUserFromSecurityContext();

        if (groupUserRequest != null && groupUserRequest.getGroup() != null
                && (user.isSystemAdmin()
                    || userService.isUserInGroup(user, groupUserRequest.getGroup(), UserRole.ROLE_GROUP_ADMIN)
                    || userService.isUserInOrganization(user, groupUserRequest.getGroup().getOrganization(), UserRole.ROLE_ORG_ADMIN))) {

            if (status) {
                success = requestService.acceptRequest(groupUserRequest, userRole);
            } else {
                success = requestService.declineRequest(groupUserRequest);
            }

        }

        result.setResult(success);

        return result;
    }

    @RequestMapping(value = "/manager/viewGroups", method = RequestMethod.GET)
    public String viewGroups(Model model) {
        User user = userService.getUserFromSecurityContext();
        model.addAttribute("groups", userService.getGroups(user));
        return "manager/groupsTH";
    }

    @PreAuthorize("isOrganizationAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addGroup", method = RequestMethod.GET)
    public String addGroup(Model model) {

        if (!model.containsAttribute("group")) {
            model.addAttribute("group", new GroupForm());
        }
        User user = userService.getUserFromSecurityContext();
        model.addAttribute("organizations", userService.getOrganizations(user));

        return "manager/manageGroupTH";
    }

    @PreAuthorize("isOrganizationAdminForGroup(#id) or isGroupAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editGroup/{id}", method = RequestMethod.GET)
    public String editGroup(Model model, @PathVariable Long id) {
        checkRequiredEntity(groupService, id);

        Group existingGroup = groupService.get(id);

        if (existingGroup != null) {
            GroupForm groupForm = new GroupForm();
            groupService.mapGroupToGroupForm(existingGroup, groupForm);
            model.addAttribute("group", groupForm);
            model.addAttribute("accessCode", existingGroup.getAccessCode());

            model.addAttribute("applications", existingGroup.getOwnedApplications());

            List<Organization> organizations = new ArrayList<Organization>();
            organizations.add(existingGroup.getOrganization());
            model.addAttribute("organizations", organizations);

            List<GroupUserRequest> requests = requestService.getAll(existingGroup.getId(), Status.PENDING);
            List<GroupUserRequestModel> pendingRequests = new ArrayList<GroupUserRequestModel>();
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

                    pendingRequests.add(requestModel);
                }
            }

            if (pendingRequests.size() > 0) {
                model.addAttribute("hasPendingRequests", true);
            }

            model.addAttribute("pendingRequests", pendingRequests);

            List<UserRole> groupUserRoles = new ArrayList<UserRole>();
            groupUserRoles.add(UserRole.ROLE_GROUP_USER);
            groupUserRoles.add(UserRole.ROLE_GROUP_ADMIN);
            model.addAttribute("userRoles", groupUserRoles);

            List<UserDomain> groupUsers = userDomainService.getAll(existingGroup.getId(), DomainType.GROUP);
            model.addAttribute("groupUsers", groupUsers);
            model.addAttribute("domainStatistics", getDomainStatisticsModel(existingGroup));
        }

        model.addAttribute("isEdit", true);

        return "manager/manageGroupTH";
    }

    @PreAuthorize("isOrganizationAdminForGroup(#id) or isGroupAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteGroup/{id}", method = RequestMethod.GET)
    public String deleteGroup(@PathVariable Long id) {
        checkRequiredEntity(groupService, id);
        groupService.delete(id);

        return "redirect:/manager/viewGroups";
    }

    @PreAuthorize("isOrganizationAdmin(#groupForm.organizationId) or isGroupAdmin(#groupForm.id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadGroup", method = RequestMethod.POST)
    public String uploadGroup(Model model, @ModelAttribute("group") @Valid GroupForm groupForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return addGroup(model);
        }

        if (groupForm.getId() != null && groupForm.getId() > 0) {
            groupService.editGroup(groupForm);
        } else {
            groupService.createGroup(groupForm);
        }

        return viewGroups(model);
    }

    @PreAuthorize("isOrganizationAdminForGroup(#groupId) or isGroupAdmin(#groupId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/removeUsers", method = RequestMethod.POST)
    public
    @ResponseBody
    Result removeUsers(@RequestParam Long groupId, @RequestParam(value = "userIds[]") List<Long> userIds) {
        Result result = new Result();

        if (groupId != null && groupId > 0 && userIds != null) {
            for (Long userId : userIds) {
                groupService.removeUserFromGroup(groupId, userId);
            }
            result.setResult(true);
        } else {
            result.setResult(false);
        }

        return result;
    }

    private DomainStatisticsModel getDomainStatisticsModel(Group group) {
        DomainStatisticsModel domainStatisticsModel = new DomainStatisticsModel();
        domainStatisticsModel.setTotalApplications(groupService.getTotalApplications(group));
        domainStatisticsModel.setTotalApplicationVersions(groupService.getTotalApplicationVersions(group));
        domainStatisticsModel.setTotalUsers(groupService.getTotalUsers(group));
        domainStatisticsModel.setTotalMegabyteStorageAmount(groupService.getTotalMegabyteStorageAmount(group));

        return domainStatisticsModel;
    }

}
