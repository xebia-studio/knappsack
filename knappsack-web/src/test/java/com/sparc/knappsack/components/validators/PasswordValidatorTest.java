package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Role;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.forms.PasswordForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PasswordValidatorTest {

    @Mock private PasswordEncoder mockPasswordEncoder;
    @Mock private UserService mockUserService;

    @InjectMocks private PasswordValidator passwordValidator = new PasswordValidator();

    @Mock private SecurityContext mockSecurityContext;
    @Mock private Authentication authentication;

    private static final String PASSWORD_PATTERN = "^\\S{6,}$";
    private Errors errors;
    private PasswordForm passwordForm;
    private User user = new User("test", "originalPassword", "test@test.com", "test", "test", new ArrayList<Role>());

    @Before
    public void setup() {
        passwordForm = new PasswordForm();
        errors = new BeanPropertyBindingResult(passwordForm, "passwordForm");
        ReflectionTestUtils.setField(passwordValidator, "passwordPattern", PASSWORD_PATTERN);

        // Set mock behaviour/expectations on the mockSecurityContext
        Mockito.when(mockUserService.getUserFromSecurityContext()).thenReturn(user);
        Mockito.when(mockSecurityContext.getAuthentication()).thenReturn(new TestingAuthenticationToken(user, null));

        SecurityContextHolder.setContext(mockSecurityContext);

        Mockito.when(mockUserService.getByEmail(user.getEmail())).thenReturn(user);
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(passwordValidator.supports(passwordForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(passwordValidator.supports(String.class));
    }

    @Test
    public void testValid() {
        Mockito.when(mockPasswordEncoder.isPasswordValid(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(true);

        passwordForm.setOriginalPassword("originalPassword");
        passwordForm.setFirstNewPassword("mypassword");
        passwordForm.setSecondNewPassword("mypassword");
        passwordValidator.validate(passwordForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testInvalidOriginalPassword() {
        Mockito.when(mockPasswordEncoder.isPasswordValid(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(false);
        Mockito.when(mockUserService.getUserFromSecurityContext()).thenReturn(user);

        passwordForm.setOriginalPassword("originalPassword");
        passwordForm.setFirstNewPassword("mypassword");
        passwordForm.setSecondNewPassword("mypassword");
        passwordValidator.validate(passwordForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorCount() == 1);
    }

    @Test
    public void testPasswordMismatch() {
        Mockito.when(mockPasswordEncoder.isPasswordValid(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(true);
        Mockito.when(mockUserService.getUserFromSecurityContext()).thenReturn(user);

        passwordForm.setOriginalPassword("originalPassword");
        passwordForm.setFirstNewPassword("correct");
        passwordForm.setSecondNewPassword("wrong");
        passwordValidator.validate(passwordForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorCount() == 1);

        setup();

        passwordForm.setOriginalPassword("originalPassword");
        passwordForm.setFirstNewPassword("correct");
        passwordForm.setSecondNewPassword("");
        passwordValidator.validate(passwordForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorCount() == 1);
    }

    @Test
    public void testEmptyPasswords() {
        Mockito.when(mockPasswordEncoder.isPasswordValid(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(false);

        passwordValidator.validate(passwordForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorCount() == 3);

        setup();

        passwordForm.setOriginalPassword("");
        passwordForm.setFirstNewPassword("");
        passwordForm.setSecondNewPassword("");

        passwordValidator.validate(passwordForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorCount() == 3);
    }

    @Test
    public void testNewPasswordMatchesOriginal() {
        Mockito.when(mockPasswordEncoder.isPasswordValid(Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(true);
        Mockito.when(mockUserService.getUserFromSecurityContext()).thenReturn(user);

        passwordForm.setOriginalPassword("originalPassword");
        passwordForm.setFirstNewPassword("originalPassword");
        passwordForm.setSecondNewPassword("originalPassword");

        passwordValidator.validate(passwordForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorCount() == 1);
    }

}
