package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.forms.InviteeForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("invitationValidator")
public class InvitationValidator implements Validator {

    @Value("${email.pattern}")
    private String emailPattern;

    @Autowired(required = true)
    private InvitationService invitationService;

    @Autowired(required = true)
    private UserService userService;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> clazz) {
        return InvitationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        InvitationForm invitationForm = (InvitationForm) target;

        Domain domain = domainService.get(invitationForm.getDomainId());
        DomainConfiguration domainConfiguration = domain.getDomainConfiguration();
        String domainTypeMessage = messageSource.getMessage(domain.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale());

        if(domainConfiguration.isDisabledDomain()) {
            errors.reject("invitationValidator.domain.disabled", new String[]{domainTypeMessage}, "");
            return;
        }

        if(DomainType.GROUP.equals(domain.getDomainType())) {
            if(groupService.isUserLimit((Group)domain)) {
                errors.reject("invitationValidator.domain.userLimit", new String[]{domainTypeMessage}, "");
                return;
            }
        }

        if(DomainType.ORGANIZATION.equals(domain.getDomainType())) {
            if(organizationService.isUserLimit((Organization)domain)) {
                errors.reject("invitationValidator.domain.userLimit", new String[]{domainTypeMessage}, "");
                return;
            }
        }

        List<InviteeForm> inviteeForms = invitationForm.getInviteeForms();
        if (inviteeForms == null || inviteeForms.isEmpty()) {
            errors.reject("invitationValidator.email.empty");
            return;
        }

        for (InviteeForm inviteeForm : inviteeForms) {
            isValidInviteeForm(inviteeForm, invitationForm.getDomainId(), invitationForm.getDomainType(), errors);
        }
    }

    public boolean isValidInviteeForm(InviteeForm inviteeForm, Long domainId, DomainType domainType, Errors errors) {
        boolean isValid = true;
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher m = pattern.matcher(inviteeForm.getEmail());
        boolean isValidEmail = m.matches();
        if (!isValidEmail) {
            errors.reject("invitationValidator.email.invalid", new Object[]{inviteeForm.getEmail()}, "");
            isValid = false;
        }

        if (inviteeForm.getUserRole() == null) {
            errors.reject("invitationValidator.userRole.empty");
            isValid = false;
        }


        List<Invitation> invitations = invitationService.getAll(inviteeForm.getEmail(), domainId);
        if (invitations != null && invitations.size() > 0) {
            errors.reject("invitationValidator.invitationExists", new Object[]{inviteeForm.getEmail()}, "");
            isValid = false;
        }

        User invitee = userService.getByEmail(inviteeForm.getEmail());
        if (invitee != null) {
            boolean isUserInDomain = userService.isUserInDomain(invitee, domainId);
            if (isUserInDomain) {
                errors.reject("invitationValidator.userExists", new Object[]{invitee.getEmail()}, "");
                isValid = false;
            }
        }
        return isValid;
    }
}
