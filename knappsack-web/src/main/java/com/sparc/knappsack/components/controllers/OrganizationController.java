package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.components.validators.OrganizationValidator;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.OrganizationForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.DomainStatisticsModel;
import com.sparc.knappsack.models.InternationalizedObject;
import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrganizationController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("storageConfigurationService")
    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private CategoryService categoryService;

    @Qualifier("bandwidthService")
    @Autowired(required = true)
    private BandwidthService bandwidthService;

    @Qualifier("organizationValidator")
    @Autowired
    private OrganizationValidator organizationValidator;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("messageSource")
    @Autowired(required = true)
    private MessageSource messageSource;

    @InitBinder("organization")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(organizationValidator);
        binder.setBindEmptyMultipartFiles(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true));
    }

    @PreAuthorize("isOrganizationAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/viewOrgs", method = RequestMethod.GET)
    public String viewOrgs() {
        return "manager/organizationsTH";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addOrg", method = RequestMethod.GET)
    public String addOrganization(Model model) {

        if(!model.containsAttribute("organization")) {
            model.addAttribute("organization", new OrganizationForm());
        }
        model.addAttribute("storageConfigurations", storageConfigurationService.getAll());

        return "manager/manageOrganizationTH";
    }

    @PreAuthorize("isOrganizationAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editOrg/{id}", method = RequestMethod.GET)
    public String editOrganization(HttpServletRequest request, Model model, @PathVariable Long id)  {
        checkRequiredEntity(organizationService, id);
        Organization existingOrg = organizationService.get(id);

        if (existingOrg != null) {
            if (!model.containsAttribute("organization")) {
                OrganizationForm orgForm = new OrganizationForm();
                orgForm.setEditing(true);
                orgForm.setName(existingOrg.getName());
                orgForm.setId(existingOrg.getId());
                orgForm.setStorageConfigurationId(existingOrg.getOrgStorageConfig().getStorageConfigurations().get(0).getId());
                orgForm.setStoragePrefix(existingOrg.getOrgStorageConfig().getPrefix());

                if (existingOrg.getCustomBranding() != null) {
                    AppFile logo = existingOrg.getCustomBranding().getLogo();
                    if (logo != null) {
                        MockMultipartFile logoMultipartFile = new MockMultipartFile(logo.getName(), logo.getName(), logo.getType(), new byte[0]);
                        orgForm.setLogo(logoMultipartFile);
                    }
                    orgForm.setEmailHeader(existingOrg.getCustomBranding().getEmailHeader());
                    orgForm.setEmailFooter(existingOrg.getCustomBranding().getEmailFooter());
                    orgForm.setSubdomain(existingOrg.getCustomBranding().getSubdomain());
                }

                model.addAttribute("organization", orgForm);
            } else {
                ((OrganizationForm) model.asMap().get("organization")).setStorageConfigurationId(existingOrg.getOrgStorageConfig().getStorageConfigurations().get(0).getId());
            }
            model.addAttribute("originalName", existingOrg.getName());
            model.addAttribute("categories", existingOrg.getCategories());
            model.addAttribute("appStates", AppState.values());
//            List<ApplicationVersion> applicationVersions = applicationVersionService.getAll(existingOrg.getId());
//            model.addAttribute("applicationVersions", applicationVersions);
//            boolean hasApplicationRequests = false;
//            for (ApplicationVersion applicationVersion : applicationVersions) {
//                if(AppState.ORG_PUBLISH_REQUEST.equals(applicationVersion.getAppState())) {
//                    hasApplicationRequests = true;
//                    break;
//                }
//            }
//            model.addAttribute("hasApplicationRequests", hasApplicationRequests);

            model.addAttribute("domainStatistics", getDomainStatisticsModel(existingOrg));

            List<InternationalizedObject> userRoles = new ArrayList<InternationalizedObject>();
            for (UserRole userRole : UserRole.getAllSelectableForDomainType(DomainType.ORGANIZATION)) {
                try {
                    userRoles.add(new InternationalizedObject(userRole, messageSource.getMessage(userRole.getMessageKey(), null, request.getLocale())));
                } catch (NoSuchMessageException ex) {
                    log.error(String.format("No message for userRole: %s", userRole.name()), ex);

                    // Put the userRole name so that the application doesn't error out.
                    userRoles.add(new InternationalizedObject(userRole, userRole.name()));
                }
            }
            model.addAttribute("userRoles", userRoles);

//            List<InternationalizedObject> applicationTypes = new ArrayList<InternationalizedObject>();
//            for (ApplicationType applicationType : ApplicationType.values()) {
//                try {
//                    applicationTypes.add(new InternationalizedObject(applicationType, messageSource.getMessage(applicationType.getMessageKey(), null, request.getLocale())));
//                } catch (NoSuchMessageException ex) {
//                    log.error(String.format("No message for applicationType: %s", applicationType.name()), ex);
//
//                    // Put the userRole name so that the application doesn't error out.
//                    applicationTypes.add(new InternationalizedObject(applicationType, applicationType.name()));
//                }
//            }
//            model.addAttribute("applicationTypes", applicationTypes);
//
//            List<InternationalizedObject> appStates = new ArrayList<InternationalizedObject>();
//            for (AppState appState : AppState.values()) {
//                try {
//                    appStates.add(new InternationalizedObject(appState, messageSource.getMessage(appState.getMessageKey(), null, request.getLocale())));
//                } catch (NoSuchMessageException ex) {
//                    log.error(String.format("No message for appState: %s", appState.name()), ex);
//
//                    // Put the userRole name so that the application doesn't error out.
//                    appStates.add(new InternationalizedObject(appState, appState.name()));
//                }
//            }
//            model.addAttribute("appStates", appStates);

            model.addAttribute("storageConfigurations", storageConfigurationService.getAll());
            model.addAttribute("isEdit", true);

            model.addAttribute("customBrandingEnabled", organizationService.isCustomBrandingEnabled(existingOrg));
            return "manager/manageOrganizationTH";
        }
        throw new EntityNotFoundException(String.format("Organization not found: %s", id));
    }

    //TODO: Make it so OrgAdmins can also delete
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteOrg", method = RequestMethod.POST)
    public @ResponseBody Result deleteOrganization(@RequestParam(required = true) Long id) {
        Result result = new Result();
        try {
            checkRequiredEntity(organizationService, id);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete non-existent organization: %s", id));
            result.setResult(false);
            return result;
        }

        organizationService.delete(id);

        result.setResult(!organizationService.doesEntityExist(id));

        return result;
    }

    @PreAuthorize("isOrganizationAdmin(#organizationForm.id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadOrg", method = RequestMethod.POST)
    public String uploadOrganization(HttpServletRequest request, Model model, @ModelAttribute("organization") @Validated OrganizationForm organizationForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            if(!model.containsAttribute("organization")) {
                model.addAttribute("organization", new OrganizationForm());
            }
            model.addAttribute("storageConfigurations", storageConfigurationService.getAll());
            model.addAttribute("organization", organizationForm);
            if (organizationForm.isEditing()) {
                return editOrganization(request, model, organizationForm.getId());
            } else {
                return addOrganization(model);
            }
        }

//        OrganizationModel organizationModel = new OrganizationModel();
//        organizationModel.setId(organizationForm.getId());
//        organizationModel.setName(organizationForm.getName());
//        organizationModel.setStorageConfigurationId(organizationForm.getStorageConfigurationId());
//        organizationModel.setStoragePrefix(organizationForm.getStoragePrefix());

        Long orgId = organizationForm.getId();
        if(organizationForm.getId() != null && organizationForm.getId() > 0) {
            organizationService.editOrganization(organizationForm);
        } else {
            //Only create organization if user is System Admin
            User user = userService.getUserFromSecurityContext();
            if (!user.isSystemAdmin()) {
                String[] codes = {"desktop.manager.organization.generic.error"};
                ObjectError error = new ObjectError("organizationForm", codes, null, null);
                bindingResult.addError(error);
                return addOrganization(model);
            }

            Organization organization = organizationService.createOrganization(organizationForm);
            if (organization == null || organization.getId() == null || organization.getId() <= 0) {
                String[] codes = {"desktop.manager.organization.create.error"};
                ObjectError error = new ObjectError("organizationForm", codes, null, null);
                bindingResult.addError(error);
                return addOrganization(model);
            }
            categoryService.createDefaultCategories(organization.getId());
            orgId = organization.getId();
        }

        if (orgId == null || orgId <= 0) {
            return "redirect:/manager/viewOrgs";
        }
        return "redirect:/manager/editOrg/" + orgId;
    }

    @PreAuthorize("isOrganizationAdmin(#orgId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/updateOrgMemberRole", method = RequestMethod.GET)
    public @ResponseBody
    Result updateOrgMemberRole(@RequestParam Long orgId, @RequestParam Long userId, @RequestParam UserRole userRole) {
        Result result = new Result();
        try {
            checkRequiredEntity(organizationService, orgId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to update organization member role for non-existent organization: %s", orgId));
            result.setResult(false);
            return result;
        }
        try {
            checkRequiredEntity(userService, userId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to update organization member role for non-existent user: %s", userId));
            result.setResult(false);
            return result;
        }

        User user = userService.getUserFromSecurityContext();
        if (user != null && !userId.equals(user.getId())) {
            userDomainService.updateUserDomainRole(userId, orgId, userRole);
            result.setResult(true);
        } else {
            result.setResult(false);
        }

        return result;
    }

    @PreAuthorize("isOrganizationAdmin(#organizationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/removeOrganizationUsers", method = RequestMethod.POST)
    public
    @ResponseBody
    Result removeUsers(@RequestParam Long organizationId, @RequestParam(value = "userIds[]") List<Long> userIds) {
        Result result = new Result();
        List<Long> userIdsRemoved = new ArrayList<Long>();

        try {
            checkRequiredEntity(organizationService, organizationId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to update member role for non-existent organization: %s", organizationId));
            result.setResult(false);
            return result;
        }

        User user = userService.getUserFromSecurityContext();

        if (organizationId != null && organizationId > 0 && userIds != null) {
            for (Long userId : userIds) {
                if (user != null && !userId.equals(user.getId())) {
                    organizationService.removeUserFromOrganization(organizationId, userId);
                    userIdsRemoved.add(userId);
                }
            }
        }

        if (userIdsRemoved.size() != userIds.size()) {
            result.setResult(false);
        } else {
            result.setResult(true);
        }
        result.setValue(userIdsRemoved);

        return result;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/organizationList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<OrganizationModel> getOrganizationsForRange(@RequestParam(required = false) String minDate, @RequestParam(required = false) String maxDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date tmpMinDate = null;
        Date tmpMaxDate = null;
        if (StringUtils.hasText(minDate)) {
            try {
                tmpMinDate = sdf.parse(minDate);
            } catch (ParseException e) {
                log.error(String.format("Error parsing to date: %s", minDate), e);
            }
        }
        if (StringUtils.hasText(maxDate)) {
            try {
                tmpMaxDate = sdf.parse(maxDate);
            } catch (ParseException e) {
                log.error(String.format("Error parsing to date: %s", maxDate), e);
            }
        }

        return organizationService.getAllOrganizationsForCreateDateRange(tmpMinDate, tmpMaxDate);
    }

    @PreAuthorize("isOrganizationAdmin(#orgId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getOrganizationAdmins", method = RequestMethod.GET)
    public @ResponseBody List<UserDomainModel> getOrganizationAdmins(@RequestParam(required = true) Long orgId) {
        return organizationService.getAllOrganizationAdmins(orgId);
    }

    @PreAuthorize("isOrganizationAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getOrganizationsForUser", method = RequestMethod.GET)
    public @ResponseBody List<OrganizationModel> getOrganizationsForUser() {
        User user = userService.getUserFromSecurityContext();

        return organizationService.createOrganizationModelsWithoutStorageConfiguration(userService.getAdministeredOrganizations(user, SortOrder.ASCENDING), false, SortOrder.ASCENDING);
    }

    @PreAuthorize("isOrganizationAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteOrgLogo", method = RequestMethod.POST)
    public @ResponseBody Result deleteOrganizationLogo(@RequestParam(required = true) Long id) {
        Result result = new Result();

        organizationService.deleteLogo(id);

        result.setResult(true);

        return result;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/activeOrganizations", method = RequestMethod.GET)
    public @ResponseBody List<OrganizationModel> getOrganizations() {
        User user = userService.getUserFromSecurityContext();

        return organizationService.createOrganizationModelsWithoutStorageConfiguration(userService.getOrganizations(user, SortOrder.ASCENDING), false, SortOrder.ASCENDING);
    }

    @PreAuthorize("isUserInOrganization(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/activeOrganization/{id}")
    public String setActiveOrganization(@PathVariable Long id) {
        User user = userService.getUserFromSecurityContext();
        user.setActiveOrganization(organizationService.get(id));
        userService.update(user);

        return "redirect:/home";
    }

    @PreAuthorize("isOrganizationAdminForActiveOrganization() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getUsersForActiveOrganization", method = RequestMethod.GET)
    public @ResponseBody List<UserDomainModel> getUsersForActiveOrganization(@RequestParam(required = true) boolean includeGuests) {
        List<UserDomainModel> userDomainModels = new ArrayList<UserDomainModel>();
        User user = userService.getUserFromSecurityContext();

        if (user != null && user.getActiveOrganization() != null) {
            userDomainModels.addAll(organizationService.getAllOrganizationMembers(user.getActiveOrganization().getId(), includeGuests));
        }

        return userDomainModels;
    }

    @PreAuthorize("isOrganizationAdminForActiveOrganization() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getGuestsForActiveOrganization", method = RequestMethod.GET)
    public @ResponseBody List<UserDomainModel> getGuestsForActiveOrganization() {
        List<UserDomainModel> userDomainModels = new ArrayList<UserDomainModel>();
        User user = userService.getUserFromSecurityContext();

        if (user != null && user.getActiveOrganization() != null) {
            userDomainModels.addAll(organizationService.getAllOrganizationGuests(user.getActiveOrganization()));
        }

        return userDomainModels;
    }

//    @PreAuthorize("isOrganizationAdminForActiveOrganization() or hasRole('ROLE_ADMIN')")
//    @RequestMapping(value = "/manager/getApplicationsForActiveOrganization", method = RequestMethod.GET)
//    public @ResponseBody List<ApplicationVersionModel> getAllApplicationVersionsForActiveOrganization(@RequestParam(required = true) boolean includeAppFiles) {
//        List<ApplicationVersionModel> applicationVersionModels = new ArrayList<ApplicationVersionModel>();
//        User user = userService.getUserFromSecurityContext();
//
//        if (user != null && user.getActiveOrganization() != null) {
//            List<ApplicationVersion> applicationVersions = applicationVersionService.getAll(user.getActiveOrganization().getId());
//            if (applicationVersions != null) {
//                for (ApplicationVersion applicationVersion : applicationVersions) {
//                    ApplicationVersionModel model = applicationVersionService.createApplicationVersionModel(applicationVersion, includeInstallFile);
//                    if (model != null) {
//                        applicationVersionModels.add(model);
//                    }
//                }
//            }
//        }
//        return applicationVersionModels;
//    }

    private DomainStatisticsModel getDomainStatisticsModel(Organization organization) {
        DomainStatisticsModel domainStatisticsModel = new DomainStatisticsModel();
        domainStatisticsModel.setTotalApplications(organization != null ? organizationService.countOrganizationApps(organization.getId()) : 0);
        domainStatisticsModel.setTotalApplicationVersions(organization != null ? organizationService.countOrganizationAppVersions(organization.getId()) : 0);
        domainStatisticsModel.setTotalUsers(organization != null ? organizationService.countOrganizationUsers(organization.getId(), true) : 0);
        domainStatisticsModel.setTotalPendingInvitations(organization != null ? invitationService.countAllForOrganizationIncludingGroups(organization.getId()) : 0);
        domainStatisticsModel.setTotalMegabyteStorageAmount(organizationService.getTotalMegabyteStorageAmount(organization));
        domainStatisticsModel.setTotalMegabyteBandwidthUsed(organization != null ? bandwidthService.getMegabyteBandwidthUsed(organization.getId()) : 0);

        return domainStatisticsModel;
    }

}
