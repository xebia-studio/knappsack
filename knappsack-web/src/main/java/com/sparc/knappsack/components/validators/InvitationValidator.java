package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.DomainConfiguration;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.models.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimWhitespace;

@Component("invitationValidator")
public class InvitationValidator implements Validator {

    private static final Logger log = LoggerFactory.getLogger(InvitationValidator.class);
    public static final String EMAIL_FIELD = "email";
    public static final String ORGANIZATION_USER_ROLE_FIELD = "organizationUserRole";
    public static final String GROUP_USER_ROLE_FIELD = "groupUserRole";
    public static final String GROUP_IDS_FIELD = "groupIds";

    @Value("${email.pattern}")
    private String emailPattern;

    @Autowired(required = true)
    private InvitationService invitationService;

    @Autowired(required = true)
    private UserService userService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> clazz) {
        return InvitationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        InvitationForm invitationForm = (InvitationForm) target;
        invitationForm.setEmail(trimWhitespace(invitationForm.getEmail()));

        User user = userService.getUserFromSecurityContext();
        if (user == null || user.getActiveOrganization() == null) {
            errors.reject("invitationValidator.error.generic");
            return;
        }

        // Check if email is valid
        validateEmail(invitationForm.getEmail(), errors);

        if (errors.hasErrors()) {
            // No need to continue validating since the email is incorrect
            return;
        }

        // Validate organization user limit
        Set<String> emails = new HashSet<String>();
        emails.add(invitationForm.getEmail());
        boolean isValid = validateOrganizationUserLimit(emails, user.getActiveOrganization(), errors);
        if (!isValid) {
            return;
        }

        // Check if User Roles are valid
        if ((invitationForm.getOrganizationUserRole() == null || UserRole.ROLE_ORG_GUEST.equals(invitationForm.getOrganizationUserRole())) && invitationForm.getGroupUserRole() == null) {
            if (user.isActiveOrganizationAdmin()) {
                errors.reject("invitationValidator.userRole.both.empty");
            } else {
                errors.rejectValue(GROUP_USER_ROLE_FIELD, "invitationValidator.userRole.group.empty");
            }
        }

        User invitedUser = userService.getByEmail(invitationForm.getEmail());

        validateInvitationForOrganization(invitationForm, user, invitedUser, errors);
        validateInvitationForGroups(invitationForm, user, invitedUser, errors);

//        Domain domain = domainService.get(invitationForm.getDomainId());
//
//        Organization organization = null;
//
//        if (domain == null) {
//            log.error(String.format("Unable to get Domain with ID: %s", invitationForm.getDomainId()));
//            errors.reject("invitationValidator.error.generic");
//            return;
//        }
//
//        if (DomainType.GROUP.equals(domain.getDomainType())) {
//            organization = ((Group) domain).getOrganization();
//        } else if (DomainType.ORGANIZATION.equals(domain.getDomainType())) {
//            organization = (Organization) domain;
//        }
//
//        if (organization == null) {
//            log.error(String.format("Organization null for Domain with ID: %s", domain.getId()));
//            errors.reject("invitationValidator.error.generic");
//            return;
//        }
//
//        DomainConfiguration domainConfiguration = organization.getDomainConfiguration();
//        if (domainConfiguration == null) {
//            log.error(String.format("Null domainConfiguration for Organization with ID: %s", organization.getId()));
//            errors.reject("invitationValidator.error.generic");
//            return;
//        }
//        String domainTypeMessage = messageSource.getMessage(DomainType.ORGANIZATION.getMessageKey(), null, LocaleContextHolder.getLocale());
//
//        if (domainConfiguration.isDisabledDomain()) {
//            errors.reject("invitationValidator.domain.disabled", new Object[]{domainTypeMessage}, "");
//            return;
//        }
//
//        List<InviteeForm> inviteeForms = invitationForm.getInviteeForms();
//        if (inviteeForms == null || inviteeForms.isEmpty()) {
//            errors.reject("invitationValidator.email.empty");
//            return;
//        } else {
//
//            Set<String> emails = new java.util.HashSet<String>();
//            for (InviteeForm inviteeForm : inviteeForms) {
//                emails.add(inviteeForm.getEmail().toLowerCase());
//            }
//            long numEmailsWithoutInvitations = invitationService.countEmailsWithoutInvitationsForOrganization(emails, organization.getId(), true);
//
//            long currentOrgUsers = organizationService.countOrganizationUsers(organization.getId(), true);
//            long currentOrgPendingInvites = invitationService.countAllForOrganizationIncludingGroups(organization.getId());
//
//            long totalUsers = currentOrgUsers + currentOrgPendingInvites;
//
//            if (domainConfiguration.getUserLimit() < totalUsers + numEmailsWithoutInvitations) {
//                if (inviteeForms.size() > 1) {
//                    errors.reject("invitationValidator.domain.userLimit.multipleInvites", new Object[]{domainTypeMessage, domainConfiguration.getUserLimit(), totalUsers - domainConfiguration.getUserLimit()}, "");
//                } else {
//                    errors.reject("invitationValidator.domain.userLimit", new Object[]{domainTypeMessage}, "");
//                }
//                return;
//            }
//        }
//
//        for (InviteeForm inviteeForm : inviteeForms) {
//            isValidInviteeForm(inviteeForm, invitationForm.getDomainId(), invitationForm.getDomainType(), errors);
//        }
    }

    protected void validateEmail(String email, Errors errors) {
        if (!hasText(email)) {
            errors.rejectValue(EMAIL_FIELD, "invitationValidator.email.empty");
        } else {
            if (!isValidEmailPattern(email)) {
                errors.rejectValue(EMAIL_FIELD, "invitationValidator.email.invalid", new Object[]{email}, "");
            }
        }
    }

    private boolean isValidEmailPattern(String email) {
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher m = pattern.matcher(email);
        boolean isValidEmail = m.matches();
        if (!isValidEmail) {
            return false;
        }

        return true;
    }

    private void validateInvitationForOrganization(InvitationForm form, User user, User invitedUser, Errors errors) {
        if (!user.isActiveOrganizationAdmin() && form.getOrganizationUserRole() != null && !UserRole.ROLE_ORG_GUEST.equals(form.getOrganizationUserRole())) {
            errors.rejectValue(ORGANIZATION_USER_ROLE_FIELD, "invitationValidator.security.nonOrgAdmin",
                    new Object[]{trimWhitespace(user.getActiveOrganization().getName())},
                    "");
            return;
        }

        // If Org role is set and NOT ROLE_ORG_GUEST
        if (form.getOrganizationUserRole() != null && !UserRole.ROLE_ORG_GUEST.equals(form.getOrganizationUserRole())) {

            // Check if invitation already exists to Organization
            if (!CollectionUtils.isEmpty(invitationService.getAll(form.getEmail(), user.getActiveOrganization().getId()))) {
                errors.reject("invitationValidator.organization.invitationExists",
                        new Object[]{form.getEmail(), trimWhitespace(user.getActiveOrganization().getName())},
                        "");
            } else {

                if (invitedUser != null) {
                    // check if user already belongs to org
                    if (userDomainService.get(invitedUser, user.getActiveOrganization().getId()) != null) {
                        errors.reject("invitationValidator.organization.userExists",
                                new Object[]{form.getEmail(), trimWhitespace(user.getActiveOrganization().getName())},
                                "");
                    }
                }
            }
        }
    }

    private void validateInvitationForGroups(InvitationForm form, User user, User invitedUser, Errors errors) {
        // A group role must be selected if organization role is set to ROLE_ORG_GUEST
        if (UserRole.ROLE_ORG_GUEST.equals(form.getOrganizationUserRole()) && form.getGroupUserRole() == null) {
            errors.rejectValue(GROUP_USER_ROLE_FIELD, "invitationValidator.orgGuest.groupRole.empty");
        }

        if (form.getGroupUserRole() != null) {

            // At least one group must be selected since the group UserRole is not empty
            if (CollectionUtils.isEmpty(form.getGroupIds())) {
                errors.rejectValue(GROUP_IDS_FIELD, "invitationValidator.groupIds.invalid");
            } else {

                List<Group> administeredGroups = userService.getAdministeredGroups(user, null);

                // Check if the user has admin access to any groups
                if (CollectionUtils.isEmpty(administeredGroups)) {
                    errors.rejectValue(GROUP_IDS_FIELD, "invitationValidator.groupIds.invalid");
                } else {
                    for (Long groupId : form.getGroupIds()) {
                        // Check if groupId belongs in List of groups which the user is an admin
                        if (!doesGroupListContainGroupWithId(administeredGroups, groupId)) {
                            errors.rejectValue(GROUP_IDS_FIELD, "invitationValidator.security.group.invalid");
                            break;
                        } else {
                            // Check if invitation already exists for any of the Groups
                            if (!CollectionUtils.isEmpty(invitationService.getAll(form.getEmail(), groupId))) {
                                errors.reject("invitationValidator.group.invitationExists",
                                        new Object[]{form.getEmail(), trimWhitespace(groupService.get(groupId).getName())},
                                        "");
                            } else {

                                if (invitedUser != null) {
                                    // Check if user already belongs to Group
                                    if (userDomainService.get(invitedUser, groupId) != null) {
                                        errors.reject("invitationValidator.group.userExists",
                                                new Object[]{form.getEmail(), trimWhitespace(groupService.get(groupId).getName())},
                                                "");
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    protected boolean doesGroupListContainGroupWithId(List<Group> groups, Long id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    private boolean validateOrganizationUserLimit(Set<String> emails, Organization organization, Errors errors) {
        DomainConfiguration domainConfiguration = organization.getDomainConfiguration();
        if (domainConfiguration == null) {
            log.error(String.format("Null domainConfiguration for Organization with ID: %s", organization.getId()));
            errors.reject("invitationValidator.error.generic");
            return false;
        }

        long numEmailsWithoutInvitations = invitationService.countEmailsWithoutInvitationsForOrganization(emails, organization.getId(), true);

        long currentOrgUsers = organizationService.countOrganizationUsers(organization.getId(), true);
        long currentOrgPendingInvites = invitationService.countAllForOrganizationIncludingGroups(organization.getId());

        long totalUsers = currentOrgUsers + currentOrgPendingInvites;

        if (domainConfiguration.getUserLimit() < totalUsers + numEmailsWithoutInvitations) {
            String domainTypeMessage = messageSource.getMessage(DomainType.ORGANIZATION.getMessageKey(), null, LocaleContextHolder.getLocale());

            if (emails.size() > 1) {
                errors.reject("invitationValidator.domain.userLimit.multipleInvites", new Object[]{domainTypeMessage, domainConfiguration.getUserLimit(), totalUsers - domainConfiguration.getUserLimit()}, "");
            } else {
                errors.reject("invitationValidator.domain.userLimit", new Object[]{domainTypeMessage}, "");
            }
            return false;
        }

        return true;
    }

    public boolean isValidContact(Contact contact, Errors errors) {
        boolean isValid = true;

        if (!isValidEmailPattern(contact.getEmail())) {
            errors.reject("invitationValidator.email.invalid", new Object[]{contact.getEmail()}, "");
            isValid = false;
        }

//        List<Invitation> invitations = invitationService.getAll(inviteeForm.getEmail(), domainId);
//        if (invitations != null && invitations.size() > 0) {
//            errors.reject("invitationValidator.invitationExists", new Object[]{inviteeForm.getEmail()}, "");
//            isValid = false;
//        }
//
//        User invitee = userService.getByEmail(inviteeForm.getEmail());
//        if (invitee != null) {
//            boolean isUserInDomain = userDomainService.get(invitee, domainId) != null;
//            if (isUserInDomain) {
//                errors.reject("invitationValidator.userExists", new Object[]{invitee.getEmail()}, "");
//                isValid = false;
//            }
//        }
        return isValid;
    }
}
