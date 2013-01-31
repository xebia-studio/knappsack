package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.services.reports.ReportService;
import com.sparc.knappsack.models.reports.DirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class ReportsController {

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private ReportService reportService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/viewReports", method = RequestMethod.GET)
    public String viewReports(Model model) {
        List<Organization> organizations = userService.getAdministeredOrganizations(userService.getUserFromSecurityContext());
        model.addAttribute("organizations", organizations);

        return "manager/reportsTH";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getGraph", method = RequestMethod.GET)
    public
    @ResponseBody
    DirectedGraph getGraph() throws IOException {
        return reportService.createGraphForAllAdministeredOrganizations();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getOrganizationGraph/{organizationId}", method = RequestMethod.GET)
    public
    @ResponseBody
    DirectedGraph getGraph(@PathVariable Long organizationId) throws IOException {
        return reportService.createGraphForOrganization(organizationId);
    }

}
