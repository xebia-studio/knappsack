package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.forms.PasswordForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("passwordValidator")
public class PasswordValidator implements Validator {

    private static final String ORIGINAL_PASSWORD_FIELD = "originalPassword";
    private static final String FIRST_NEW_PASSWORD_FIELD = "firstNewPassword";
    @Value("${password.pattern}")
    private String passwordPattern;

    @Qualifier("passwordEncoder")
    @Autowired(required = true)
    private PasswordEncoder passwordEncoder;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return PasswordForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) o;

        User user = userService.getUserFromSecurityContext();

        if (isPasswordEmpty(passwordForm.getOriginalPassword())) {
            errors.rejectValue(ORIGINAL_PASSWORD_FIELD, "passwordValidator.emptyOriginalPassword");
        } else if (!passwordEncoder.isPasswordValid(user.getPassword(), passwordForm.getOriginalPassword(), user.getUsername())) {
            errors.rejectValue(ORIGINAL_PASSWORD_FIELD, "passwordValidator.originalPasswordMismatch");
        }

        if (!isPasswordValid(passwordForm.getFirstNewPassword())) {
            errors.rejectValue(FIRST_NEW_PASSWORD_FIELD, "passwordValidator.password.invalid");
        }

        if (!doPasswordsMatch(passwordForm.getFirstNewPassword(), passwordForm.getSecondNewPassword())) {
            errors.rejectValue(FIRST_NEW_PASSWORD_FIELD, "passwordValidator.newPasswordMismatch");
        }

        if (doPasswordsMatch(passwordForm.getFirstNewPassword(), passwordForm.getSecondNewPassword()) && doPasswordsMatch(passwordForm.getOriginalPassword(), passwordForm.getFirstNewPassword())) {
            errors.rejectValue(FIRST_NEW_PASSWORD_FIELD, "passwordValidator.newPasswordMatchesOriginal");
        }
    }

    private boolean isPasswordEmpty(String password) {
        return (password == null || "".equals(password.trim()));
    }

    private boolean isPasswordValid(String password) {
        if (isPasswordEmpty(password)) {
            return false;
        }
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean doPasswordsMatch(String firstPassword, String secondPassword) {
        return (!isPasswordEmpty(firstPassword) && !isPasswordEmpty(secondPassword) && firstPassword.equals(secondPassword));
    }
}
