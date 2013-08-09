package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainRequest;
import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.InviteeForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.DomainRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DomainController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(DomainController.class);

    @Autowired(required = true)
    private DomainUserRequestService requestService;

    @Autowired(required = true)
    private UserService userService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Autowired(required = true)
    private DomainUserRequestService domainUserRequestService;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Autowired(required = true)
    private DomainRequestService domainRequestService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Autowired(required = true)
    private InvitationControllerService invitationControllerService;

    @Autowired(required = true)
    private RoleService roleService;

    @RequestMapping(value = "/domain/requestAccess", method = RequestMethod.GET)
    public String showRequestAccessPage() {
        return "domain_accessTH";
    }

    @RequestMapping(value = "/domain/requestAccess/{accessCode}", method = RequestMethod.POST)
    public String requestAccess(Model model, @PathVariable String accessCode) {
        boolean success = false;

        User user = userService.getUserFromSecurityContext();
        DomainUserRequest domainUserRequest = domainUserRequestService.createDomainUserRequest(user, accessCode);

        if (domainUserRequest != null && domainUserRequest.getId() != null && domainUserRequest.getId() > 0) {
            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.DOMAIN_USER_ACCESS_REQUEST);
            if (deliveryMechanism != null) {
                success = deliveryMechanism.sendNotifications(domainUserRequest);
            }
            if (!success) {
                log.error("Error sending DomainAccessRequest email.", domainUserRequest);
                requestService.delete(domainUserRequest.getId());
            }
        }

        model.addAttribute("success", success);

        return showRequestAccessPage();
    }

    @PreAuthorize("canEditDomainUserRequest(#requestId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/userRequest", method = RequestMethod.POST)
    public
    @ResponseBody
    Result userRequest(@RequestParam Long requestId, @RequestParam boolean status, @RequestParam(required = false) UserRole userRole) {
        Result result = new Result();
        result.setResult(false);

        boolean success = false;
        DomainUserRequest domainUserRequest = requestService.get(requestId);
        User user = userService.getUserFromSecurityContext();

        if (domainUserRequest != null && domainUserRequest.getDomain() != null
                && (user.isSystemAdmin()
                || userDomainService.get(user, domainUserRequest.getDomain().getId(), UserRole.ROLE_GROUP_ADMIN) != null
                || userDomainService.get(user, domainUserRequest.getDomain().getId(), UserRole.ROLE_ORG_ADMIN) != null)) {

            if (status) {
                success = requestService.acceptRequest(domainUserRequest, userRole);
            } else {
                success = requestService.declineRequest(domainUserRequest);
            }

        }

        result.setResult(success);

        return result;
    }

    @PreAuthorize("(canEditDomainRequest(#requestId) and isDomainAdmin(#domainIds)) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/acceptDomainRequest", method = RequestMethod.POST)
    public
    @ResponseBody
    Result acceptDomainRequest(@RequestParam Long requestId, @RequestParam(value = "domainIds[]") List<Long> domainIds) {
        Result result = new Result();
        result.setResult(false);
        boolean success = false;

        DomainRequest domainRequest = domainRequestService.get(requestId);
        for (Long domainId : domainIds) {
            Domain assignedDomain = domainService.get(domainId);
            InviteeForm inviteeForm = new InviteeForm();
            inviteeForm.setEmail(domainRequest.getEmailAddress());
            inviteeForm.setName(domainRequest.getFirstName() + " " + domainRequest.getLastName());
            UserRole userRole = DomainType.GROUP.equals(assignedDomain.getDomainType()) ? UserRole.ROLE_GROUP_USER : UserRole.ROLE_ORG_USER;
            inviteeForm.setUserRole(userRole);
            success = invitationControllerService.inviteUserToDomain(domainRequest.getEmailAddress(), domainId, userRole, false);
        }

        domainRequestService.delete(requestId);

        result.setResult(success);

        return result;
    }

    @PreAuthorize("canEditDomainRequest(#requestId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/declineDomainRequest", method = RequestMethod.POST)
    public
    @ResponseBody
    Result declineDomainRequest(@RequestParam Long requestId) {
        Result result = new Result();
        result.setResult(false);
        boolean success = false;

        domainRequestService.delete(requestId);
        success = true;

        result.setResult(success);

        return result;
    }

    @PreAuthorize("isDomainAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/requestsPending/{id}", method = RequestMethod.GET)
    public String requestsPending(@PathVariable Long id, Model model) {
        Domain domain = domainService.get(id);
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while viewing pending invites: %s", id));
        }
        model.addAttribute("domainType", domain.getDomainType().name());
        model.addAttribute("domainName", domain.getName());
        model.addAttribute("domainId", domain.getId());

        List<DomainRequestModel> domainRequestModels = domainRequestService.getAllDomainRequestModelsForDomain(id);
        model.addAttribute("domainRequests", domainRequestModels);

        return "manager/requestsTH";
    }

}
