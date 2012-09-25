package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.comparators.ApplicationDescriptionComparator;
import com.sparc.knappsack.comparators.ApplicationNameComparator;
import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.SearchService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.models.ImageModel;
import com.sparc.knappsack.util.UserAgentInfo;
import com.sparc.knappsack.util.WebRequest;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class SearchController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    @Qualifier("searchService")
    @Autowired(required = true)
    private SearchService searchService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String displayPage(Model model, UserAgentInfo userAgentInfo) {
        User user = userService.getUserFromSecurityContext();
        List<Application> applications = userService.getApplicationsForUser(user, userAgentInfo.getApplicationType(), AppState.ORGANIZATION_PUBLISH, AppState.GROUP_PUBLISH, AppState.ORG_PUBLISH_REQUEST);

        ComparatorChain chain = new ComparatorChain();
        chain.addComparator(new ApplicationNameComparator());
        chain.addComparator(new ApplicationDescriptionComparator());

        Collections.sort(applications, chain);

        List<ApplicationModel> applicationModels = applicationService.createApplicationModels(applications);
        model.addAttribute("applications", applicationModels);

        return "searchTH";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<ApplicationModel> search(@RequestParam(value = "searchCriteria", required = true) String searchCriteria, UserAgentInfo userAgentInfo) {
        User user = userService.getUserFromSecurityContext();

        List<ApplicationModel> searchResults = new ArrayList<ApplicationModel>();
        try {
            searchResults = searchService.searchApplications(URLDecoder.decode(searchCriteria.toLowerCase(), "UTF-8"), user, userAgentInfo.getApplicationType());
            for (ApplicationModel applicationModel : searchResults) {
                ImageModel imageModel = applicationModel.getIcon();
                if (imageModel != null) {
                    imageModel.setUrl(WebRequest.getInstance().generateURL(imageModel.getUrl()));
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException caught performing a search with the following criteria: " + searchCriteria);
        }

        return searchResults;
    }
}
