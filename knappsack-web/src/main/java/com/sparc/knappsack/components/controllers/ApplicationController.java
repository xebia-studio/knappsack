package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.components.validators.ApplicationValidator;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.exceptions.TokenException;
import com.sparc.knappsack.forms.ApplicationForm;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import com.sparc.knappsack.forms.EnumEditor;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.*;
import com.sparc.knappsack.security.SingleUseToken;
import com.sparc.knappsack.security.SingleUseTokenRepository;
import com.sparc.knappsack.util.UserAgentInfo;
import com.sparc.knappsack.util.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ApplicationController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationVersionUserStatisticService")
    @Autowired(required = true)
    private ApplicationVersionUserStatisticService applicationVersionUserStatisticService;

    @Qualifier("eventWatchService")
    @Autowired(required = true)
    private EventWatchService eventWatchService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("applicationValidator")
    @Autowired(required = true)
    private ApplicationValidator applicationValidator;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("categoryService")
    @Autowired(required = true)
    private CategoryService categoryService;

    @Qualifier("bandwidthService")
    @Autowired(required = true)
    private BandwidthService bandwidthService;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("iosService")
    @Autowired(required = true)
    private IOSService iosService;

    @Qualifier("singleUseTokenRepository")
    @Autowired(required = true)
    private SingleUseTokenRepository singleUseTokenRepository;

    @Qualifier("messageSource")
    @Autowired(required = true)
    private MessageSource messageSource;

    @InitBinder("applicationForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(applicationValidator);
        binder.setBindEmptyMultipartFiles(false);
        binder.registerCustomEditor(StorageType.class, new EnumEditor(StorageType.class));
    }

    @PreAuthorize("isDomainAdmin(#applicationForm.groupId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/saveApplication", method = RequestMethod.POST)
    public String saveApplication(Model model, @ModelAttribute("applicationForm") @Validated ApplicationForm applicationForm, BindingResult bindingResult, HttpServletRequest request, final RedirectAttributes redirectAttributes) {
        boolean editing = applicationForm.getId() != null && applicationForm.getId() > 0;

        if(bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.applicationForm", bindingResult);
            redirectAttributes.addFlashAttribute("applicationForm", applicationForm);

            return editing ? String.format("redirect:/manager/editApplication/%s", applicationForm.getId()) : "redirect:/manager/addApplication";
        }


        applicationForm.setContextPath(request.getHeader("origin") + request.getContextPath());

        Application savedApplication = applicationService.saveApplication(applicationForm);

        if (savedApplication == null || savedApplication.getId() == null || savedApplication.getId() <= 0) {
            bindingResult.reject("desktop.manageApplication.error.generic");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.applicationForm", bindingResult);
            redirectAttributes.addFlashAttribute("applicationForm", applicationForm);
            return editing ? String.format("redirect:/manager/editApplication/%s", applicationForm.getId()) : "redirect:/manager/addApplication";
        }

        return String.format("redirect:/detail/%s", savedApplication.getId());

    }

    @PreAuthorize("canEditApplication(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteApplication/{id}", method = RequestMethod.GET)
    public String deleteApplication(@PathVariable Long id) {
        checkRequiredEntity(applicationService, id);

        applicationService.delete(id);

        return "redirect:/home";
    }

    @PreAuthorize("canEditApplication(#applicationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteApplication", method = RequestMethod.POST)
    public @ResponseBody Result deleteApplicationAJAX(@RequestParam(value = "id", required = true) Long applicationId) {
        Result result = new Result();
        applicationService.delete(applicationId);

        result.setResult(!applicationService.doesEntityExist(applicationId));

        return result;
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
    @RequestMapping(value = "/manager/viewDownloadSummary/{applicationId}", method = RequestMethod.GET)
    public @ResponseBody List<ApplicationVersionStatisticSummaryModel> viewDownloadSummary(@PathVariable Long applicationId) {
        Application application = applicationService.get(applicationId);

        return applicationVersionUserStatisticService.getApplicationVersionUserStatisticSummaryModels(application);
    }

    @PreAuthorize("canEditApplication(#applicationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/viewDownloadDetails/{applicationId}", method = RequestMethod.GET)
    public @ResponseBody List<ApplicationVersionUserStatisticModel> viewDownloadDetails(@PathVariable Long applicationId) {
        Application application = applicationService.get(applicationId);
        List<ApplicationVersionUserStatistic> applicationVersionUserStatistics = applicationVersionUserStatisticService.get(application);

        return applicationVersionUserStatisticService.toModels(applicationVersionUserStatistics);
    }

    @PreAuthorize("canEditApplication(#applicationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteScreenshot/{applicationId}/{index}", method = RequestMethod.POST)
    public @ResponseBody Result deleteScreenshot(@PathVariable Long applicationId, @PathVariable int index) {
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

    @PreAuthorize("isDomainAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addApplication", method = RequestMethod.GET)
    public String addApplication(Model model, @RequestParam(required = false) Long grp) {

        if (!model.containsAttribute("applicationForm")) {
            ApplicationForm applicationForm = new ApplicationForm();
            applicationForm.setApplicationVersion(new ApplicationVersionForm());
            applicationForm.setGroupId(grp);
            model.addAttribute("applicationForm", applicationForm);
        }

        User user = userService.getUserFromSecurityContext();
        Organization organization = user.getActiveOrganization();

        List<CategoryModel> categories = new ArrayList<CategoryModel>();
        if (organization != null) {
            for (Category category : organization.getCategories()) {
                CategoryModel categoryModel = categoryService.createCategoryModel(category, false);
                if (categoryModel != null) {
                    categories.add(categoryModel);
                }
            }
        }

        model.addAttribute("groups", userService.getAdministeredGroupModels(user, GroupModel.class, SortOrder.ASCENDING));
        model.addAttribute("categories", categories);
        model.addAttribute("applicationTypes", ApplicationType.values());
        model.addAttribute("isEdit", false);

        return "manager/manageApplicationTH";
    }

    @PreAuthorize("canEditApplication(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editApplication/{id}", method = RequestMethod.GET)
    public String editApplication(Model model, @PathVariable Long id, HttpServletRequest request) throws IOException {

        checkRequiredEntity(applicationService, id);

        User user = userService.getUserFromSecurityContext();

        Application application = applicationService.get(id);

        if (!model.containsAttribute("applicationForm")) {

            ApplicationForm applicationForm = new ApplicationForm();
            applicationForm.setId(application.getId());
            applicationForm.setGroupId(application.getOwnedGroup().getId());
            applicationForm.setApplicationType(application.getApplicationType());
            applicationForm.setDescription(application.getDescription());
            applicationForm.setName(application.getName());
            applicationForm.setCategoryId(application.getCategory().getId());

            StorageService storageService = storageServiceFactory.getStorageService(application.getStorageConfiguration().getStorageType());

            AppFile icon = application.getIcon();
            if (icon != null) {
                MockMultipartFile iconMultipartFile = new MockMultipartFile(application.getIcon().getName(), application.getIcon().getName(), application.getIcon().getType(), new byte[0]);
                applicationForm.setIcon(iconMultipartFile);
                model.addAttribute("icon", appFileService.createImageModel(icon));
            }

            List<AppFile> screenShots = application.getScreenshots();
            List<MultipartFile> screenShotMultipartFiles = new ArrayList<MultipartFile>();
            List<ImageModel> screenshotImageModels = new ArrayList<ImageModel>();
            for (AppFile screenShot : screenShots) {
                MockMultipartFile screenShotMultipartFile = new MockMultipartFile(screenShot.getName(), screenShot.getName(), screenShot.getType(), storageService.getInputStream(screenShot));
                screenShotMultipartFiles.add(screenShotMultipartFile);
                screenshotImageModels.add(appFileService.createImageModel(screenShot));
            }
            applicationForm.setScreenshots(screenShotMultipartFiles);
            model.addAttribute("screenshots", screenshotImageModels);

            model.addAttribute("applicationForm", applicationForm);
        }

        List<ApplicationVersionModel> applicationVersions = new ArrayList<ApplicationVersionModel>();
        for (ApplicationVersion applicationVersion : application.getApplicationVersions()) {
            ApplicationVersionModel applicationVersionModel = applicationVersionService.createApplicationVersionModel(applicationVersion, false);
            if (applicationVersionModel != null) {
                applicationVersions.add(applicationVersionModel);
            }
        }
        model.addAttribute("applicationVersion", applicationVersions);

        model.addAttribute("groups", userService.getAdministeredGroupModels(user, GroupModel.class, SortOrder.ASCENDING));
        model.addAttribute("categories", application.getOwnedGroup().getOrganization().getCategories());
        model.addAttribute("applicationTypes", ApplicationType.getAllInGroup(application.getApplicationType()));
        model.addAttribute("isEdit", true);
        model.addAttribute("appStates", getAppStates(request));

        return "manager/manageApplicationTH";
    }

    @PreAuthorize("hasAccessToApplicationVersion(#applicationVersionId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/downloadApplication/{applicationVersionId}", method = RequestMethod.GET)
    public String downloadApplication(HttpServletRequest request, HttpServletResponse response, @PathVariable String applicationVersionId, UserAgentInfo userAgentInfo) {

        checkRequiredEntity(applicationVersionService, Long.valueOf(applicationVersionId));

        String url = addApplicationToResponse(response, applicationVersionId, userAgentInfo, true);

        subscribeUserAndCreateStatistic(Long.valueOf(applicationVersionId), request);

        if (!StringUtils.hasText(url)) {
            return "";
        }

        return "redirect:" + url;
    }

    @RequestMapping(value = "/ios/downloadApplication/{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public void downloadApplicationIOS(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestParam(required = true) String token, UserAgentInfo userAgentInfo) {
        checkRequiredEntity(applicationVersionService, Long.valueOf(id));

        // HttpSession session = request.getSession(false);
        // if (session != null) {
        //     session.invalidate();
        // }

        addApplicationToResponse(response, id, userAgentInfo, RequestMethod.GET.equals(RequestMethod.valueOf(request.getMethod())) ? true : false);
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
            subscribeUserAndCreateStatistic(Long.valueOf(id), request);

            return "itms-services://?action=download-manifest&url=" + url.toString();
        } else {
            return "/downloadApplication/" + id;
        }

    }

    public String addApplicationToResponse(HttpServletResponse response, String id, UserAgentInfo userAgentInfo, boolean includeFile) {
        ApplicationVersion version = applicationVersionService.get(Long.parseLong(id));

        if (version != null && version.getAppState() != null && version.getAppState().isDownloadable()) {
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
            if (storageService instanceof RemoteStorageService) {
                if(bandwidthService.isBandwidthLimitReached(version)) {
                    return "/detail/" + version.getApplication().getId();
                }

                String url = ((RemoteStorageService)storageService).getUrl(appFile, 20);
                response.setStatus(200);
                response.setContentType(appFile.getType());
                response.setHeader("Content-Disposition", "attachment; filename=\"" + url + "\"");
                return url;
            }

            response.setContentType(appFile.getType());
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(false);
            response.setStatus(200);
            response.setHeader("Content-Length", numberFormat.format(appFile.getSize() * 1024 * 1024) /*Convert MB to Bytes*/);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + appFile.getName() + "\"");
            if (includeFile) {
                File file = new File(appFile.getAbsolutePath());
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
        }
        return "";
    }

    @RequestMapping(value = "ios/downloadIOSPlist/{id}/{token}", method = RequestMethod.GET)
    public @ResponseBody String downloadIOSPlist(WebRequest request, HttpServletResponse response, @PathVariable Long id, @PathVariable String token) {
        checkRequiredEntity(applicationVersionService, Long.valueOf(id));

        response.setContentType("text/xml");

        return (iosService.createIOSPlistXML(id, request, token));
    }

    /**
     * When a user downloads a specific application version, automatically subscribe them to that application and
     * create an ApplicationVersionUserStatistic to track the download.
     */
    public void subscribeUserAndCreateStatistic(Long applicationVersionId, HttpServletRequest request) {
        User user = userService.getUserFromSecurityContext();
        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);
        applicationVersionUserStatisticService.create(applicationVersion, user, request.getRemoteAddr(), request.getHeader("User-Agent"));
        boolean isSubscribed = eventWatchService.doesEventWatchExist(user, applicationVersion.getApplication());
        if(!isSubscribed) {
            eventWatchService.createEventWatch(user, applicationVersion.getApplication(), EventType.APPLICATION_VERSION_BECOMES_AVAILABLE);
        }
    }

    private List<InternationalizedObject> getAppStates(HttpServletRequest request) {
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

        return appStates;
    }

}
