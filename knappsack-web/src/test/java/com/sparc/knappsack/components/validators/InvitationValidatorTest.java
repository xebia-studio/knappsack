package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.forms.InviteeForm;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class InvitationValidatorTest {

    private static final String EMAIL_PATTERN = "[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9-]+(.[A-Za-z0-9-]+)*";

    @Mock
    private InvitationService invitationService;

    @Mock
    private UserService userService;

    @Mock
    private DomainService domainService;

    @Mock
    private GroupService groupService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private InvitationValidator validator = new InvitationValidator();

    private Errors errors;
    private InvitationForm invitationForm;

    @Before
    public void setup() {
        invitationForm = createInvitationForm();
        errors = new BeanPropertyBindingResult(invitationForm, "invitationForm");
        ReflectionTestUtils.setField(validator, "emailPattern", EMAIL_PATTERN);
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(invitationForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValidInvitiation() {
        invitationForm.getInviteeForms().add(createInviteeForm());

        Organization organization = new Organization();
        organization.setName("Test Organziation");
        organization.setDomainConfiguration(new DomainConfiguration());

        Mockito.when(domainService.get(invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(organization);
        Mockito.when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");

        Mockito.when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");
        Mockito.when(invitationService.getAll(Matchers.anyString(), Matchers.anyLong(), Matchers.any(DomainType.class))).thenReturn(new ArrayList<Invitation>());
        Mockito.when(userService.getByEmail(Matchers.anyString())).thenReturn(null);

        validator.validate(invitationForm, errors);

        assertFalse(errors.hasErrors());

    }

    @Test
    public void testInvalidInvitation() {
        InviteeForm inviteeForm1 = createInviteeForm();
        inviteeForm1.setEmail("invalid");
        inviteeForm1.setUserRole(UserRole.ROLE_ADMIN);

        InviteeForm inviteeForm2 = createInviteeForm();

        invitationForm.getInviteeForms().add(inviteeForm1);
        invitationForm.getInviteeForms().add(inviteeForm2);

        List<Invitation> invitations = new ArrayList<Invitation>();
        Invitation invitation = new Invitation();
        invitation.setDomainId(1L);
        invitation.setDomainType(DomainType.ORGANIZATION);
        invitation.setEmail("test@test.com");
        Role role = new Role();
        role.setAuthority(UserRole.ROLE_ORG_USER.name());
        invitation.setRole(role);
        invitations.add(invitation);

//        when(invitationService.getAll(inviteeForm1.getEmail(), invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(invitations);
        Mockito.when(invitationService.getAll(Matchers.anyString(), Matchers.anyLong(), Matchers.any(DomainType.class))).thenReturn(invitations);

        User user1 = new User();

        User user2 = new User();
        user2.setEmail(inviteeForm2.getEmail());

        Organization organization = new Organization();
        organization.setName("Test Organziation");
        organization.setDomainConfiguration(new DomainConfiguration());

        Mockito.when(domainService.get(invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(organization);
        Mockito.when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");

        Mockito.when(userService.getByEmail(inviteeForm1.getEmail())).thenReturn(user1);
        Mockito.when(userService.isUserInDomain(user1, invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(false);
        Mockito.when(userService.getByEmail(inviteeForm2.getEmail())).thenReturn(user2);
        Mockito.when(userService.isUserInDomain(user2, invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(true);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 5);

    }

    @Test
    public void testInvalidUserRole() {
        InviteeForm inviteeForm = createInviteeForm();
        inviteeForm.setUserRole(null);
        invitationForm.getInviteeForms().add(inviteeForm);

        Organization organization = new Organization();
        organization.setName("Test Organziation");
        organization.setDomainConfiguration(new DomainConfiguration());

        Mockito.when(domainService.get(invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(organization);
        Mockito.when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");

        Mockito.when(invitationService.getAll(Matchers.anyString(), Matchers.anyLong(), Matchers.any(DomainType.class))).thenReturn(null);
        Mockito.when(userService.getByEmail(Matchers.anyString())).thenReturn(null);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasGlobalErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        Assert.assertEquals(errors.getGlobalErrorCount(), 1);
    }

    @Test
    public void testEmptyInviteeForms() {
        Organization organization = new Organization();
        organization.setName("Test Organziation");
        organization.setDomainConfiguration(new DomainConfiguration());

        Mockito.when(domainService.get(invitationForm.getDomainId(), invitationForm.getDomainType())).thenReturn(organization);
        Mockito.when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasGlobalErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        Assert.assertEquals(errors.getGlobalErrorCount(), 1);

        setup();

        invitationForm.setInviteeForms(null);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasGlobalErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        Assert.assertEquals(errors.getGlobalErrorCount(), 1);
    }

    private InvitationForm createInvitationForm() {
        InvitationForm invitationForm = new InvitationForm();
        invitationForm.setDomainId(1L);
        invitationForm.setDomainType(DomainType.ORGANIZATION);

        return invitationForm;
    }

    private InviteeForm createInviteeForm() {
        InviteeForm inviteeForm = new InviteeForm();

        inviteeForm.setEmail("test@test.com");
        inviteeForm.setName("Test");
        inviteeForm.setUserRole(UserRole.ROLE_ORG_USER);

        return inviteeForm;
    }
}
