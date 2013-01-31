package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.DomainUserRequestService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.UserDomainService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.validators.GroupValidator;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.GroupForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.DomainStatisticsModel;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GroupController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private DomainUserRequestService requestService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("groupValidator")
    @Autowired(required = true)
    private GroupValidator groupValidator;

    @InitBinder("group")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(groupValidator);
    }

    @RequestMapping(value = "/manager/viewGroups", method = RequestMethod.GET)
    public String viewGroups(Model model) {
//        User user = userService.getUserFromSecurityContext();
//        model.addAttribute("groups", userService.getGroups(user));
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

    @PreAuthorize("isDomainAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editGroup/{id}", method = RequestMethod.GET)
    public String editGroup(Model model, @PathVariable Long id) {
        checkRequiredEntity(groupService, id);

        Group existingGroup = groupService.get(id);

        if (existingGroup != null) {
            GroupForm groupForm = new GroupForm();
            groupService.mapGroupToGroupForm(existingGroup, groupForm);
            model.addAttribute("group", groupForm);
            model.addAttribute("accessCode", existingGroup.getUuid());

            model.addAttribute("applications", existingGroup.getOwnedApplications());

            List<Organization> organizations = new ArrayList<Organization>();
            organizations.add(existingGroup.getOrganization());
            model.addAttribute("organizations", organizations);

            List<DomainUserRequest> requests = requestService.getAll(existingGroup.getId(), Status.PENDING);
            List<DomainUserRequestModel> pendingRequests = new ArrayList<DomainUserRequestModel>();
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

            List<UserDomain> groupUsers = userDomainService.getAll(existingGroup.getId());
            model.addAttribute("groupUsers", groupUsers);
            model.addAttribute("domainStatistics", getDomainStatisticsModel(existingGroup));
        }

        model.addAttribute("isEdit", true);

        return "manager/manageGroupTH";
    }

    @PreAuthorize("isDomainAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteGroup", method = RequestMethod.POST)
    public @ResponseBody Result deleteGroup(@RequestParam(required = true) Long id) {
        Result result = new Result();
        try {
            checkRequiredEntity(groupService, id);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete non-existent group: %s", id));
            result.setResult(false);
            return result;
        }

        groupService.delete(id);

        result.setResult(!groupService.doesEntityExist(id));

        return result;
    }

    @PreAuthorize("isOrganizationAdmin(#groupForm.organizationId) or isDomainAdmin(#groupForm.id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadGroup", method = RequestMethod.POST)
    public String uploadGroup(Model model, @ModelAttribute("group") @Validated GroupForm groupForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return addGroup(model);
        }

        Long groupId = groupForm.getId();
        if (groupForm.getId() != null && groupForm.getId() > 0) {
            groupService.editGroup(groupForm);
        } else {
            Group group = groupService.createGroup(groupForm);
            if (group == null || group.getId() == null || group.getId() <= 0) {
                String[] codes = {"desktop.manager.group.create.error"};
                ObjectError error = new ObjectError("organizationForm", codes, null, null);
                bindingResult.addError(error);
                return addGroup(model);
            }

            groupId = group.getId();
        }

        if (groupId == null || groupId <= 0) {
            return "redirect:/manager/viewGroups";
        }
        return "redirect:/manager/editGroup/" + groupId;
    }

    @PreAuthorize("isDomainAdmin(#groupId) or hasRole('ROLE_ADMIN')")
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

    @PreAuthorize("isDomainAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getGroupsForUser", method = RequestMethod.GET)
    public @ResponseBody List<GroupModel> getGroupsForUser() {
        User user = userService.getUserFromSecurityContext();

        List<GroupModel> models = new ArrayList<GroupModel>();

        for (Group group : userService.getGroups(user)) {
            models.add(groupService.createGroupModelWithOrganization(group, false, false));
        }

        return models;
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
