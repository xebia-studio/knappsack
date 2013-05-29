package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.components.services.InvitationControllerService;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.validators.BatchInvitationValidator;
import com.sparc.knappsack.components.validators.InvitationValidator;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.BatchInvitationForm;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.forms.InviteeForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.Contact;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.util.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

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

    @Qualifier("batchInvitationValidator")
    @Autowired(required = true)
    private BatchInvitationValidator batchInvitationValidator;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

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

    @PreAuthorize("isDomainAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/inviteUser", method = RequestMethod.GET)
    public String inviteUser(Model model, @RequestParam(value = "grp", required = false) Long groupId) {

        User user = userService.getUserFromSecurityContext();

        if (!model.containsAttribute("invitationForm")) {
            InvitationForm invitationForm = new InvitationForm();
            if (groupId != null && groupId > 0) {
                invitationForm.getGroupIds().add(groupId);
                invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);
                model.addAttribute("isGroupInvite", true);
            }
            model.addAttribute("invitationForm", invitationForm);
        }

        model.addAttribute("organizationUserRoles", UserRole.getAllForDomainType(DomainType.ORGANIZATION));
        model.addAttribute("groupUserRoles", UserRole.getAllSelectableForDomainType(DomainType.GROUP));
        model.addAttribute("groups", userService.getAdministeredGroupModels(user, GroupModel.class, SortOrder.ASCENDING));

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
        result.setValue(idsSuccesfullyDeleted);

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
                List<Invitation> invitations = new ArrayList<Invitation>();
                invitations.add(invitation);
                invitationControllerService.sendInvitation(invitations, false);
            } else {
                idsFailure.add(id);
            }
        }

        Result result = new Result();
        result.setValue(idsFailure);
        if (idsFailure.size() > 0) {
            result.setResult(false);
        } else {
            result.setResult(true);
        }

        return result;
    }


    @PreAuthorize("isDomainAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/sendInvitation", method = RequestMethod.POST)
    public String sendInvitation(Model model, @ModelAttribute("invitationForm") @Validated InvitationForm invitationForm, BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.invitationForm", bindingResult);
            redirectAttributes.addFlashAttribute("invitationForm", invitationForm);
            return "redirect:/manager/inviteUser";
        }

        boolean invitationSent = invitationControllerService.inviteUser(invitationForm, true);

        redirectAttributes.addFlashAttribute("emailError", !invitationSent);
        return "redirect:/manager/inviteUser";
    }

    @PreAuthorize("isDomainAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/batchInvitations", method = RequestMethod.POST)
    public String batchInvitations(Model model, @RequestParam(value = "contactsGmail", required = false) MultipartFile contactsGmail, @RequestParam(value = "contactsOutlook", required = false) MultipartFile contactsOutlook, final RedirectAttributes redirectAttributes) {
        if (!model.containsAttribute("batchInvitationForm")) {

            if ((contactsGmail == null || contactsGmail.isEmpty()) && (contactsOutlook == null || contactsOutlook.isEmpty())) {
                redirectAttributes.addFlashAttribute("batchCsvParseError", true);
                return "redirect:/manager/inviteUser";
            }

            Set<Contact> contacts = new HashSet<Contact>();
            if (contactsGmail != null && !contactsGmail.isEmpty()) {
                contacts.addAll(invitationService.parseContactsGoogle(contactsGmail));
            }

            if (contactsOutlook != null && !contactsOutlook.isEmpty()) {
                contacts.addAll(invitationService.parseContactsOutlook(contactsOutlook));
            }

            BatchInvitationForm batchInvitationForm = new BatchInvitationForm();

            Errors errors = new BeanPropertyBindingResult(batchInvitationForm, "batchInvitationForm");
            Set<Contact> validContacts = new HashSet<Contact>();
            for (Contact contact : contacts) {
                boolean isValid = invitationValidator.isValidContact(contact, errors);
                if (isValid) {
                    validContacts.add(contact);
                }
            }

            model.addAttribute("org.springframework.validation.BindingResult.batchInvitationForm", errors);

            batchInvitationForm.getContacts().addAll(new ArrayList<Contact>(validContacts));

            model.addAttribute("batchInvitationForm", batchInvitationForm);
        }

        User user = userService.getUserFromSecurityContext();

        model.addAttribute("organizationUserRoles", UserRole.getAllSelectableForDomainType(DomainType.ORGANIZATION));
        model.addAttribute("groups", userService.getAdministeredGroupModels(user, GroupModel.class, SortOrder.ASCENDING));

//        if (bindingResult.hasErrors()) {
//            return inviteUser(model, invitationForm.getDomainId());
//        }
//        Domain domain = domainService.get(invitationForm.getDomainId());
//        if (domain == null) {
//            throw new EntityNotFoundException(String.format("Domain entity not found while view batch invites: %s", invitationForm.getDomainId()));
//        }
//        initModel(model, domain);
//        if (!model.containsAttribute("invitationForm"))
//            invitationForm.getInviteeForms().clear();
//        invitationForm.setDomainId(invitationForm.getDomainId());
//        invitationForm.setDomainType(invitationForm.getDomainType());
//        List<InviteeForm> googleInviteeForms = invitationService.parseContactsGoogle(invitationForm.getContactsGmail());
//        for (InviteeForm inviteeForm : googleInviteeForms) {
//            invitationForm.getInviteeForms().add(inviteeForm);
//        }
//
//        List<InviteeForm> outlookInviteeForms = invitationService.parseContactsOutlook(invitationForm.getContactsOutlook());
//        for (InviteeForm inviteeForm : outlookInviteeForms) {
//            invitationForm.getInviteeForms().add(inviteeForm);
//        }
//        List<InviteeForm> inviteeForms = invitationForm.getInviteeForms();
//        List<InviteeForm> validatedInviteeForms = new ArrayList<InviteeForm>();
//        for (InviteeForm inviteeForm : inviteeForms) {
//            boolean isValid = invitationValidator.isValidInviteeForm(inviteeForm, invitationForm.getDomainId(), invitationForm.getDomainType(), bindingResult);
//            inviteeForm.setDelete(!isValid);
//            if (isValid) {
//                validatedInviteeForms.add(inviteeForm);
//            }
//        }
//        invitationForm.setInviteeForms(validatedInviteeForms);

        return "manager/inviteBatchUsersTH";
    }

    @PreAuthorize("isDomainAdmin() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/sendBatchInvitations", method = RequestMethod.POST)
    public String sendBatchInvitations(Model model, @ModelAttribute("batchInvitationForm") BatchInvitationForm batchInvitationForm, BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        // Remove deleted contacts
        Iterator<Contact> iterator = batchInvitationForm.getContacts().iterator();
        while (iterator.hasNext()) {
            Contact contact = iterator.next();
            if (contact == null || !StringUtils.hasText(contact.getEmail())) {
                iterator.remove();
            }
        }
        int originalContactsSize = CollectionUtils.isEmpty(batchInvitationForm.getContacts()) ? 0 : batchInvitationForm.getContacts().size();

        List<Contact> invalidContacts = new ArrayList<Contact>();
        ValidationUtils.invokeValidator(batchInvitationValidator, batchInvitationForm, bindingResult);
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                if (error.getRejectedValue() instanceof Contact) {
                    invalidContacts.add((Contact) error.getRejectedValue());
                    batchInvitationForm.getContacts().remove(error.getRejectedValue());
                }
            }

            model.addAttribute("containsErrors", true);
        }

        boolean invitationsSent = false;
        if (!CollectionUtils.isEmpty(batchInvitationForm.getContacts())) {
            invitationsSent = invitationControllerService.inviteBatchUsers(batchInvitationForm, true);
        }

        if (invitationsSent && originalContactsSize == batchInvitationForm.getContacts().size()) {
            redirectAttributes.addFlashAttribute("emailError", false);
            return "redirect:/manager/inviteUser";
        } else {
            if (invitationsSent) {
                batchInvitationForm.getContacts().clear();
            }
            batchInvitationForm.getContacts().addAll(invalidContacts);
            return batchInvitations(model, null, null, redirectAttributes);
        }
    }
}
