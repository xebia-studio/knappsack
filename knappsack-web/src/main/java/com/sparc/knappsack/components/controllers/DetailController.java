package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.forms.EnumEditor;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.util.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class DetailController extends AbstractController {

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("eventWatchService")
    @Autowired(required = true)
    private EventWatchService eventWatchService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(ApplicationType.class, new EnumEditor(ApplicationType.class));
    }

    @PreAuthorize("hasAccessToApplication(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public String loadDetailPage(HttpServletRequest request, Model model,  @PathVariable Long id, UserAgentInfo userAgentInfo) {
        checkRequiredEntity(applicationService, id);
        User user = userService.getUserFromSecurityContext();
        Application application = applicationService.get(id);

        ApplicationModel applicationModel = applicationService.createApplicationModel(id);
        model.addAttribute("selectedApplication", applicationModel);
        model.addAttribute("applicationType", applicationModel.getApplicationType().name());

        model.addAttribute("deviceType", userAgentInfo.getApplicationType());

        boolean iosDetected = false;
        if (userAgentInfo.detectIos()) {
            iosDetected = true;
        }

        model.addAttribute("downloadDirectly", determineDownloadDirectly(userAgentInfo));

        model.addAttribute("iosDetected", iosDetected);
        model.addAttribute("showInstallBtn", applicationService.determineApplicationVisibility(application, userAgentInfo.getApplicationType()));
        List<ApplicationVersion> versions = userService.getApplicationVersions(user, id, SortOrder.DESCENDING, AppState.ORGANIZATION_PUBLISH, AppState.GROUP_PUBLISH, AppState.ORG_PUBLISH_REQUEST);
        model.addAttribute("versions", versions);
        model.addAttribute("initialVersionId", (versions != null && versions.size() > 0 ? versions.get(0).getId() : null));

        model.addAttribute("isSubscribed", eventWatchService.doesEventWatchExist(user, applicationService.get(id)));

        return "detailTH";
    }

    private boolean determineDownloadDirectly(UserAgentInfo userAgentInfo) {
        return !userAgentInfo.detectAndroid();
    }
}
