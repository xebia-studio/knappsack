package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.EventWatchService;
import com.sparc.knappsack.components.services.UserControllerService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.validators.PasswordValidator;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.forms.PasswordForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.util.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("passwordValidator")
    @Autowired(required = true)
    private PasswordValidator passwordValidator;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("userControllerService")
    @Autowired(required = true)
    private UserControllerService userControllerService;

    @Qualifier("eventWatchService")
    @Autowired(required = true)
    private EventWatchService eventWatchService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @InitBinder("passwordForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(passwordValidator);
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.GET)
    public String showChangePasswordPage(Model model, @RequestParam(value="success", required=false) Boolean success) {

        User user = userService.getUserFromSecurityContext();
        model.addAttribute("email", user.getEmail());

        if (success != null) {
            model.addAttribute("success", success);
        }

        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordForm());
        }

        return "changePasswordTH";
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public String changePassword(Model model, @ModelAttribute("passwordForm") @Validated PasswordForm passwordForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return showChangePasswordPage(model, null);
        }

        User user = userService.getUserFromSecurityContext();
        boolean success = userControllerService.changePassword(user, passwordForm.getFirstNewPassword(), false);

        userService.updateSecurityContext(user);

        return "redirect:/profile/changePassword?success=" + success;
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public
    @ResponseBody
    Result forgotPassword(WebRequest request) {
        Result result = new Result();
        result.setResult(false);

        User user = userService.getUserFromSecurityContext();
        boolean success = userControllerService.resetPassword(user);

        result.setResult(success);

        return result;
    }

    @RequestMapping(value = "/unsubscribe/{applicationId}", method = RequestMethod.GET)
    public
    @ResponseBody Result unsubscribe(@PathVariable Long applicationId) {
        Result result = new Result();

        User principal = userService.getUserFromSecurityContext();
        if (principal != null) {
            Application application = applicationService.get(applicationId);
            boolean isSubscribed = eventWatchService.doesEventWatchExist(principal, application);
            if(isSubscribed) {
                result.setResult(eventWatchService.delete(principal, applicationService.get(applicationId)));
            }
        } else {
            result.setResult(false);
        }

        return result;
    }

    @PreAuthorize("hasAccessToApplication(#applicationId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/subscribe/{applicationId}", method = RequestMethod.GET)
    public
    @ResponseBody
    Result subscribe(@PathVariable Long applicationId) {
        Result result = new Result();

        User principal = userService.getUserFromSecurityContext();
        if (principal != null) {
            Application application = applicationService.get(applicationId);
            boolean isSubscribed = eventWatchService.doesEventWatchExist(principal, application);
            if (!isSubscribed) {
                result.setResult(eventWatchService.createEventWatch(principal, application, EventType.APPLICATION_VERSION_BECOMES_AVAILABLE));
            }
        } else {
            result.setResult(false);
        }

        return result;
    }

}
