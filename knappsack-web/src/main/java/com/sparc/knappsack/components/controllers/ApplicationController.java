package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.components.validators.ApplicationValidator;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.exceptions.TokenException;
import com.sparc.knappsack.forms.EnumEditor;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.forms.UploadApplication;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import com.sparc.knappsack.security.SingleUseToken;
import com.sparc.knappsack.security.SingleUseTokenRepository;
import com.sparc.knappsack.util.UserAgentInfo;
import com.sparc.knappsack.util.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ApplicationController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Qualifier("applicationService")
    @Autowired
    private ApplicationService applicationService;

    @Qualifier("applicationVersionService")
    @Autowired
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationValidator")
    @Autowired
    private ApplicationValidator applicationValidator;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("groupService")
    @Autowired
    private GroupService groupService;

    @Qualifier("iosService")
    @Autowired(required = true)
    private IOSService iosService;

    @Autowired(required = true)
    private SingleUseTokenRepository singleUseTokenRepository;


    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(applicationValidator);
        binder.setBindEmptyMultipartFiles(false);
        binder.registerCustomEditor(StorageType.class, new EnumEditor(StorageType.class));
    }

    @PreAuthorize("isOrganizationAdminForGroup(#uploadApplication.groupId) or isGroupAdmin(#uploadApplication.groupId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/uploadFile", method = RequestMethod.POST)
    public String saveApplication(HttpServletRequest request, @ModelAttribute("uploadApplication") UploadApplication uploadApplication, Model model, BindingResult bindingResult) {
        applicationValidator.validate(uploadApplication, bindingResult);
        if(bindingResult.hasErrors()) {
            return addApplication(model, uploadApplication.getGroupId());
        }

        uploadApplication.setContextPath(request.getHeader("origin") + request.getContextPath());

        boolean editing = (uploadApplication.getId() != null && uploadApplication.getId() > 0);

        Application savedApplication = applicationService.saveApplication(uploadApplication);

        if (!editing) {
            return "redirect:/manager/addVersion/" + savedApplication.getId();
        } else {
            return "redirect:/manager/editGroup/" + uploadApplication.getGroupId();
        }
    }

    @PreAuthorize("canEditApplication(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteApplication/{id}", method = RequestMethod.GET)
    public String deleteApplication(@PathVariable Long id) {
        checkRequiredEntity(applicationService, id);

        Application application = applicationService.get(id);
        Group group = groupService.getOwnedGroup(application);
        applicationService.delete(id);

        return "redirect:/manager/editGroup/" + group.getId();
    }

    @PreAuthorize("canEditApplication(#applicationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteIcon/{applicationId}", method = RequestMethod.POST)
    public @ResponseBody Result deleteIcon(@PathVariable Long applicationId) {
        Result result = new Result();

        try {
            checkRequiredEntity(applicationService, applicationId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete icon for non-existent application: %s", applicationId));
            result.setResult(false);
            return result;
        }

        applicationService.deleteIcon(applicationId);

        result.setResult(true);
        return result;
    }

    @PreAuthorize("canEditApplication(#applicationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteScreenShot/{applicationId}/{index}", method = RequestMethod.POST)
    public @ResponseBody Result deleteScreenShot(@PathVariable Long applicationId, @PathVariable int index) {
        Result result = new Result();

        try {
            checkRequiredEntity(applicationService, applicationId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete screenshot for non-existent application: %s", applicationId));
            result.setResult(false);
            return result;
        }

        applicationService.deleteScreenshot(applicationId, index);

        result.setResult(true);

        return result;
    }

    @PreAuthorize("isOrganizationAdminForGroup(#groupId) or isGroupAdmin(#groupId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addApplication/{groupId}", method = RequestMethod.GET)
    public String addApplication(Model model, @PathVariable Long groupId) {

        checkRequiredEntity(groupService, groupId);

        Group group = groupService.get(groupId);

        model.addAttribute("parentGroupId", group.getId());
        model.addAttribute("parentGroupName", group.getName());

        if (!model.containsAttribute("uploadApplication")) {
            UploadApplication uploadApplication = new UploadApplication();
            uploadApplication.setGroupId(groupId);
            uploadApplication.setOrgStorageConfigId(group.getOrganization().getOrgStorageConfig().getId());
            model.addAttribute("orgStorageConfigId", group.getOrganization().getOrgStorageConfig().getId());
            model.addAttribute("uploadApplication", uploadApplication);
        }
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>(group.getOrganization().getOrgStorageConfig().getStorageConfigurations());
        model.addAttribute("storageConfigurations", storageConfigurations);
        model.addAttribute("categories", group.getOrganization().getCategories());
        model.addAttribute("applicationTypes", ApplicationType.values());
        model.addAttribute("isEdit", false);

        return "manager/manageApplicationTH";
    }

    @PreAuthorize("canEditApplication(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editApplication/{id}", method = RequestMethod.GET)
    public String editApplication(Model model, @PathVariable Long id) {

        checkRequiredEntity(applicationService, id);

        Application application = applicationService.get(id);

        Group group = groupService.getOwnedGroup(application);

        if (group != null) {

            model.addAttribute("parentGroupId", group.getId());
            model.addAttribute("parentGroupName", group.getName());

            if (!model.containsAttribute("uploadApplication")) {

                UploadApplication uploadApplication = new UploadApplication();
                uploadApplication.setId(application.getId());
                uploadApplication.setGroupId(group.getId());
                uploadApplication.setApplicationType(application.getApplicationType());
                uploadApplication.setDescription(application.getDescription());
                uploadApplication.setName(application.getName());
                uploadApplication.setCategoryId(application.getCategory().getId());

                uploadApplication.setOrgStorageConfigId(group.getOrganization().getOrgStorageConfig().getId());
                uploadApplication.setStorageConfigurationId(application.getStorageConfiguration().getId());

                AppFile icon = application.getIcon();
                if (icon != null) {
                    MockMultipartFile iconMultipartFile = new MockMultipartFile(application.getIcon().getName(), application.getIcon().getName(), application.getIcon().getType(), new byte[0]);
                    uploadApplication.setIcon(iconMultipartFile);
                }

                List<AppFile> screenShots = application.getScreenShots();
                List<MultipartFile> screenShotMultipartFiles = new ArrayList<MultipartFile>();
                for (AppFile screenShot : screenShots) {
                    MockMultipartFile screenShotMultipartFile = new MockMultipartFile(screenShot.getName(), screenShot.getName(), screenShot.getType(), new byte[0]);
                    screenShotMultipartFiles.add(screenShotMultipartFile);
                }
                uploadApplication.setScreenShots(screenShotMultipartFiles);

                List<UploadApplicationVersion> uploadApplicationVersions = new ArrayList<UploadApplicationVersion>();
                for (ApplicationVersion version : application.getApplicationVersions()) {
                    UploadApplicationVersion uploadApplicationVersion = new UploadApplicationVersion();
                    uploadApplicationVersion.setId(version.getId());
                    uploadApplicationVersion.setVersionName(version.getVersionName());
                    uploadApplicationVersion.setAppState(version.getAppState());
                    uploadApplicationVersions.add(uploadApplicationVersion);
                }
                uploadApplication.setApplicationVersions(uploadApplicationVersions);

                model.addAttribute("uploadApplication", uploadApplication);
            }

            List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>(group.getOrganization().getOrgStorageConfig().getStorageConfigurations());
            model.addAttribute("storageConfigurations", storageConfigurations);
            model.addAttribute("categories", group.getOrganization().getCategories());
            model.addAttribute("applicationTypes", ApplicationType.values());
            model.addAttribute("isEdit", true);

            return "manager/manageApplicationTH";
        } else {
            throw new EntityNotFoundException(String.format("Group Entity not found while editing Application: %s", application.getId()));
        }
    }

    @PreAuthorize("hasAccessToApplicationVersion(#applicationVersionId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/downloadApplication/{applicationVersionId}", method = RequestMethod.GET)
    public String downloadApplication(HttpServletResponse response, @PathVariable String applicationVersionId, UserAgentInfo userAgentInfo) {

        checkRequiredEntity(applicationVersionService, Long.valueOf(applicationVersionId));

        String url = addApplicationToResponse(response, applicationVersionId, userAgentInfo);
        if (url.isEmpty()) {
            return "";
        }
        return "redirect:" + url;
    }

    @RequestMapping(value = "ios/downloadApplication/{id}", method = RequestMethod.GET)
    public void downloadApplicationIos(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, UserAgentInfo userAgentInfo) {
        checkRequiredEntity(applicationVersionService, Long.valueOf(id));

        request.getSession().invalidate();
        request.getSession().removeAttribute("continueAttribute");
        addApplicationToResponse(response, id, userAgentInfo);
    }

    @PreAuthorize("hasAccessToApplicationVersion(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/generateDownloadApplicationUrl/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String generateDownloadApplicationUrl(HttpServletRequest request, UserAgentInfo userAgentInfo, @PathVariable String id) throws TokenException {
        checkRequiredEntity(applicationVersionService, Long.valueOf(id));

        String sessionId = request.getSession(false).getId();

        if (userAgentInfo.detectIos()) {
            SingleUseToken token = new SingleUseToken(sessionId);
            singleUseTokenRepository.putToken(token);

            int index = request.getRequestURL().indexOf(request.getServletPath());
            String newContext = "/ios/downloadIOSPlist/" + id + "/" + token.getSessionIdHash();
            StringBuffer url = request.getRequestURL().replace(index, request.getRequestURL().length(), newContext);

            return "itms-services://?action=download-manifest&url=" + url.toString();
        } else {
            return "/downloadApplication/" + id;
        }

    }

    private String addApplicationToResponse(HttpServletResponse response, String id, UserAgentInfo userAgentInfo) {
        ApplicationVersion version = applicationVersionService.get(Long.parseLong(id));

        if (version != null) {
            if (!applicationService.determineApplicationVisibility(version.getApplication(), userAgentInfo.getApplicationType())) {
                try {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "The specified application is unsupported on your current device.");
                } catch (IOException e) {
                    log.error("Error sending: " + HttpServletResponse.SC_FORBIDDEN, e);
                }
                return "";
            }

            AppFile appFile = version.getInstallationFile();
            StorageService storageService = storageServiceFactory.getStorageService(appFile.getStorageType());

            File file = new File(appFile.getAbsolutePath());

            response.setContentType(appFile.getType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error("Error creating FileInputStream", e);
            }
            try {
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            } catch (IOException e) {
                log.error("Error copying inputStream to response outputStream", e);
            }
        }
        return "";
    }

    @RequestMapping(value = "ios/downloadIOSPlist/{id}/{token}", method = RequestMethod.GET)
    public @ResponseBody String downloadIOSPlist(WebRequest request, HttpServletResponse response, @PathVariable Long id, @PathVariable String token) {
        checkRequiredEntity(applicationVersionService, Long.valueOf(id));

        response.setContentType("text/xml");

        return (iosService.createIOSPlistXML(id, request, token));
    }

}
