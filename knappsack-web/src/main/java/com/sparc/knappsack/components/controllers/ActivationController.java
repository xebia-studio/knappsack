package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.forms.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/activate")
public class ActivationController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ActivationController.class);

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @RequestMapping(method = RequestMethod.GET)
    public String showActivatePage(Model model, @RequestParam(required = false) Boolean error) {
        User user = userService.getUserFromSecurityContext();

        if (user.isActivated()) {
            return "redirect:/home";
        }

        if (user.isActivated()) {
            model.addAttribute("activated", true);
        } else {
            model.addAttribute("activated", false);
        }

        if (error != null) {
            model.addAttribute("error", error);
        }

        return "activateTH";
    }

    @RequestMapping(value = "/{code}", method = RequestMethod.GET)
    public String activate(@PathVariable String code, final RedirectAttributes redirectAttributes) {
        User user = userService.getUserFromSecurityContext();

        if (user.isActivated()) {
            return "redirect:/home";
        }

        if (userService.activate(user.getId(), code)) {
            userService.updateSecurityContext(userService.get(user.getId()));
            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.USER_ACCOUNT_ACTIVATION_SUCCESS);
            if (deliveryMechanism != null) {
                deliveryMechanism.sendNotifications(user);
            }
            redirectAttributes.addFlashAttribute("activationSuccess", true);
            return "redirect:/home";
        } else {
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/activate";
        }
    }

    @RequestMapping(value = "/sendCode", method = RequestMethod.GET)
    public @ResponseBody Result sendCode() {
        Result result = new Result();
        result.setResult(false);

        User user = userService.getUserFromSecurityContext();

        boolean success = false;

        EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.USER_ACCOUNT_ACTIVATION);
        if (deliveryMechanism != null) {
            success = deliveryMechanism.sendNotifications(user);
        }

        result.setResult(success);

        return result;
    }

}
