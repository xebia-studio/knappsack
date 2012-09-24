package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.components.events.EventDeliveryWithCompositeFactory;
import com.sparc.knappsack.components.services.RegistrationService;
import com.sparc.knappsack.components.services.UserControllerService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.validators.RegistrationValidator;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.LoginError;
import com.sparc.knappsack.forms.RegistrationForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.UserModel;
import com.sparc.knappsack.security.NormalizedOpenIdAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class LoginLogoutController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(LoginLogoutController.class);

    @Qualifier("registrationService")
    @Autowired(required = true)
    private RegistrationService registrationService;

    @Qualifier("registrationValidator")
    @Autowired(required = true)
    private RegistrationValidator registrationValidator;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("userControllerService")
    @Autowired(required = true)
    private UserControllerService userControllerService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Qualifier("eventDeliveryWithCompositeFactory")
    @Autowired(required = true)
    private EventDeliveryWithCompositeFactory eventDeliveryWithCompositeFactory;

    private static final String SPRING_SECURITY_LAST_EXCEPTION = "SPRING_SECURITY_LAST_EXCEPTION";

    @InitBinder("registrationForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(registrationValidator);
    }

    /**
     * Handles and retrieves the login JSP page
     *
     * @return the name of the JSP page
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String showLoginPage(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "error", required = false) boolean error, @RequestParam(value = "registrationSuccess", required = false) boolean success, ModelMap model) {

        log.debug("Received request to show login page");

        invalidateSession(request, response);

//        request.getSession(false).removeAttribute("continueAttribute");

        if (error) {
            model.put("error", generateLoginErrorMessage(request.getSession(false)));
        }

        if (success) {
            model.put("registrationSuccess", true);
        }

        model.addAttribute("activeTab", "login");
        model.addAttribute("registrationForm", new RegistrationForm());

        return "loginTH";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationPage(HttpServletRequest request, HttpServletResponse response, Model model, @RequestParam(required = false) String email) {

        invalidateSession(request, response);

        if (!model.containsAttribute("registrationForm")) {
            RegistrationForm registrationForm = new RegistrationForm();
            model.addAttribute("registrationForm", registrationForm);

            registrationForm.setEmail(email);

            HttpSession session = request.getSession(false);
            if (session != null) {
                Object attributes = session.getAttribute("openid");
                if (attributes != null && attributes instanceof NormalizedOpenIdAttributes) {
                    registrationForm.setFirstName(((NormalizedOpenIdAttributes) attributes).getFirstName());
                    registrationForm.setLastName(((NormalizedOpenIdAttributes) attributes).getLastName());
                    registrationForm.setEmail(((NormalizedOpenIdAttributes) attributes).getEmailAddress());
                }
            }
        }

        model.addAttribute("activeTab", "register");

        return "loginTH";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, HttpServletRequest request, HttpServletResponse response, @ModelAttribute("registrationForm") @Valid RegistrationForm registrationForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return showRegistrationPage(request, response, model, null);
        }

        invalidateSession(request, response);

        UserModel userModel = new UserModel();
        userModel.setEmail(registrationForm.getEmail().trim());
        userModel.setFirstName(registrationForm.getFirstName().trim());
        userModel.setLastName(registrationForm.getLastName().trim());
        userModel.setPassword(registrationForm.getFirstPassword().trim());

        User user = registrationService.registerUser(userModel, false);
        if (user != null && user.getId() != null && user.getId() > 0) {
            try {
                EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.USER_ACCOUNT_ACTIVATION);
                if (deliveryMechanism != null) {
                    deliveryMechanism.sendNotifications(user);
                }
            } catch (MailException e) {
                log.error("Error sending Activation email to " + user.getEmail() + ".", e);
                String[] codes = {"registration.emailException"};
                ObjectError error = new ObjectError("registrationForm", codes, null, null);
                bindingResult.addError(error);
            }
            return "redirect:/auth/login?registrationSuccess=true";
        }

        String[] codes = {"registration.failure"};

        ObjectError error = new ObjectError("registrationForm", codes, null, null);
        bindingResult.addError(error);

        return showRegistrationPage(request, response, model, null);
    }

    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        invalidateSession(request, response);

        return "redirect:/auth/login";
    }

    /**
     * Handles and retrieves the denied JSP page. This is shown whenever a regular user
     * tries to access an admin only page.
     *
     * @return the name of the JSP page
     */
    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        log.info("received request to show denied page");

        // This will resolve to /WEB-INF/jsp/deniedpage.jsp
        return "deniedpage";
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
    public String viewForgotPasswordPage(HttpServletRequest request, Model model) {
        model.addAttribute("activeTab", "forgotPassword");
        model.addAttribute("registrationForm", new RegistrationForm());

        return "loginTH";
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Result forgotPassword(HttpServletRequest request, HttpServletResponse response, @RequestParam String email) {
//        invalidateSession(request, response);

        Result result = new Result();
        result.setResult(false);

        User user = userService.getByEmail(email);

        boolean success = userControllerService.resetPassword(user);

        result.setResult(success);

        return result;
    }

    private LoginError generateLoginErrorMessage(HttpSession session) {
        if (session != null) {
            Exception exception = (Exception) session.getAttribute(SPRING_SECURITY_LAST_EXCEPTION);
            if (exception != null) {
                if (exception instanceof BadCredentialsException) {
                    return LoginError.INVALID_CREDENTIALS;
                } else {
                    return LoginError.OTHER;
                }
            }
        }

        return null;
    }

    private void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (request != null && response != null && authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String cookieName = "SPRING_SECURITY_REMEMBER_ME_COOKIE";
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setMaxAge(0);
            cookie.setPath(StringUtils.hasLength(request.getContextPath()) ? request.getContextPath() : "/");

            response.addCookie(cookie);

            HttpSession session = request.getSession(false);
            if (session != null) {
                Object savedRequest = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
                Object lastException = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");

                log.debug("Invalidating session: " + session.getId());
                session.invalidate();

                if (savedRequest != null && savedRequest instanceof SavedRequest) {
                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("SPRING_SECURITY_SAVED_REQUEST", savedRequest);
                }

                if (lastException != null && lastException instanceof AuthenticationException) {
                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", lastException);
                }
            }

            SecurityContextHolder.clearContext();

        }
    }
}
