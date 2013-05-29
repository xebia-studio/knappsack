package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.ApplicationForm;
import com.sparc.knappsack.models.api.v1.Application;
import com.sparc.knappsack.models.api.v1.ApplicationVersion;
import com.sparc.knappsack.util.UserAgentInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api/v1/applications")
@Api(value = "Application operations", listingClass = "ApplicationAPIv1", basePath = "/api/v1/applications", description = "All operations for applications")
public class ApplicationAPIv1 extends BaseAPIv1Controller {

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Autowired(required = true)
    private UserService userService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Find all applications", notes = "Get all applications applicable to the user and user agent", httpMethod = "GET", responseClass = "Application", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Application[] getApplications(UserAgentInfo userAgentInfo) {
        User user = userService.getUserFromSecurityContext();
        List<Application> applications = userService.getApplicationModelsForUser(user, userAgentInfo.getApplicationType(), Application.class);

        return applications.toArray(new Application[applications.size()]);
    }

    @PreAuthorize("hasAccessToApplication(#applicationId) or hasRole('ROLE_USER')")
    @ApiOperation(value = "Find application", notes = "Get the application with this ID", httpMethod = "GET", responseClass = "Application", multiValueResponse = false)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/{applicationId}", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Application getApplication(
            @ApiParam(name = "applicationId", value = "The application ID", required = true, internalDescription = "java.lang.Long")
            @PathVariable Long applicationId) {
        checkRequiredEntity(applicationService, applicationId);
        return applicationService.getApplicationModel(applicationId, Application.class);
    }

    @PreAuthorize("hasAccessToApplication(#applicationId) or hasRole('ROLE_USER')")
    @ApiOperation(value = "Find all application versions", notes = "Get all application versions for the application with this ID", httpMethod = "GET", responseClass = "ApplicationVersion", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/{applicationId}/applicationVersions", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    List<ApplicationVersion> getApplicationVersions(
            @ApiParam(name = "applicationId", value = "The application ID", required = true, internalDescription = "java.lang.Long")
            @PathVariable Long applicationId,
            @ApiParam(name = "appState", value = "The state of the application.  Determines the visibility of the application to users.", allowableValues = "GROUP_PUBLISH,ORGANIZATION_PUBLISH,DISABLED,ORG_PUBLISH_REQUEST", required = false, internalDescription = "com.sparc.knappsack.enums.AppState")
            @RequestParam(value = "appState", required = false) String appState) {

        checkRequiredEntity(applicationService, applicationId);

        if (appState == null || appState.isEmpty()) {
            return applicationVersionService.getApplicationVersionModels(applicationId, ApplicationVersion.class, AppState.values());
        } else {
            AppState state = AppState.valueOf(appState);
            return applicationVersionService.getApplicationVersionModels(applicationId, ApplicationVersion.class, state);
        }
    }

    @PreAuthorize("isDomainAdmin(#groupId) or hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Create an application", notes = "Create a new application", httpMethod = "POST", responseClass = "Application", multiValueResponse = false)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(method = RequestMethod.POST, produces = contentType)
    public
    @ResponseBody
    Application createApplication(HttpServletRequest request,
                                       @ApiParam(name = "applicationType", value = "The installation file", allowableValues = "ANDROID,IPHONE,IPAD,IOS,CHROME,FIREFOX,WINDOWSPHONE7,BLACKBERRY,OTHER", required = true, internalDescription = "java.lang.String") @RequestParam(value = "applicationType", required = true) String applicationType,
                                       @ApiParam(name = "categoryId", value = "The ID of the category to which this application belongs", required = true, internalDescription = "java.lang.Long") @RequestParam(value = "categoryId", required = true) Long categoryId,
                                       @ApiParam(name = "description", value = "The description of the application", required = true, internalDescription = "java.lang.String") @RequestParam(value = "description", required = true) String description,
                                       @ApiParam(name = "groupId", value = "The group that this application belongs to", required = true, internalDescription = "java.lang.Long") @RequestParam(value = "groupId", required = true) Long groupId,
                                       @ApiParam(name = "name", value = "The name of the application", required = true, internalDescription = "java.lang.String") @RequestParam(value = "name", required = true) String name,
                                       @ApiParam(name = "icon", value = "The icon image file", required = false, internalDescription = "org.springframework.web.multipart.MultipartFile") @RequestParam(value = "icon", required = false) MultipartFile icon) {

        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setApplicationType(ApplicationType.valueOf(applicationType));
        applicationForm.setCategoryId(categoryId);
        applicationForm.setDescription(description);
        applicationForm.setGroupId(groupId);
        applicationForm.setName(name);
        if(icon != null) {
            applicationForm.setIcon(icon);
        }

        applicationForm.setContextPath(request.getHeader("origin") + request.getContextPath());

        return applicationService.getApplicationModel(applicationService.saveApplication(applicationForm).getId(), Application.class);
    }
}
