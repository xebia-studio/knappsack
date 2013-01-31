package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.services.ExcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class ExcelController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("excelService")
    @Autowired(required = true)
    private ExcelService excelService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/exportOrganizationsForDateRange")
    public void exportOrganizationsForDateRange(HttpServletResponse response, @RequestParam(required = false) String minDate, @RequestParam(required = false) String maxDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date tmpMinDate = null;
        Date tmpMaxDate = null;
        if (StringUtils.hasText(minDate)) {
            try {
                tmpMinDate = sdf.parse(minDate);
            } catch (ParseException e) {
                log.error(String.format("Error parsing to date: %s", minDate), e);
            }
        }
        if (StringUtils.hasText(maxDate)) {
            try {
                tmpMaxDate = sdf.parse(maxDate);
            } catch (ParseException e) {
                log.error(String.format("Error parsing to date: %s", maxDate), e);
            }
        }

        response.setContentType("application/ms-excel");
        response.setHeader("Expires:", "0"); // eliminates browser caching

        String fileName = String.format("Organization_Details_%s-%s", (tmpMinDate == null ? "start" : sdf.format(tmpMinDate)), (tmpMaxDate == null ? "present" : sdf.format(tmpMaxDate)));
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
        response.addCookie(new Cookie("fileDownloadTime", Long.toString(new Date().getTime())));

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            if (outputStream != null) {
                excelService.createOrganizationDetailsForDateRangeReport(tmpMinDate, tmpMaxDate, outputStream);

                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("Error writing excel export to response.", e);
        }
    }

}
