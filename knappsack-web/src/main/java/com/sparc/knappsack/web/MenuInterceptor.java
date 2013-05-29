package com.sparc.knappsack.web;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.models.MenuItem;
import com.sparc.knappsack.util.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuInterceptor extends FilteredRequestHandlerInterceptorAdapter {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(isBlackLabeledRequest(request)) {
            return;
        }

        if (request != null && modelAndView != null && (!(modelAndView.getView() instanceof RedirectView) && !modelAndView.getViewName().startsWith("redirect:"))) {
            User user = userService.getUserFromSecurityContext();
            if (user != null) {
                ApplicationType deviceType = new UserAgentInfo(request.getHeader("User-Agent"), request.getHeader("Accept")).getApplicationType();
                // Get categories for side menu
                List<Category> categories = userService.getCategoriesForUser(user, deviceType, SortOrder.ASCENDING);
                List<MenuItem> categoryMenuItems = new ArrayList<MenuItem>();
                if (categories != null) {
                    for (Category category : categories) {
                        MenuItem menuItem = new MenuItem(category.getName(), category.getId());
                        categoryMenuItems.add(menuItem);
                    }
                }
                modelAndView.getModel().put("categoryMenuItems", categoryMenuItems);

                // Get groups for side menu
                List<MenuItem> groupMenuItems = new ArrayList<MenuItem>();
                List<Group> groups = userService.getGroupsForActiveOrganization(user, SortOrder.ASCENDING);
                if (groups != null) {
                    for (Group group : groups) {
                        MenuItem groupModel = new MenuItem(group.getName(), group.getId());
                        groupMenuItems.add(groupModel);
                    }
                }
                modelAndView.getModel().put("groupMenuItems", groupMenuItems);

                // Get available device types for side menu
                List<Application> applications = userService.getApplicationsForUser(user, deviceType);
                Set<ApplicationType> deviceTypes = new HashSet<ApplicationType>();
                if (applications != null) {
                    for (Application application : applications) {
                        deviceTypes.add(application.getApplicationType());
                    }
                }
                modelAndView.getModel().put("deviceTypeMenuItems", deviceTypes);

                if (!modelAndView.getModel().containsKey("groupNavActiveIDs")) {
                    modelAndView.getModel().put("groupNavActiveIDs", new Long[]{});
                }
                if (!modelAndView.getModel().containsKey("categoryNavActiveIDs")) {
                    modelAndView.getModel().put("categoryNavActiveIDs", new Long[]{});
                }
                if (!modelAndView.getModel().containsKey("typeNavActive")) {
                    modelAndView.getModel().put("typeNavActive", new ApplicationType[]{});
                }
            }
        }
    }
}
