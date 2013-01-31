package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.InvitationControllerService;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.validators.InvitationValidator;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.forms.InviteeForm;
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

import java.util.ArrayList;
import java.util.List;

@Controller
public class InvitationController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(InvitationController.class);

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("invitationControllerService")
    @Autowired(required = true)
    private InvitationControllerService invitationControllerService;

    @Qualifier("invitationValidator")
    @Autowired(required = true)
    private InvitationValidator invitationValidator;

    @Autowired(required = true)
    private EmailService emailService;

    @InitBinder("invitationForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(invitationValidator);
        binder.setBindEmptyMultipartFiles(false);
    }

    private void initModel(Model model, Domain domain) {
        model.addAttribute("domainType", domain.getDomainType().name());

        model.addAttribute("domainName", domain.getName());
        model.addAttribute("domainId", domain.getId());

        if (DomainType.ORGANIZATION.equals(domain.getDomainType())) {
            initOrgInvite(model);
        } else if (DomainType.GROUP.equals(domain.getDomainType())) {
            initGroupInvite(model);
        }
    }

    private void initGroupInvite(Model model) {
        model.addAttribute("subNav", "groupNav");

        List<UserRole> roles = new ArrayList<UserRole>();
        roles.add(UserRole.ROLE_GROUP_USER);
        roles.add(UserRole.ROLE_GROUP_ADMIN);
        model.addAttribute("userRoles", roles);
    }

    private void initOrgInvite(Model model) {
        model.addAttribute("subNav", "organizationNav");

        List<UserRole> roles = new ArrayList<UserRole>();
        roles.add(UserRole.ROLE_ORG_USER);
        roles.add(UserRole.ROLE_ORG_ADMIN);
        model.addAttribute("userRoles", roles);
    }

    @PreAuthorize("isDomainAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/inviteUser/{id}", method = RequestMethod.GET)
    public String inviteUser(Model model, @PathVariable Long id) {
        Domain domain = domainService.get(id);
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while viewing InviteUser page: %s", id));
        }
        initModel(model, domain);

        InvitationForm invitationForm = new InvitationForm();
        invitationForm.setDomainId(domain.getId());
        invitationForm.setDomainType(domain.getDomainType());
        model.addAttribute("invitationForm", invitationForm);

        return "manager/inviteUserTH";
    }


    @PreAuthorize("isDomainAdmin(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/invitesPending/{id}", method = RequestMethod.GET)
    public String invitesPending(Model model, @PathVariable Long id) {
        Domain domain = domainService.get(id);
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while viewing pending invites: %s", id));
        }
        initModel(model, domain);

        List<Invitation> invitations = invitationService.getAll(domain.getId());
        List<InviteeForm> inviteeForms = new ArrayList<InviteeForm>();

        for (Invitation invitation : invitations) {
            InviteeForm inviteeForm = new InviteeForm();
            inviteeForm.setId(invitation.getId());
            inviteeForm.setEmail(invitation.getEmail());
            inviteeForm.setUserRole(invitation.getRole().getUserRole());
            inviteeForms.add(inviteeForm);
        }
        model.addAttribute("inviteeForms", inviteeForms);
        model.addAttribute("domainId", domain.getId());
        model.addAttribute("domainType", domain.getDomainType().name());

        return "manager/inviteesTH";
    }

    @PreAuthorize("isDomainAdmin(#domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteInvitations", method = RequestMethod.POST)
    public @ResponseBody Result deleteInvite(@RequestParam Long domainId, @RequestParam(value = "invitationIds[]") List<Long> invitationIds) {
        List<Long> idsSuccesfullyDeleted = new ArrayList<Long>();

        for (Long id : invitationIds) {
            invitationService.delete(id);
            if (invitationService.get(id) == null) {
                idsSuccesfullyDeleted.add(id);
            }
        }

        Result result = new Result();
        result.setIds(idsSuccesfullyDeleted);

        if (idsSuccesfullyDeleted.size() != invitationIds.size()) {
            result.setResult(false);
        } else {
            result.setResult(true);
        }

        return result;
    }

    @PreAuthorize("isDomainAdmin(#domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/reSendInvitation", method = RequestMethod.POST)
    public @ResponseBody Result reSendInvitation(@RequestParam(value = "invitationIds[]") List<Long> invitationIds, @RequestParam Long domainId, WebRequest request) {
        List<Long> idsFailure = new ArrayList<Long>();

        for (Long id : invitationIds) {
            Invitation invitation = invitationService.get(id);
            Domain domain = null;
            if (invitation != null) {
                domain = invitation.getDomain();
            }

            if (domain != null) {
                invitationControllerService.sendInvitation(invitation, false);
            } else {
                idsFailure.add(id);
            }
        }

        Result result = new Result();
        result.setIds(idsFailure);
        if (idsFailure.size() > 0) {
            result.setResult(false);
        } else {
            result.setResult(true);
        }

        return result;
    }


    @PreAuthorize("isDomainAdmin(#invitationForm.domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/sendInvitation", method = RequestMethod.POST)
    public String sendInvitation(Model model, @ModelAttribute("invitationForm") @Validated InvitationForm invitationForm, BindingResult bindingResult, WebRequest webRequest) {
        Domain domain = domainService.get(invitationForm.getDomainId());
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while sending invitation: %s", invitationForm.getDomainId()));
        }

        if (bindingResult.hasErrors()) {
            initModel(model, domain);
            return "manager/inviteUserTH";
        }

        boolean invitationSent = invitationControllerService.inviteUser(invitationForm.getInviteeForms().get(0), invitationForm.getDomainId(), true);

        model.addAttribute("emailError", !invitationSent);
        return inviteUser(model, invitationForm.getDomainId());
    }

    @PreAuthorize("isDomainAdmin(#invitationForm.domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/batchInvitations", method = RequestMethod.POST)
    public String batchInvitations(Model model, @ModelAttribute("invitationForm") InvitationForm invitationForm, BindingResult bindingResult) {
        Domain domain = domainService.get(invitationForm.getDomainId());
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while view batch invites: %s", invitationForm.getDomainId()));
        }
        initModel(model, domain);
        invitationForm.getInviteeForms().clear();
        invitationForm.setDomainId(invitationForm.getDomainId());
        invitationForm.setDomainType(invitationForm.getDomainType());
        List<InviteeForm> googleInviteeForms = invitationService.parseContactsGoogle(invitationForm.getContactsGmail());
        for (InviteeForm inviteeForm : googleInviteeForms) {
            invitationForm.getInviteeForms().add(inviteeForm);
        }

        List<InviteeForm> outlookInviteeForms = invitationService.parseContactsOutlook(invitationForm.getContactsOutlook());
        for (InviteeForm inviteeForm : outlookInviteeForms) {
            invitationForm.getInviteeForms().add(inviteeForm);
        }
        List<InviteeForm> inviteeForms = invitationForm.getInviteeForms();
        List<InviteeForm> validatedInviteeForms = new ArrayList<InviteeForm>();
        for (InviteeForm inviteeForm : inviteeForms) {
            boolean isValid = invitationValidator.isValidInviteeForm(inviteeForm, invitationForm.getDomainId(), invitationForm.getDomainType(), bindingResult);
            inviteeForm.setDelete(!isValid);
            if (isValid) {
                validatedInviteeForms.add(inviteeForm);
            }
        }
        invitationForm.setInviteeForms(validatedInviteeForms);

        return "manager/inviteBatchUsersTH";
    }

    @PreAuthorize("isDomainAdmin(#invitationForm.domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/sendBatchInvitations", method = RequestMethod.POST)
    public String sendBatchInvitations(Model model, @ModelAttribute("invitationForm") @Validated InvitationForm invitationForm, WebRequest request, BindingResult bindingResult) {
        List<InviteeForm> errors = new ArrayList<InviteeForm>();
        for (InviteeForm inviteeForm : invitationForm.getInviteeForms()) {
            if (!inviteeForm.isDelete()) {
                boolean invitationSent = invitationControllerService.inviteUser(inviteeForm, invitationForm.getDomainId(), true);
                if (!invitationSent) {
                    errors.add(inviteeForm);
                }
            }
        }
        invitationForm.getInviteeForms().clear();
        if (errors.size() > 0) {
            Domain domain = domainService.get(invitationForm.getDomainId());
            if (domain == null) {
                throw new EntityNotFoundException(String.format("Domain entity not found sending batch invites: %s", invitationForm.getDomainId()));
            }

            initModel(model, domain);
            model.addAttribute("emailError", true);
            invitationForm.getInviteeForms().addAll(errors);

            model.addAttribute("invitationForm", invitationForm);

            return "manager/inviteBatchUsersTH";
        }

        model.addAttribute("emailError", false);

        return inviteUser(model, invitationForm.getDomainId());
    }
}
