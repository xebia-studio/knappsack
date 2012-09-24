package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.ApplicationVersionControllerService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.validators.ApplicationVersionValidator;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
public class ApplicationVersionController extends AbstractController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("applicationVersionValidator")
    @Autowired
    private ApplicationVersionValidator applicationVersionValidator;

    @Qualifier("applicationService")
    @Autowired
    private ApplicationService applicationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationVersionControllerService")
    @Autowired(required = true)
    private ApplicationVersionControllerService applicationVersionControllerService;

    @Qualifier("groupService")
    @Autowired
    private GroupService groupService;

    @InitBinder()
    protected void initBinder(WebDataBinder binder) {
        binder.setBindEmptyMultipartFiles(false);
    }

    @PreAuthorize("hasAccessToApplicationVersion(#appVersionId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/updateAppVersionState", method = RequestMethod.GET)
    public
    @ResponseBody
    Result updateAppState(@RequestParam Long appVersionId, @RequestParam String appState) {
        boolean success = applicationVersionControllerService.updateApplicationVersionState(appVersionId, AppState.valueOf(appState));
        Result result = new Result();
        result.setResult(success);
        return result;
    }


    @PreAuthorize("isOrganizationAdminForGroup(#groupId) or isGroupAdmin(#groupId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addVersion/{parentId}/{groupId}", method = RequestMethod.GET)
    public String addApplicationVersion(Model model, @PathVariable Long parentId, @PathVariable Long groupId) {

        Application application = applicationService.get(parentId);
        model.addAttribute("parentApplicationId", application.getId());
        model.addAttribute("parentApplicationName", application.getName());
        Group group = groupService.get(groupId);
        model.addAttribute("parentGroupId", group.getId());
        model.addAttribute("parentGroupName", group.getName());

        if (!model.containsAttribute("version")) {
            UploadApplicationVersion version = new UploadApplicationVersion();
            version.setParentId(parentId);
            version.setGroupId(groupId);
            version.setStorageConfigurationId(application.getStorageConfiguration().getId());

            model.addAttribute("version", version);
        }

        model.addAttribute("currentGuestGroupIds", new ArrayList<Group>());
        List<Group> groups = new ArrayList<Group>(group.getOrganization().getGroups());
        groups.remove(group);
        model.addAttribute("groups", groups);
        model.addAttribute("appStates", AppState.values());
        model.addAttribute("isEdit", false);

        return "manager/manageApplicationVersionTH";
    }

    @PreAuthorize("canEditApplication(#parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editVersion/{parentId}/{versionId}", method = RequestMethod.GET)
    public String editApplicationVersion(Model model, @PathVariable Long parentId, @PathVariable Long versionId) {
        ApplicationVersion version = applicationVersionService.get(versionId);

        Application application = applicationService.get(parentId);
        model.addAttribute("parentApplicationId", application.getId());
        model.addAttribute("parentApplicationName", application.getName());
        Group group = groupService.getOwnedGroup(application);
        model.addAttribute("parentGroupId", group.getId());
        model.addAttribute("parentGroupName", group.getName());

        UploadApplicationVersion uploadApplicationVersion;
        if (model.containsAttribute("version")) {
            uploadApplicationVersion = (UploadApplicationVersion) model.asMap().get("version");
        } else {
            uploadApplicationVersion = new UploadApplicationVersion();
        }

        uploadApplicationVersion.setId(version.getId());
        uploadApplicationVersion.setRecentChanges(version.getRecentChanges());
        uploadApplicationVersion.setVersionName(version.getVersionName());
        uploadApplicationVersion.setParentId(parentId);
        uploadApplicationVersion.setAppState((version.getAppState()));
        uploadApplicationVersion.setStorageConfigurationId(application.getStorageConfiguration().getId());

        if (version.getInstallationFile() != null) {
            MockMultipartFile multipartFile = new MockMultipartFile(version.getInstallationFile().getName(), version.getInstallationFile().getName(), version.getInstallationFile().getType(), new byte[0]);
            uploadApplicationVersion.setAppFile(multipartFile);
        }

        if (version.getProvisioningProfile() != null) {
            MockMultipartFile multipartFile = new MockMultipartFile(version.getProvisioningProfile().getName(), version.getProvisioningProfile().getName(), version.getProvisioningProfile().getType(), new byte[0]);
            uploadApplicationVersion.setProvisioningProfile(multipartFile);
        }

        model.addAttribute("version", uploadApplicationVersion);

        //Put this app versions guest groups on the model so we can highlight them in the multi-select box
        List<Group> currentGuestGroups = groupService.getGuestGroups(version);
        Set<Long> currentGuestGroupIds = new HashSet<Long>();
        if (currentGuestGroups != null) {
            for (Group guestGroup : currentGuestGroups) {
                currentGuestGroupIds.add(guestGroup.getId());
            }
        }
        model.addAttribute("currentGuestGroupIds", currentGuestGroupIds);
        model.addAttribute("groupId", group.getId());
        //We don't want the group that owns the application in the list of guest groups
        List<Group> groups = new ArrayList<Group>(group.getOrganization().getGroups());
        groups.remove(group);
        model.addAttribute("groups", groups);
        model.addAttribute("appStates", Arrays.asList(AppState.values()));

        model.addAttribute("isEdit", true);

        return "manager/manageApplicationVersionTH";
    }

    @RequestMapping(value = "/manager/applicationVersionUploadProgress", method = RequestMethod.POST)
    public
    @ResponseBody
    Result applicationVersionUploadProgress(HttpServletResponse response, @ModelAttribute("version") UploadApplicationVersion uploadApplicationVersion) {
        MultipartFile file = uploadApplicationVersion.getAppFile();
        Result result = new Result();

        try {
            if (file != null && !file.isEmpty()) {
                file.getBytes();
                result.setResult(true);
            } else {
                result.setResult(false);
            }
        } catch (IOException e) {
            log.error("IOException getting bytes for file during application version upload.", e);
            result.setResult(false);
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        return result;
    }

    @PreAuthorize("canEditApplication(#uploadApplicationVersion.parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadVersion", method = RequestMethod.POST)
    public String create(@ModelAttribute("version") UploadApplicationVersion uploadApplicationVersion, Model model, BindingResult bindingResult) {
        applicationVersionValidator.validate(uploadApplicationVersion, bindingResult);
        if (bindingResult.hasErrors()) {
            if (uploadApplicationVersion.isEditing()) {
                return editApplicationVersion(model, uploadApplicationVersion.getParentId(), uploadApplicationVersion.getId());
            }
            return addApplicationVersion(model, uploadApplicationVersion.getParentId(), uploadApplicationVersion.getGroupId());
        }

        Long parentId = uploadApplicationVersion.getParentId();

        //TODO: error handling
        boolean success = applicationVersionControllerService.saveApplicationVersion(uploadApplicationVersion);

        return "redirect:/manager/editApplication/" + parentId;
    }

    @PreAuthorize("canEditApplication(#parentId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteVersion/{parentId}/{versionId}")
    public String deleteVersion(@PathVariable Long parentId, @PathVariable Long versionId) {
        applicationVersionService.delete(versionId);

        return "redirect:/manager/editApplication/" + parentId;
    }

}
