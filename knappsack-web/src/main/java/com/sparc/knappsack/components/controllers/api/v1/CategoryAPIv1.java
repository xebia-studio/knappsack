package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.CategoryService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.models.api.v1.Application;
import com.sparc.knappsack.models.api.v1.Category;
import com.sparc.knappsack.util.UserAgentInfo;
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
@RequestMapping("/api/v1/categories")
@Api(value = "Category operations", listingClass = "CategoryAPIv1", basePath = "/api/v1/categories", description = "All operations for categories")
public class CategoryAPIv1 extends BaseAPIv1Controller {

    @Qualifier("categoryService")
    @Autowired
    private CategoryService categoryService;

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Find all categories", notes = "Get all categories applicable to the user and user agent", httpMethod = "GET", responseClass = "Category", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Category[] categories(UserAgentInfo userAgentInfo) {
        User user = userService.getUserFromSecurityContext();
        List<Category> categoryModels = userService.getCategoryModelsForUser(user, userAgentInfo.getApplicationType(), Category.class, SortOrder.ASCENDING);
        return categoryModels.toArray(new Category[categoryModels.size()]);
    }

    @PreAuthorize("hasAccessToCategory(#id, #userAgentInfo.applicationType) or hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Find all applications", notes = "Get all applications belonging to this category that are accessible to the user with the given user agent", httpMethod = "GET", responseClass = "Application", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/{categoryId}/applications", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    Application[] displayApplicationsForCategory(@ApiParam(name = "categoryId", value = "The category ID", required = true, internalDescription = "java.lang.Long") @PathVariable Long categoryId, UserAgentInfo userAgentInfo) {
        checkRequiredEntity(categoryService, categoryId);

        User user = userService.getUserFromSecurityContext();
        List<Application> applicationModels = userService.getApplicationModelsForUser(user, userAgentInfo.getApplicationType(), categoryId, Application.class);
        return applicationModels.toArray(new Application[applicationModels.size()]);
    }
}
