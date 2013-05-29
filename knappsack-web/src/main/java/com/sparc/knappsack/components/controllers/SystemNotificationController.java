package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.comparators.SystemNotificationSeverityComparator;
import com.sparc.knappsack.comparators.SystemNotificationTypeComparator;
import com.sparc.knappsack.components.entities.SystemNotification;
import com.sparc.knappsack.components.services.SystemNotificationService;
import com.sparc.knappsack.components.validators.SystemNotificationValidator;
import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.EnumEditor;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.forms.SystemNotificationForm;
import com.sparc.knappsack.models.SystemNotificationModel;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class SystemNotificationController extends AbstractController {

    public static final Logger log = LoggerFactory.getLogger(SystemNotificationController.class);

    @Qualifier("systemNotificationService")
    @Autowired(required = true)
    private SystemNotificationService systemNotificationService;

    @Qualifier("systemNotificationValidator")
    @Autowired(required = true)
    private SystemNotificationValidator systemNotificationValidator;

    @InitBinder("systemNotificationForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(systemNotificationValidator);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
        binder.registerCustomEditor(SystemNotificationType.class, new EnumEditor(SystemNotificationType.class));
        binder.registerCustomEditor(SystemNotificationSeverity.class, new EnumEditor(SystemNotificationSeverity.class));
        binder.registerCustomEditor(List.class, "types", new CustomCollectionEditor(List.class) {
            @Override
            protected Object convertElement(Object element) {
                SystemNotificationType type = null;
                if (element != null) {
                    try {
                        type = SystemNotificationType.valueOf((String) element);
                    } catch (Exception ex) {
                        log.error(String.format("Error converting element to SystemNotificationType: %s", element), ex);
                    }
                }
                return type;
            }
        });
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/saveSystemNotification", method = RequestMethod.POST)
    public String saveSystemNotification(@ModelAttribute("systemNotificationForm") @Validated SystemNotificationForm systemNotificationForm, BindingResult bindingResult, Model model, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.systemNotificationForm", bindingResult);
            redirectAttributes.addFlashAttribute("systemNotificationForm", systemNotificationForm);
            return "redirect:/manager/system?notifUpdateSuccess=false";
        }
        SystemNotification returnedSystemNotification = null;

        //Map form to model
        SystemNotificationModel systemNotificationModel = new SystemNotificationModel();
        systemNotificationModel.setId(systemNotificationForm.getId());
        systemNotificationModel.setStartDate(systemNotificationForm.getStartDate());
        systemNotificationModel.setEndDate(systemNotificationForm.getEndDate());
        systemNotificationModel.setMessage(systemNotificationForm.getMessage());
        systemNotificationModel.setAllPages(systemNotificationForm.isAllPages());
        systemNotificationModel.setNotificationType(systemNotificationForm.getNotificationType());
        systemNotificationModel.setNotificationSeverity(systemNotificationForm.getNotificationSeverity());

        boolean success = false;

        try {
            //Check if editing or not
            if (systemNotificationModel.getId() != null && systemNotificationModel.getId() > 0) {
                returnedSystemNotification = systemNotificationService.editSystemNotification(systemNotificationModel);
                if (returnedSystemNotification != null) {
                    success = true;
                }
            } else {
                returnedSystemNotification = systemNotificationService.addSystemNotification(systemNotificationModel);
                if (returnedSystemNotification != null && returnedSystemNotification.getId() != null && returnedSystemNotification.getId() > 0) {
                    success = true;
                }
            }
        } catch (Exception ex) {
            log.info("Error updating systemNotification.", ex);
            String[] codes = {"transaction.genericError"};
            ObjectError error = new ObjectError("systemNotificationForm", codes, null, "An error occurred processing your request.  Please try again.");
            bindingResult.addError(error);

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.systemNotificationForm", bindingResult);
            redirectAttributes.addFlashAttribute("systemNotificationForm", systemNotificationForm);
        }

        return "redirect:/manager/system?notifUpdateSuccess=" + success;
    }

    @RequestMapping(value = "/manager/getAllSystemNotifications", method = RequestMethod.GET)
    public @ResponseBody
    List<SystemNotificationModel> getAllSystemNotifications() {
        return systemNotificationService.getAllModels(false);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteSystemNotification")
    public @ResponseBody
    Result deleteSystemNotification(@RequestParam(value = "id", required = true) Long id) {
        Result result = new Result();

        try {
            checkRequiredEntity(systemNotificationService, id);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete a non-existent SystemNotification: %s", id));
            result.setResult(false);
            return result;
        }

        systemNotificationService.delete(id);

        if (systemNotificationService.get(id) != null) {
            log.info(String.format("SystemNotification not deleted: %s", id));
        } else {
            result.setResult(true);
        }

        return result;
    }

    @RequestMapping(value = "/getSystemNotifications", method = RequestMethod.GET)
    public @ResponseBody List<SystemNotificationModel> getSystemNotifications(@RequestParam(value = "types[]", required = false) SystemNotificationType[] types) {
        List<SystemNotificationModel> models;
        if (types != null && types.length > 0) {
            models = systemNotificationService.getAllForTypes(true, types);
        } else {
            models = systemNotificationService.getAllModels(true);
        }

        Comparator<SystemNotificationModel> notificationTypeComparator = new BeanComparator("notificationType", new SystemNotificationTypeComparator());
        Comparator<SystemNotificationModel> notificationSeverityComparator = new BeanComparator("notificationSeverity", new SystemNotificationSeverityComparator());

        ComparatorChain chain = new ComparatorChain();
        chain.addComparator(notificationTypeComparator);
        chain.addComparator(notificationSeverityComparator);

        Collections.sort(models, chain);

        return models;
    }

}
