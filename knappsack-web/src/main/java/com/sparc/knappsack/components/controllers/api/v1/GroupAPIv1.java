package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.models.api.v1.Application;
import com.sparc.knappsack.models.api.v1.Group;
import com.sparc.knappsack.util.UserAgentInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/api/v1/groups")
@Api(value = "Group operations", listingClass = "GroupAPIv1", basePath = "/api/v1/groups", description = "All operations for groups")
public class GroupAPIv1 extends BaseAPIv1Controller {

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("groupService")
    @Autowired
    private GroupService groupService;

    @Qualifier("applicationService")
    @Autowired
    private ApplicationService applicationService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Find all groups for active organization", notes = "Get all groups applicable to the user for the users active organization", httpMethod = "GET", responseClass = "Group", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Group[] getGroups() {
        User user = userService.getUserFromSecurityContext();
        List<Group> groupModels = userService.getGroupModelsForActiveOrganization(user, Group.class, SortOrder.ASCENDING);

        return groupModels.toArray(new Group[groupModels.size()]);
    }

    @PreAuthorize("isUserInDomain(#groupId)")
    @ApiOperation(value = "Find all applications for a group", notes = "This will return all applications for a group unless specified application types are given, in which case only applications that support those platforms are returned.", httpMethod = "GET", responseClass = "Application", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/{groupId}/applications", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Application[] getGroupApplications(
            @ApiParam(name = "groupId", value = "The group ID", required = true, internalDescription = "java.lang.Long")
            @PathVariable Long groupId,
            @ApiParam(name = "applicationTypes", value = "The specific platform that the applications support.  This overrides getting applications applicable for the client user agent.  For instance, set this parameter to IOS if you want to only return applications that work on an iOS device.", allowableValues = "ANDROID,IPHONE,IPAD,IOS,CHROME,FIREFOX,WINDOWSPHONE7,BLACKBERRY,OTHER", required = false, allowMultiple = true, internalDescription = "com.sparc.knappsack.enums.ApplicationType")
            @RequestParam(value = "applicationTypes", required = false) String[] applicationTypes) {
        checkRequiredEntity(groupService, groupId);
        Set<ApplicationType> applicationTypeValues = new HashSet<ApplicationType>();
        if(applicationTypes != null && applicationTypes.length > 0) {
            for (String applicationType : applicationTypes) {
                applicationTypeValues.addAll(ApplicationType.getAllChildrenDeviceTypes(ApplicationType.valueOf(applicationType)));
            }
        }

        List<Application> applications = applicationService.getApplicationModels(applicationService.getAll(groupService.get(groupId), applicationTypeValues.toArray(new ApplicationType[applicationTypeValues.size()])), Application.class);
        return applications.toArray(new Application[applications.size()]);
    }
}
