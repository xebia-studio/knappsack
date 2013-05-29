package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDeliveryWithCompositeFactory;
import com.sparc.knappsack.components.events.composits.ApplicationVersionResignCompleteComposite;
import com.sparc.knappsack.components.events.composits.EventDeliveryWithComposite;
import com.sparc.knappsack.components.services.ApplicationVersionControllerService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.ResignErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/resigner")
public class ResignerController extends AbstractController {

    @Qualifier("applicationVersionControllerService")
    @Autowired(required = true)
    private ApplicationVersionControllerService applicationVersionControllerService;

    @Qualifier("eventDeliveryWithCompositeFactory")
    @Autowired(required = true)
    private EventDeliveryWithCompositeFactory eventDeliveryWithCompositeFactory;

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @RequestMapping(value = "/webhook/{id}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void handleEvent(@PathVariable Long id, @RequestParam(value = "success", required = true) boolean success, @RequestParam(value = "appState", required = true) AppState requestedAppState, @RequestParam(value = "user", required = true) String user, @RequestParam(value = "errorType", required = false) ResignErrorType resignErrorType) {

        ApplicationVersion applicationVersion = applicationVersionService.get(id);
        User initiationUser = userService.getByEmail(user);

        if (applicationVersion != null) {
            EventDeliveryWithComposite deliveryMechanism = eventDeliveryWithCompositeFactory.getEventDelivery(EventType.APPLICATION_VERSION_RESIGN_COMPLETE);
            if (deliveryMechanism != null) {
                if (!success && resignErrorType == null) {
                    resignErrorType = ResignErrorType.GENERIC;
                }
                deliveryMechanism.sendNotifications(applicationVersion, new ApplicationVersionResignCompleteComposite(success, initiationUser, resignErrorType));
            }

            if (success) {
                applicationVersionControllerService.updateApplicationVersionState(id, requestedAppState, true);
            } else {

                // TODO: refactor to send notifications whenever proper audit trail is in place so that admins being notified know why the application version is in the error state.
                applicationVersionControllerService.updateApplicationVersionState(id, AppState.ERROR, false);
            }
        }

    }

}
