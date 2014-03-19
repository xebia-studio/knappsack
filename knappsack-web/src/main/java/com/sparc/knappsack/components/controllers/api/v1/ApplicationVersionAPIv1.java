package com.sparc.knappsack.components.controllers.api.v1;

import com.google.common.io.Files;
import com.sparc.knappsack.components.controllers.ApplicationController;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.exceptions.TokenException;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.api.v1.ApplicationVersion;
import com.sparc.knappsack.models.api.v1.Property;
import com.sparc.knappsack.security.SingleUseToken;
import com.sparc.knappsack.security.SingleUseTokenRepository;
import com.sparc.knappsack.util.UserAgentInfo;
import com.sparc.knappsack.utils.Manifest;
import com.sparc.knappsack.utils.ManifestFactory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/api/v1/applicationVersions")
@Api(value = "Application version operations", listingClass = "ApplicationVersionAPIv1", basePath = "/api/v1/applicationVersions", description = "All operations for application versions")
public class ApplicationVersionAPIv1 extends BaseAPIv1Controller {

  @Autowired(required = true)
  private ApplicationVersionService applicationVersionService;
  //TODO temp test until logic can be cleaned up
  @Autowired(required = true)
  private ApplicationController applicationController;
  @Autowired(required = true)
  private ApplicationService applicationService;
  @Qualifier("singleUseTokenRepository")
  @Autowired(required = true)
  private SingleUseTokenRepository singleUseTokenRepository;

  @PreAuthorize("hasAccessToApplicationVersion(#applicationVersionId) or hasRole('ROLE_ADMIN')")
  @ApiOperation(value = "Find application version", notes = "Get the application version with this ID", httpMethod = "GET", responseClass = "ApplicationVersion", multiValueResponse = false)
  @ApiError(code = 500, reason = "Process error")
  @RequestMapping(value = "/{applicationVersionId}", method = RequestMethod.GET, produces = contentType)
  public
  @ResponseBody
  ApplicationVersion getApplicationVersion(
      @ApiParam(name = "applicationVersionId", value = "The application version ID", required = true, internalDescription = "java.lang.Long")
      @PathVariable Long applicationVersionId) {
    checkRequiredEntity(applicationVersionService, applicationVersionId);
    return applicationVersionService.getApplicationVersionModel(applicationVersionId, ApplicationVersion.class);
  }

  @PreAuthorize("hasAccessToApplicationVersion(#applicationVersionId) or hasRole('ROLE_ADMIN')")
  @ApiOperation(value = "Download the application version", notes = "Adds the file to the HTTP response.  The content-disposition is set to attachment and the filename is the URL to the application. Also, the response body is the temporary, signed URL to the application installation file.", httpMethod = "GET", responseClass = "string")
  @ApiError(code = 500, reason = "Process error")
  @RequestMapping(value = "/download/{applicationVersionId}", method = RequestMethod.GET)
  public
  @ResponseBody
  String downloadApplicationVersion(HttpServletRequest request, HttpServletResponse response,
                                    @ApiParam(name = "applicationVersionId", value = "The application version ID", required = true, internalDescription = "java.lang.Long")
                                    @PathVariable String applicationVersionId, UserAgentInfo userAgentInfo) {
    checkRequiredEntity(applicationVersionService, Long.parseLong(applicationVersionId));

    String url = applicationController.addApplicationToResponse(response, applicationVersionId, userAgentInfo, true);

    applicationController.subscribeUserAndCreateStatistic(Long.valueOf(applicationVersionId), request);

    return url;
  }

  @PreAuthorize("hasAccessToApplicationVersion(#applicationVersionId) or hasRole('ROLE_ADMIN')")
  @ApiOperation(value = "Download the application version for iOS", notes = "Returns a property with the key 'URL' and a value in the form 'itms-services://?action=download-manifest&url='. Also, the URL is a temporary, signed URL to the application installation file.", httpMethod = "GET", responseClass = "string")
  @ApiError(code = 500, reason = "Process error")
  @RequestMapping(value = "/download/ios/{applicationVersionId}", method = RequestMethod.GET)
  public
  @ResponseBody
  Property downloadApplicationVersionIOS(HttpServletRequest request, HttpServletResponse response,
                                         @ApiParam(name = "applicationVersionId", value = "The application version ID", required = true, internalDescription = "java.lang.Long")
                                         @PathVariable String applicationVersionId, UserAgentInfo userAgentInfo) throws TokenException {
    checkRequiredEntity(applicationVersionService, Long.valueOf(applicationVersionId));

    String sessionId = request.getSession(false).getId();

    SingleUseToken token = new SingleUseToken(sessionId);
    singleUseTokenRepository.putToken(token);

    int index = request.getRequestURL().indexOf(request.getServletPath());
    String newContext = "/ios/downloadIOSPlist/" + applicationVersionId + "/" + token.getSessionIdHash();
    StringBuffer url = request.getRequestURL().replace(index, request.getRequestURL().length(), newContext);
    applicationController.subscribeUserAndCreateStatistic(Long.valueOf(applicationVersionId), request);

    String itmsURL = "itms-services://?action=download-manifest&url=" + url.toString();

    Property property = new Property();
    property.setKey("URL");
    property.setValue(itmsURL);

    return property;
  }

  @PreAuthorize("canEditApplication(#applicationId) or hasRole('ROLE_ADMIN')")
  @ApiOperation(value = "Create a new application version", notes = "Add a new version to the application with this ID", httpMethod = "POST", responseClass = "String")
  @ApiError(code = 500, reason = "Process error")
  @RequestMapping(method = RequestMethod.POST)
  public
  @ResponseBody
  Result createApplicationVersion(HttpServletRequest request,
                                  @ApiParam(name = "applicationId", value = "The application ID", required = true, internalDescription = "java.lang.Long") @RequestParam("applicationId") Long applicationId,
                                  @ApiParam(name = "versionName", value = "The version name (ex, 0.1.1)\"", required = true, internalDescription = "java.lang.String") @RequestParam("versionName") String versionName,
                                  @ApiParam(name = "recentChanges", value = "Recent changes included in this application version", required = true, internalDescription = "java.lang.String") @RequestParam("recentChanges") String recentChanges,
                                  @ApiParam(name = "appState", value = "The visable state of the application", required = true, allowableValues = "GROUP_PUBLISH, ORGANIZATION_PUBLISH, DISABLED, ORG_PUBLISH_REQUEST, RESIGNING", internalDescription = "java.lang.String") @RequestParam("appState") String appstate,
                                  @ApiParam(name = "installationFile", value = "The installation file", required = true, internalDescription = "org.springframework.web.multipart.MultipartFile") @RequestParam("installationFile") MultipartFile installationFile
                                 ) throws IOException {
    checkRequiredEntity(applicationService, applicationId);
    //  Application application = applicationService.get(applicationId);

    File appArchive = new File(installationFile.getOriginalFilename());
    Files.write(installationFile.getBytes(), appArchive);
    Manifest manifest = ManifestFactory.getInstance(appArchive, versionName);

    ApplicationVersionForm applicationVersionForm = new ApplicationVersionForm();
    applicationVersionForm.setParentId(applicationId);
    applicationVersionForm.setAppFile(installationFile);
    applicationVersionForm.setAppState(AppState.valueOf(appstate));
    applicationVersionForm.setEditing(false);
    applicationVersionForm.setRecentChanges(recentChanges);
    applicationVersionForm.setVersionName(manifest.getVersionName());
    ApplicationVersion applicationVersion = applicationVersionService.getApplicationVersionModel(applicationVersionService.saveApplicationVersion(applicationVersionForm).getId(), ApplicationVersion.class);

    Result result = new Result();
    result.setResult(true);
    result.setValue(applicationVersion);

    return result;
  }

}
