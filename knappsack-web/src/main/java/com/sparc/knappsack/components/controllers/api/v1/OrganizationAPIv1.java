package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.CustomBrandingService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.api.v1.Branding;
import com.sparc.knappsack.models.api.v1.Organization;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/v1/organizations")
@Api(value = "Organization operations", listingClass = "OrganizationAPIv1", basePath = "/api/v1/organizations", description = "All operations for organizations")
public class OrganizationAPIv1 extends BaseAPIv1Controller {

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("customBrandingService")
    @Autowired(required = true)
    private CustomBrandingService customBrandingService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Find all organizations", notes = "Get all organizations applicable to the user", httpMethod = "GET", responseClass = "Organization", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Organization[] getOrganizations() {
        User user = userService.getUserFromSecurityContext();
        List<Organization> organizations = userService.getOrganizationModels(user, Organization.class, SortOrder.ASCENDING);
        return organizations.toArray(new Organization[organizations.size()]);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Set active organization", notes = "Set your currently active organization ", httpMethod = "GET", responseClass = "Result", multiValueResponse = false)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/active/{organizationId}", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Result setActiveOrganization( @ApiParam(name = "organizationId", value = "The organization ID", required = true, internalDescription = "java.lang.Long") @PathVariable Long organizationId) {
        checkRequiredEntity(organizationService, organizationId);

        User user = userService.getUserFromSecurityContext();
        userService.setActiveOrganization(user, organizationId);
        Result result = new Result();
        result.setResult(true);
        result.setValue(organizationId);

        return result;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Get organization branding", notes = "Get the custom branding of the currently active organization", httpMethod = "GET", responseClass = "Branding", multiValueResponse = false)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/branding", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Branding getBranding() {
        User user = userService.getUserFromSecurityContext();

        return customBrandingService.getCustomBrandingModel(user.getActiveOrganization().getCustomBranding(), Branding.class);
    }

}
