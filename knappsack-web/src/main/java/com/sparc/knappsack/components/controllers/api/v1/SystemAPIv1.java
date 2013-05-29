package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.api.v1.SystemStatistics;
import com.sparc.knappsack.models.api.v1.Properties;
import com.sparc.knappsack.models.api.v1.Property;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api/v1/system")
@Api(value = "System operations", listingClass = "SystemAPIv1", basePath = "/api/v1/system", open = false)
public class SystemAPIv1 extends BaseAPIv1Controller {

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Get system statistics", notes = "Get various system statistics such as total application and user count.", httpMethod = "GET", responseClass = "Properties", multiValueResponse = false)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/statistics", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Properties viewSystemManagementPage(HttpServletRequest request) {
        Property[] properties = new Property[3];
        properties[0] = new Property(SystemStatistics.ORGANIZATION_COUNT.name(), Long.toString(organizationService.countAll()), SystemStatistics.ORGANIZATION_COUNT.getDescription());
        properties[1] = new Property(SystemStatistics.USER_COUNT.name(), Long.toString(userService.countAll()), SystemStatistics.USER_COUNT.getDescription());
        properties[2] = new Property(SystemStatistics.APPLICATION_COUNT.name(), Long.toString(applicationService.countAll()), SystemStatistics.APPLICATION_COUNT.getDescription());

        Properties props = new Properties();
        props.setProperties(properties);

        return props;
    }
}
