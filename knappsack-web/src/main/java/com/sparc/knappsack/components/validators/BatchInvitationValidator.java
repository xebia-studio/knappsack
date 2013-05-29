package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserDomainService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.BatchInvitationForm;
import com.sparc.knappsack.models.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.trimWhitespace;

@Component("batchInvitationValidator")
public class BatchInvitationValidator implements Validator {

    public static final String ORGANIZATION_USER_ROLE_FIELD = "organizationUserRole";
    public static final String GROUP_IDS_FIELD = "groupIds";
    @Qualifier("invitationValidator")
    @Autowired(required = true)
    private InvitationValidator invitationValidator;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Override
    public boolean supports(Class<?> clazz) {
        return BatchInvitationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BatchInvitationForm form = (BatchInvitationForm) target;

        User user = userService.getUserFromSecurityContext();
        if (user == null || user.getActiveOrganization() == null) {
            errors.reject("batchInvitationValidator.error.generic");
            return;
        }

        // Check if User Roles are valid
        if (form.getOrganizationUserRole() == null || !UserRole.getAllSelectableForDomainType(DomainType.ORGANIZATION).contains(form.getOrganizationUserRole())) {
            errors.rejectValue(ORGANIZATION_USER_ROLE_FIELD, "batchInvitationValidator.userRole.organization.invalid");
            return;
        } else if (UserRole.ROLE_ORG_GUEST.equals(form.getOrganizationUserRole()) && CollectionUtils.isEmpty(form.getGroupIds())) {
            errors.rejectValue(GROUP_IDS_FIELD, "batchInvitationValidator.orgGuest.groupIds.empty");
        }

        // Validate security
        validateSecurity(errors, form, user);

        if (errors.hasErrors()) {
            return;
        }

        List<Contact> validContacts = new ArrayList<Contact>(); /*List of valid contacts*/
        List<Contact> invalidContacts = new ArrayList<Contact>(); /*List of invalid contacts*/

        for (int index = 0; index < form.getContacts().size(); index++) {
            Contact contact = form.getContacts().get(index);

            Errors contactErrors = new BeanPropertyBindingResult(form, "batchInvitationForm");
            contact.setEmail(trimWhitespace(contact.getEmail()));

            // Validate email
            invitationValidator.validateEmail(contact.getEmail(), contactErrors);

            if (contactErrors.hasErrors()) {
                copyErrors(contactErrors, errors, String.format("contacts[%s]", index));
                invalidContacts.add(contact);
                continue;
            } else {
                validContacts.add(contact);
            }
        }

        validateOrganization(errors, form, user, validContacts, invalidContacts);

        validateGroups(errors, form, validContacts, invalidContacts);
    }

    private void validateSecurity(Errors errors, BatchInvitationForm form, User user) {
        if (form.getOrganizationUserRole() != null && UserRole.getAllSelectableForDomainType(DomainType.ORGANIZATION).contains(form.getOrganizationUserRole()) && !user.isActiveOrganizationAdmin()) {
            errors.reject(ORGANIZATION_USER_ROLE_FIELD, "batchInvitationValidator.security.nonOrgAdmin");
        } else if (!CollectionUtils.isEmpty(form.getGroupIds())) {
            List<Group> administeredGroups = userService.getAdministeredGroups(user, null);

            // Check if the user has admin access to any groups
            if (CollectionUtils.isEmpty(administeredGroups)) {
                errors.rejectValue(GROUP_IDS_FIELD, "invitationValidator.groupIds.invalid");
            } else {
                for (Long groupId : form.getGroupIds()) {
                    // Check if groupId belongs in List of groups which the user is an admin
                    if (!invitationValidator.doesGroupListContainGroupWithId(administeredGroups, groupId)) {
                        errors.rejectValue(GROUP_IDS_FIELD, "batchInvitationValidator.security.group.invalid");
                        break;
                    }
                }
            }
        }
    }

    private void validateOrganization(Errors errors, BatchInvitationForm form, User user, List<Contact> validContacts, List<Contact> invalidContacts) {
        // Check if inviting to organization and if so check if invitations already exists

        if (form.getOrganizationUserRole() != null && UserRole.getAllSelectableForDomainType(DomainType.ORGANIZATION).contains(form.getOrganizationUserRole()) && !UserRole.ROLE_ORG_GUEST.equals(form.getOrganizationUserRole())) {
            Set<String> emails = getContactEmails(validContacts);

            // Check if any of the provided emails have existing invitations for the organization
            List<Long> organizationIds = new ArrayList<Long>();
            organizationIds.add(user.getActiveOrganization().getId());
            List<Invitation> invitations = invitationService.getAllForEmailsAndDomains(new ArrayList<String>(emails), organizationIds);
            for (Invitation invitation : invitations) {
                Contact invalidContact = null;
                for (Contact contact : validContacts) {

                    // If invitation exists create error for contact, add contact to invalidContacts list and remove from validContacts list
                    if (contact.getEmail().equalsIgnoreCase(invitation.getEmail())) {
                        errors.rejectValue(String.format("contacts[%s]", form.getContacts().indexOf(contact)), "batchInvitationValidator.organization.invitationExists", new Object[]{contact.getEmail(), user.getActiveOrganization().getName()}, "");
                        invalidContacts.add(contact);
                        invalidContact = contact;
                        break;
                    }
                }

                // If invalidContact is not null then remove from validContacts list
                if (invalidContact != null) {
                    validContacts.remove(invalidContact);
                }
            }

            // Check if user already belongs to organization
            emails = getContactEmails(validContacts);
            List<Long> organizationId = new ArrayList<Long>();
            organizationId.add(user.getActiveOrganization().getId());
            List<UserDomain> userDomains = userDomainService.getUserDomainsForEmailsAndDomains(new ArrayList<String>(emails), organizationId);
            for (UserDomain userDomain : userDomains) {
                Contact invalidContact = null;
                for (Contact contact : validContacts) {

                    // If userDomain exists for contact
                    if (contact.getEmail().equalsIgnoreCase(userDomain.getUser().getEmail())) {
                        errors.rejectValue(String.format("contacts[%s]", form.getContacts().indexOf(contact)), "batchInvitationValidator.organization.userExists", new Object[]{contact.getEmail(), user.getActiveOrganization().getName()}, "");
                        invalidContacts.add(contact);
                        invalidContact = contact;
                        break;
                    }
                }

                // If invalidContact is not null then remove from validContacts list
                if (invalidContact != null) {
                    validContacts.remove(invalidContact);
                }
            }

        }
    }

    private void validateGroups(Errors errors, BatchInvitationForm form, List<Contact> validContacts, List<Contact> invalidContacts) {
        // Check if inviting to a group and if so check if invitations already exist for groups
        if (!CollectionUtils.isEmpty(form.getGroupIds())) {
            Set<String> emails = getContactEmails(validContacts);

            List<Invitation> invitations = invitationService.getAllForEmailsAndDomains(new ArrayList<String>(emails), form.getGroupIds());
            for (Invitation invitation : invitations) {
                Contact invalidContact = null;

                for (Contact contact : validContacts) {

                    // If invitation exists create error for contact, add contact to invalidContacts list and remove from validContacts list
                    if (contact.getEmail().equalsIgnoreCase(invitation.getEmail())) {
                        errors.rejectValue(String.format("contacts[%s]", form.getContacts().indexOf(contact)), "batchInvitationValidator.group.invitationExists", new Object[]{contact.getEmail(), invitation.getDomain().getName()}, "");
                        invalidContacts.add(contact);
                        invalidContact = contact;
                        break;
                    }
                }

                // If invalidContact is not null then remove from validContacts list
                if (invalidContact != null) {
                    validContacts.remove(invalidContact);
                }
            }

            // Check if user already belongs to any of the specified groups
            emails = getContactEmails(validContacts);
            List<UserDomain> userDomains = userDomainService.getUserDomainsForEmailsAndDomains(new ArrayList<String>(emails), form.getGroupIds());
            for (UserDomain userDomain : userDomains) {
                Contact invalidContact = null;
                for (Contact contact : validContacts) {

                    // If userDomain exists for contact
                    if (contact.getEmail().equalsIgnoreCase(userDomain.getUser().getEmail())) {
                        errors.rejectValue(String.format("contacts[%s]", form.getContacts().indexOf(contact)), "batchInvitationValidator.group.userExists", new Object[]{contact.getEmail(), userDomain.getDomain().getName()}, "");
                        invalidContacts.add(contact);
                        invalidContact = contact;
                        break;
                    }
                }

                // If invalidContact is not null then remove from validContacts list
                if (invalidContact != null) {
                    validContacts.remove(invalidContact);
                }
            }
        }
    }

    private Set<String> getContactEmails(List<Contact> validContacts) {
        Set<String> emails = new HashSet<String>();
        for (Contact contact : validContacts) {
            emails.add(contact.getEmail());
        }
        return emails;
    }

    private void copyErrors(Errors src, Errors dest, String fieldPrefix) {
        for (FieldError error : src.getFieldErrors()) {
            dest.rejectValue(StringUtils.hasText(fieldPrefix) ? StringUtils.trimAllWhitespace(fieldPrefix) + "." + error.getField() : error.getField(), error.getCode());
        }
        for (ObjectError error : src.getGlobalErrors()) {
            dest.reject(error.getCode());
        }
    }
}
