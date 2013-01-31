package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.Role;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.InvitationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.forms.RegistrationForm;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationValidatorTest {

    @Mock private UserService mockUserService;

    @Mock private InvitationService invitationService;

    @InjectMocks
    private RegistrationValidator validator = new RegistrationValidator();


    private static final String PASSWORD_PATTERN = "^\\S{6,}$";
    private static final String EMAIL_PATTERN = "[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9-]+(.[A-Za-z0-9-]+)*";
    private Errors errors;
    private RegistrationForm registrationForm;
    private User user = new User("test", "originalPassword", "test@test.com", "test", "test", new ArrayList<Role>());

    @Before
    public void setup() {
        registrationForm = new RegistrationForm();
        errors = new BeanPropertyBindingResult(registrationForm, "registrationForm");
        ReflectionTestUtils.setField(validator, "passwordPattern", PASSWORD_PATTERN);
        ReflectionTestUtils.setField(validator, "emailPattern", EMAIL_PATTERN);

        Mockito.when(mockUserService.getByEmail(user.getEmail())).thenReturn(user);
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(registrationForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {

        registrationForm.setEmail("new@test.com");
        registrationForm.setFirstName("Test");
        registrationForm.setLastName("Test");
        registrationForm.setFirstPassword("password");
        registrationForm.setSecondPassword("password");

        Invitation invitation = new Invitation();
        List<Invitation> invitations = new ArrayList<Invitation>();
        invitations.add(invitation);
        Mockito.when(invitationService.getByEmail(registrationForm.getEmail())).thenReturn(invitations);

        validator.validate(registrationForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testAllFieldsEmpty() {
        validator.validate(registrationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 5);

        setup();

        registrationForm.setEmail("");
        registrationForm.setFirstName("");
        registrationForm.setLastName("");
        registrationForm.setFirstPassword("");
        registrationForm.setSecondPassword("");

        validator.validate(registrationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 5);

    }

    @Test
    public void testEmailAlreadyExists() {
        registrationForm.setEmail("test@test.com");
        validator.validate(registrationForm, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("email"));
        Assert.assertEquals(errors.getFieldError("email").getCode(), "registrationValidator.emailAlreadyExists");
    }

    @Test
    public void testPasswordMismatch() {
        registrationForm.setEmail("my@email.com");
        registrationForm.setFirstName("firstName");
        registrationForm.setLastName("lastName");
        registrationForm.setFirstPassword("firstPassword");
        registrationForm.setSecondPassword("secondPassword");

        Invitation invitation = new Invitation();
        List<Invitation> invitations = new ArrayList<Invitation>();
        invitations.add(invitation);
        Mockito.when(invitationService.getByEmail(registrationForm.getEmail())).thenReturn(invitations);

        validator.validate(registrationForm, errors);
        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("firstPassword"));

        setup();

        registrationForm.setEmail("my@email.com");
        registrationForm.setFirstName("firstName");
        registrationForm.setLastName("lastName");
        registrationForm.setFirstPassword("firstPassword");
        registrationForm.setSecondPassword("");

        validator.validate(registrationForm, errors);
        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("firstPassword"));
    }

    @Test
    public void testInvalidEmailAddress() {
        registrationForm.setEmail("invalidEmailAddress");
        validator.validate(registrationForm, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("email"));
    }
}
