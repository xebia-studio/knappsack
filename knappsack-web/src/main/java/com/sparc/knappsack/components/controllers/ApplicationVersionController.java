package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.ApplicationVersionControllerService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.components.services.KeyVaultEntryService;
import com.sparc.knappsack.components.validators.ApplicationVersionValidator;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.exceptions.ApplicationVersionResignException;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.InternationalizedObject;
import com.sparc.knappsack.models.KeyVaultEntryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class ApplicationVersionController extends AbstractController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("applicationVersionValidator")
    @Autowired(required = true)
    private ApplicationVersionValidator applicationVersionValidator;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationVersionControllerService")
    @Autowired(required = true)
    private ApplicationVersionControllerService applicationVersionControllerService;

    @Qualifier("keyVaultEntryService")
    @Autowired(required = true)
    private KeyVaultEntryService keyVaultEntryService;

    @Qualifier("messageSource")
    @Autowired(required = true)
    private MessageSource messageSource;

    @InitBinder("version")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(applicationVersionValidator);
        binder.setBindEmptyMultipartFiles(false);
    }

    @PreAuthorize("canEditApplicationVersion(#appVersionId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/updateAppVersionState", method = RequestMethod.GET)
    public
    @ResponseBody
    Result updateAppState(@RequestParam Long appVersionId, @RequestParam String appState) {
        Result result = new Result();
        try {
            checkRequiredEntity(applicationVersionService, appVersionId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to update state for non-existent application version: %s", appVersionId));
            result.setResult(false);
            return result;
        }
        boolean success = applicationVersionControllerService.updateApplicationVersionState(appVersionId, AppState.valueOf(appState), true);
        result.setResult(success);
        return result;
    }


    @PreAuthorize("canEditApplication(#parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addVersion/{parentId}", method = RequestMethod.GET)
    public String addApplicationVersion(final HttpServletRequest request, Model model, @PathVariable Long parentId) {

        checkRequiredEntity(applicationService, parentId);

        Application application = applicationService.get(parentId);

        Group group = application.getOwnedGroup();

        model.addAttribute("parentApplicationId", application.getId());
        model.addAttribute("parentApplicationName", application.getName());

        if (!model.containsAttribute("version")) {
            ApplicationVersionForm version = new ApplicationVersionForm();
            version.setParentId(parentId);
            version.setEditing(false);

            model.addAttribute("version", version);
        }

        model.addAttribute("currentGuestGroupIds", new ArrayList<Group>());
        List<Group> groups = new ArrayList<Group>(group.getOrganization().getGroups());
        groups.remove(group);
        model.addAttribute("groups", groups);
        model.addAttribute("isEdit", false);

        // Create a List of all KeyVaultEntries which are available for the given application.
        List<KeyVaultEntryModel> keyVaultEntryModels = new ArrayList<KeyVaultEntryModel>();
        for (KeyVaultEntry keyVaultEntry : keyVaultEntryService.getAllForDomainAndApplicationType(group, application.getApplicationType())) {
            KeyVaultEntryModel keyVaultEntryModel = keyVaultEntryService.convertToModel(keyVaultEntry);
            if (keyVaultEntry != null) {
                keyVaultEntryModels.add(keyVaultEntryModel);
            }
        }
        model.addAttribute("keyVaultEntries", keyVaultEntryModels);

        List<InternationalizedObject> appStates = new ArrayList<InternationalizedObject>();
        for (AppState appState : AppState.values()) {
            try {
                appStates.add(new InternationalizedObject(appState, messageSource.getMessage(appState.getMessageKey(), null, request.getLocale())));
            } catch (NoSuchMessageException ex) {
                log.error(String.format("No message for appState: %s", appState.name()), ex);

                // Put the applicationType name so that the application doesn't error out.
                appStates.add(new InternationalizedObject(appState, appState.name()));
            }
        }
        model.addAttribute("appStates", appStates);

        return "manager/manageApplicationVersionTH";
    }

    @PreAuthorize("canEditApplication(#parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editVersion/{parentId}/{versionId}", method = RequestMethod.GET)
    public String editApplicationVersion(final HttpServletRequest request, Model model, @PathVariable Long parentId, @PathVariable Long versionId) {
        checkRequiredEntity(applicationVersionService, versionId);
        checkRequiredEntity(applicationService, parentId);

        ApplicationVersion version = applicationVersionService.get(versionId);
        Application application = applicationService.get(parentId);
        Group group = application.getOwnedGroup();
        if (group != null) {
            model.addAttribute("parentApplicationId", application.getId());
            model.addAttribute("parentApplicationName", application.getName());

            ApplicationVersionForm applicationVersionForm;
            if (model.containsAttribute("version")) {
                applicationVersionForm = (ApplicationVersionForm) model.asMap().get("version");
            } else {
                applicationVersionForm = new ApplicationVersionForm();
            }

            applicationVersionForm.setId(version.getId());
            applicationVersionForm.setRecentChanges(version.getRecentChanges());
            applicationVersionForm.setVersionName(version.getVersionName());
            applicationVersionForm.setParentId(parentId);
            applicationVersionForm.setAppState(version.getAppState());

            if (version.getInstallationFile() != null) {
                MockMultipartFile multipartFile = new MockMultipartFile(version.getInstallationFile().getName(), version.getInstallationFile().getName(), version.getInstallationFile().getType(), new byte[0]);
                applicationVersionForm.setAppFile(multipartFile);
            }

            if (version.getProvisioningProfile() != null) {
                MockMultipartFile multipartFile = new MockMultipartFile(version.getProvisioningProfile().getName(), version.getProvisioningProfile().getName(), version.getProvisioningProfile().getType(), new byte[0]);
                applicationVersionForm.setProvisioningProfile(multipartFile);
            }

            model.addAttribute("version", applicationVersionForm);

            //Put this app versions guest groups on the model so we can highlight them in the multi-select box
            List<Group> currentGuestGroups = version.getGuestGroups();
            Set<Long> currentGuestGroupIds = new HashSet<Long>();
            if (currentGuestGroups != null) {
                for (Group guestGroup : currentGuestGroups) {
                    currentGuestGroupIds.add(guestGroup.getId());
                }
            }
            applicationVersionForm.setGuestGroupIds(new ArrayList<Long>(currentGuestGroupIds));
            model.addAttribute("currentGuestGroupIds", currentGuestGroupIds);
            //We don't want the group that owns the application in the list of guest groups
            List<Group> groups = new ArrayList<Group>(group.getOrganization().getGroups());
            groups.remove(group);
            model.addAttribute("groups", groups);
            model.addAttribute("isEdit", true);

            // Create a List of all KeyVaultEntries which are available for the given application.
            List<KeyVaultEntryModel> keyVaultEntryModels = new ArrayList<KeyVaultEntryModel>();
            for (KeyVaultEntry keyVaultEntry : keyVaultEntryService.getAllForDomainAndApplicationType(group, application.getApplicationType())) {
                KeyVaultEntryModel keyVaultEntryModel = keyVaultEntryService.convertToModel(keyVaultEntry);
                if (keyVaultEntry != null) {
                    keyVaultEntryModels.add(keyVaultEntryModel);
                }
            }
            model.addAttribute("keyVaultEntries", keyVaultEntryModels);

            List<InternationalizedObject> appStates = new ArrayList<InternationalizedObject>();
            for (AppState appState : AppState.values()) {
                try {
                    appStates.add(new InternationalizedObject(appState, messageSource.getMessage(appState.getMessageKey(), null, request.getLocale())));
                } catch (NoSuchMessageException ex) {
                    log.error(String.format("No message for appState: %s", appState.name()), ex);

                    // Put the applicationType name so that the application doesn't error out.
                    appStates.add(new InternationalizedObject(appState, appState.name()));
                }
            }
            model.addAttribute("appStates", appStates);

            return "manager/manageApplicationVersionTH";
        } else {
            throw new EntityNotFoundException(String.format("Group Entity not found for Application: %s", application.getId()));
        }
    }

//    @RequestMapping(value = "/manager/applicationVersionUploadProgress", method = RequestMethod.POST)
//    public
//    @ResponseBody
//    Result applicationVersionUploadProgress(HttpServletResponse response, @ModelAttribute("version") UploadApplicationVersion uploadApplicationVersion) {
//        MultipartFile file = uploadApplicationVersion.getAppFile();
//        Result result = new Result();
//
//        try {
//            if (file != null && !file.isEmpty()) {
//                file.getBytes();
//                result.setResult(true);
//            } else {
//                result.setResult(false);
//            }
//        } catch (IOException e) {
//            log.error("IOException getting bytes for file during application version upload.", e);
//            result.setResult(false);
//        }
//
//        response.setHeader("Pragma", "no-cache");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setDateHeader("Expires", 0);
//
//        return result;
//    }

    @PreAuthorize("canEditApplication(#applicationVersionForm.parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadVersion", method = RequestMethod.POST)
    public String create(final HttpServletRequest request, Model model, @ModelAttribute("version") @Validated ApplicationVersionForm applicationVersionForm, BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        Long parentId = applicationVersionForm.getParentId();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.version", bindingResult);
            redirectAttributes.addFlashAttribute("version", applicationVersionForm);

            if (applicationVersionForm.getId() == null || applicationVersionForm.getId() <= 0) {
                return String.format("redirect:/manager/addVersion/%s", parentId, applicationVersionForm.getId());
            } else {
                return String.format("redirect:/manager/editVersion/%s/%s", parentId, applicationVersionForm.getId());
            }
        }

        ApplicationVersion savedApplicationVersion = null;
        try {
            savedApplicationVersion = applicationVersionControllerService.saveApplicationVersion(applicationVersionForm, true);
        } catch (ApplicationVersionResignException e) {
            log.error(e.getMessage());
            String[] codes = {"desktop.manageApplicationVersion.resignError.generic"};
            ObjectError error = new ObjectError("version", codes, null, null);
            bindingResult.addError(error);

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.version", bindingResult);
            redirectAttributes.addFlashAttribute("version", applicationVersionForm);

            if (applicationVersionForm.getId() == null || applicationVersionForm.getId() <= 0) {
                return String.format("redirect:/manager/addVersion/%s", parentId, applicationVersionForm.getId());
            } else {
                return String.format("redirect:/manager/editVersion/%s/%s", parentId, applicationVersionForm.getId());
            }
        }

        if (savedApplicationVersion == null || savedApplicationVersion.getId() == null && savedApplicationVersion.getId() <= 0) {
            bindingResult.reject("desktop.manageApplicationVersion.error.generic");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.version", bindingResult);
            redirectAttributes.addFlashAttribute("version", applicationVersionForm);

            if (applicationVersionForm.getId() == null || applicationVersionForm.getId() <= 0) {
                return String.format("redirect:/manager/addVersion/%s", parentId, applicationVersionForm.getId());
            } else {
                return String.format("redirect:/manager/editVersion/%s/%s", parentId, applicationVersionForm.getId());
            }
        }

        redirectAttributes.addFlashAttribute("updateSuccess", true);
        return String.format("redirect:/manager/editVersion/%s/%s", parentId, savedApplicationVersion.getId());
    }

    @PreAuthorize("canEditApplication(#parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteVersion/{parentId}/{versionId}")
    public String deleteVersion(@PathVariable Long parentId, @PathVariable Long versionId, final RedirectAttributes redirectAttributes) {
        checkRequiredEntity(applicationService, parentId);
        checkRequiredEntity(applicationVersionService, versionId);

        applicationVersionService.delete(versionId);

        redirectAttributes.addFlashAttribute("deleteSuccess", !applicationVersionService.doesEntityExist(versionId));

        return "redirect:/manager/addVersion/" + parentId;
    }

}
