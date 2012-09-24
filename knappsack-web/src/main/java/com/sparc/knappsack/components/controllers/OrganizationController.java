package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.components.validators.OrganizationValidator;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.OrganizationForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.DomainStatisticsModel;
import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;
import com.sparc.knappsack.util.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrganizationController extends AbstractController {

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("storageConfigurationService")
    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("organizationValidator")
    @Autowired
    private OrganizationValidator organizationValidator;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @InitBinder("organizationForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(organizationValidator);
    }

    @PreAuthorize("isOrganizationAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/viewOrgs", method = RequestMethod.GET)
    public String viewOrgs(Model model) {
        User user = userService.getUserFromSecurityContext();

        model.addAttribute("orgs", userService.getOrganizations(user));
        return "manager/organizationsTH";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addOrg", method = RequestMethod.GET)
    public String addOrganization(Model model) {

        if(!model.containsAttribute("org")) {
            model.addAttribute("org", new OrganizationForm());
        }
        model.addAttribute("storageConfigurations", storageConfigurationService.getAll());

        return "manager/manageOrganizationTH";
    }

    @PreAuthorize("isOrganizationAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editOrg/{id}", method = RequestMethod.GET)
    public String editOrganization(Model model, @PathVariable Long id) {

        Organization existingOrg = organizationService.get(id);

        if (existingOrg != null) {
            if (!model.containsAttribute("org")) {
                OrganizationForm orgForm = new OrganizationForm();
                orgForm.setEditing(true);
                orgForm.setName(existingOrg.getName());
                orgForm.setId(existingOrg.getId());
                orgForm.setStorageConfigurationId(existingOrg.getOrgStorageConfig().getStorageConfigurations().get(0).getId());
                orgForm.setStoragePrefix(existingOrg.getOrgStorageConfig().getPrefix());

                model.addAttribute("org", orgForm);
            } else {
                ((OrganizationForm) model.asMap().get("org")).setStorageConfigurationId(existingOrg.getOrgStorageConfig().getStorageConfigurations().get(0).getId());
            }
            model.addAttribute("originalName", existingOrg.getName());
            model.addAttribute("categories", existingOrg.getCategories());
            model.addAttribute("appStates", AppState.values());
            List<ApplicationVersion> applicationVersions = applicationVersionService.getAll(existingOrg.getId());
            model.addAttribute("applicationVersions", applicationVersions);
            boolean hasApplicationRequests = false;
            for (ApplicationVersion applicationVersion : applicationVersions) {
                if(AppState.ORG_PUBLISH_REQUEST.equals(applicationVersion.getAppState())) {
                    hasApplicationRequests = true;
                    break;
                }
            }
            model.addAttribute("hasApplicationRequests", hasApplicationRequests);

            List<UserDomainModel> organizationMembers = organizationService.getAllOrganizationMembers(id, false);
            model.addAttribute("organizationMembers", organizationMembers);
            List<UserDomainModel> organizationGuests = organizationService.getAllOrganizationGuests(id);
            model.addAttribute("organizationGuests", organizationGuests);

            model.addAttribute("domainStatistics", getDomainStatisticsModel(existingOrg));
        }

        List<UserRole> userRoles = new ArrayList<UserRole>();
        userRoles.add(UserRole.ROLE_ORG_ADMIN);
        userRoles.add(UserRole.ROLE_ORG_USER);
        model.addAttribute("userRoles", userRoles);


        model.addAttribute("storageConfigurations", storageConfigurationService.getAll());
        model.addAttribute("isEdit", true);

        return "manager/manageOrganizationTH";
    }

    //TODO: Make it so OrgAdmins can also delete
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteOrg/{id}", method = RequestMethod.GET)
    public String deleteOrganization(Model model, @PathVariable Long id) {
        organizationService.delete(id);

        return addOrganization(model);
    }

    @PreAuthorize("isOrganizationAdmin(#organizationForm.id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadOrg", method = RequestMethod.POST)
    public String uploadOrganization(Model model, @ModelAttribute("org") @Valid OrganizationForm organizationForm, BindingResult bindingResult, WebRequest webRequest) {

        if (bindingResult.hasErrors()) {
            if(!model.containsAttribute("org")) {
                model.addAttribute("org", new OrganizationForm());
            }
            model.addAttribute("storageConfigurations", storageConfigurationService.getAll());
            model.addAttribute("org", organizationForm);
            if (organizationForm.isEditing()) {
                return editOrganization(model, organizationForm.getId());
            } else {
                return addOrganization(model);
            }
        }

        OrganizationModel organizationModel = new OrganizationModel();
        organizationModel.setId(organizationForm.getId());
        organizationModel.setName(organizationForm.getName());
        organizationModel.setStorageConfigurationId(organizationForm.getStorageConfigurationId());
        organizationModel.setStoragePrefix(organizationForm.getStoragePrefix());

        if(organizationForm.getId() != null && organizationForm.getId() > 0) {
            organizationService.editOrganization(organizationModel);
        } else {
            //Only create organization if user is System Admin
            User user = userService.getUserFromSecurityContext();
            if (!user.isSystemAdmin()) {
                String[] codes = {"desktop.manager.organization.generic.error"};
                ObjectError error = new ObjectError("organizationForm", codes, null, null);
                bindingResult.addError(error);
                return addOrganization(model);
            }

            Organization organization = organizationService.createOrganization(organizationModel);
            if (organization == null || organization.getId() == null || organization.getId() <= 0) {
                String[] codes = {"desktop.manager.organization.create.error"};
                ObjectError error = new ObjectError("organizationForm", codes, null, null);
                bindingResult.addError(error);
                return addOrganization(model);
            }
        }

        return viewOrgs(model);
    }

    @PreAuthorize("isOrganizationAdmin(#orgId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/updateOrgMemberRole", method = RequestMethod.GET)
    public @ResponseBody
    Result updateOrgMemberRole(@RequestParam Long orgId, @RequestParam Long userId, @RequestParam UserRole userRole) {
        Result result = new Result();
        result.setResult(true);

        userDomainService.updateUserDomainRole(userId, orgId, DomainType.ORGANIZATION, userRole);

        return result;
    }

    @PreAuthorize("isOrganizationAdmin(#organizationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/removeOrganizationUsers", method = RequestMethod.POST)
    public
    @ResponseBody
    Result removeUsers(@RequestParam Long organizationId, @RequestParam(value = "userIds[]") List<Long> userIds) {
        Result result = new Result();

        if (organizationId != null && organizationId > 0 && userIds != null) {
            for (Long userId : userIds) {
                organizationService.removeUserFromOrganization(organizationId, userId);
            }
            result.setResult(true);
        } else {
            result.setResult(false);
        }
        result.setResult(true);

        return result;
    }

    private DomainStatisticsModel getDomainStatisticsModel(Organization organization) {
        DomainStatisticsModel domainStatisticsModel = new DomainStatisticsModel();
        domainStatisticsModel.setTotalApplications(organizationService.getTotalApplications(organization));
        domainStatisticsModel.setTotalApplicationVersions(organizationService.getTotalApplicationVersions(organization));
        domainStatisticsModel.setTotalUsers(organizationService.getTotalUsers(organization));
        domainStatisticsModel.setTotalMegabyteStorageAmount(organizationService.getTotalMegabyteStorageAmount(organization));

        return domainStatisticsModel;
    }

}
