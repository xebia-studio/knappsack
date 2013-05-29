package com.sparc.knappsack.components.controllers.api.v1;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.SearchService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.models.api.v1.Application;
import com.sparc.knappsack.util.UserAgentInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v1/search")
@Api(value = "Search operations", listingClass = "SearchAPIv1", basePath = "/api/v1/search", description = "All search operations")
public class SearchAPIv1 extends BaseAPIv1Controller {

    private static final Logger log = LoggerFactory.getLogger(SearchAPIv1.class);

    @Qualifier("searchService")
    @Autowired(required = true)
    private SearchService searchService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "Search applications", notes = "Find all applications matching the given criteria", httpMethod = "GET", responseClass = "Application", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/applications/{criteria}", method = RequestMethod.GET, produces = contentType)
    public
    @ResponseBody
    List<Application> searchApplications(
            @ApiParam(name = "criteria", value = "The search criteria", required = true, internalDescription = "java.lang.String")
            @PathVariable String criteria, UserAgentInfo userAgentInfo) {
        User user = userService.getUserFromSecurityContext();

        List<Application> searchResults = new ArrayList<Application>();
        try {
            searchResults = searchService.searchApplications(URLDecoder.decode(criteria.toLowerCase(), "UTF-8"), user, userAgentInfo.getApplicationType(), Application.class);
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException caught performing a search with the following criteria: " + criteria);
        }

        return searchResults;
    }
}
