package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.forms.RegistrationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("registrationValidator")
public class RegistrationValidator implements Validator {

    private static final String FIRST_NAME_FIELD = "firstName";
    private static final String LAST_NAME_FIELD = "lastName";
    private static final String EMAIL_FIELD = "email";
    private static final String FIRST_PASSWORD_FIELD = "firstPassword";
    private static final String TERMS_OF_SERVICE_FIELD = "termsOfService";

    @Value("${password.pattern}")
    private String passwordPattern;

    @Value("${email.pattern}")
    private String emailPattern;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Override
    public boolean supports(Class<?> aClass) {
        return RegistrationForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RegistrationForm registrationForm = (RegistrationForm) o;

        checkForInvitations(registrationForm.getEmail(), errors);

        if (registrationForm.getFirstName() == null || "".equals(registrationForm.getFirstName().trim())) {
            registrationForm.setFirstName(null);
            errors.rejectValue(FIRST_NAME_FIELD, "registrationValidator.emptyFirstName");
        }

        if (registrationForm.getLastName() == null || "".equals(registrationForm.getLastName().trim())) {
            registrationForm.setLastName(null);
            errors.rejectValue(LAST_NAME_FIELD, "registrationValidator.emptyLastName");
        }

        if (registrationForm.getEmail() == null || "".equals(registrationForm.getEmail().trim())) {
            registrationForm.setEmail(null);
            errors.rejectValue(EMAIL_FIELD, "registrationValidator.email.invalid");
        } else {
            Pattern pattern = Pattern.compile(emailPattern);
            Matcher m = pattern.matcher(registrationForm.getEmail());
            if (!m.matches()) {
                errors.rejectValue(EMAIL_FIELD, "registrationValidator.email.invalid");
            } else if (userService.getByEmail(registrationForm.getEmail()) != null) {
                errors.rejectValue(EMAIL_FIELD, "registrationValidator.emailAlreadyExists");
            }
        }

        if (!isPasswordValid(registrationForm.getFirstPassword())) {
            errors.rejectValue(FIRST_PASSWORD_FIELD, "registrationValidator.password.invalid");
        }

        if (!doPasswordsMatch(registrationForm.getFirstPassword(), registrationForm.getSecondPassword())) {
            errors.rejectValue(FIRST_PASSWORD_FIELD, "registrationValidator.passwordMismatch");
        }

        if (!registrationForm.isTermsOfService()) {
            errors.rejectValue(TERMS_OF_SERVICE_FIELD, "registrationValidator.terms.of.service.false");
        }
    }

    private void checkForInvitations(String email, Errors errors) {
        if(email == null || "".equals(email)) {
            return;
        }

        //If this is the first user in the system, don't check for an invitation
        if(userService.countAll() == 0) {
            return;
        }

        List<Invitation> invitationList = invitationService.getByEmail(email);
        if(invitationList == null || invitationList.size() == 0) {
            errors.reject("registrationValidator.noInvitations");
        }
    }

    private boolean isPasswordValid(String password) {
        if (password == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean isPasswordEmpty(String password) {
        return (password == null || "".equals(password.trim()));
    }

    private boolean doPasswordsMatch(String firstPassword, String secondPassword) {
        return (!isPasswordEmpty(firstPassword) && !isPasswordEmpty(secondPassword) && firstPassword.equals(secondPassword));
    }
}
