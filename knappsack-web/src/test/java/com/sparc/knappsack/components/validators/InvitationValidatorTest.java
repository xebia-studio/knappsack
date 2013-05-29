package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.InvitationForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InvitationValidatorTest {

    private static final String EMAIL_PATTERN = "[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9-]+(.[A-Za-z0-9-]+)*";

    @Mock
    private InvitationService invitationService;

    @Mock
    private UserService userService;

    @Mock
    private UserDomainService userDomainService;

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
    private User user;

    @Before
    public void setup() {
        invitationForm = new InvitationForm();
        errors = new BeanPropertyBindingResult(invitationForm, "invitationForm");
        ReflectionTestUtils.setField(validator, "emailPattern", EMAIL_PATTERN);
        user = new User("test", "originalPassword", "test@test.com", "test", "test", new ArrayList<Role>());
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
    public void testValidOrganizationInvitiation() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_ADMIN);

        Organization organization = createOrganization(null);
        user.setActiveOrganization(organization);
        addUserToDomain(organization, user, UserRole.ROLE_ORG_ADMIN);

        when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");

        when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");
        when(invitationService.getAll(Matchers.anyString(), Matchers.anyLong())).thenReturn(new ArrayList<Invitation>());
        when(userService.getByEmail(Matchers.anyString())).thenReturn(null);
        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testValidGroupInvitiation() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);
        invitationForm.setGroupUserRole(UserRole.ROLE_GROUP_ADMIN);

        Group group1 = new Group();
        group1.setName("group1");
        group1.setId(1L);
        Group group2 = new Group();
        group2.setName("group2");
        group2.setId(2L);
        List<Group> groups = new ArrayList<Group>();
        groups.add(group1);
        groups.add(group2);

        invitationForm.getGroupIds().add(group1.getId());
        invitationForm.getGroupIds().add(group2.getId());

        Organization organization = createOrganization(groups);
        user.setActiveOrganization(organization);

        when(groupService.get(group1.getId())).thenReturn(group1);
        when(groupService.get(group2.getId())).thenReturn(group2);
        when(messageSource.getMessage(organization.getDomainType().getMessageKey(), null, LocaleContextHolder.getLocale())).thenReturn("organization");
        when(invitationService.getAll(Matchers.anyString(), Matchers.anyLong())).thenReturn(new ArrayList<Invitation>());
        when(userService.getByEmail(Matchers.anyString())).thenReturn(null);
        when(userService.getUserFromSecurityContext()).thenReturn(user);
        when(userService.getAdministeredGroups(user, null)).thenReturn(groups);
        when(userDomainService.get(any(User.class), anyLong())).thenReturn(null);

        validator.validate(invitationForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testInvalidEmail() {
        Organization organization = createOrganization(null);
        user.setActiveOrganization(organization);

        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("email"));

        setup();

        invitationForm.setEmail("badEmail");

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("email"));
    }

    @Test
    public void testInvalidUserRoles() {
        invitationForm.setEmail("invitee@knappsack.com");

        Organization organization = createOrganization(null);
        user.setActiveOrganization(organization);

        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("groupUserRole"));

        setup();

        invitationForm.setEmail("invitee@knappsack.com");

        user.setActiveOrganization(organization);
        addUserToDomain(organization, user, UserRole.ROLE_ORG_ADMIN);
        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        boolean userRoleErrorExists = false;
        for(ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.userRole.both.empty".equalsIgnoreCase(error.getCode())) {
                userRoleErrorExists = true;
                break;
            }
        }
        assertTrue(userRoleErrorExists);

        setup();

        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);

        user.setActiveOrganization(organization);
        addUserToDomain(organization, user, UserRole.ROLE_ORG_ADMIN);
        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("groupUserRole"));
        boolean groupUserRoleErrorExists = false;
        for(ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.orgGuest.groupRole.empty".equalsIgnoreCase(error.getCode())) {
                userRoleErrorExists = true;
                break;
            }
        }
        assertTrue(userRoleErrorExists);
    }

    @Test
    public void testInvalidOrganizationInvitation_UserNotOrgAdmin() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ADMIN);

        Organization organization = createOrganization(null);
        user.setActiveOrganization(organization);

        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("organizationUserRole"));
    }

    @Test
    public void testOrganizationOverLimit() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ADMIN);

        Organization organization = createOrganization(null);
        organization.getDomainConfiguration().setUserLimit(10);
        user.setActiveOrganization(organization);

        when(userService.getUserFromSecurityContext()).thenReturn(user);
        when(invitationService.countEmailsWithoutInvitationsForOrganization(anySet(), anyLong(), anyBoolean())).thenReturn(1L);
        when(organizationService.countOrganizationUsers(organization.getId(), true)).thenReturn(8L);
        when(invitationService.countAllForOrganizationIncludingGroups(organization.getId())).thenReturn(2L);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        boolean userLimitErrorExists = false;
        for(ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.domain.userLimit".equalsIgnoreCase(error.getCode())) {
                userLimitErrorExists = true;
                break;
            }
        }
        assertTrue(userLimitErrorExists);
    }

    @Test
    public void testInvitationAlreadyExistsForOrganization() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ADMIN);

        Organization organization = createOrganization(null);
        organization.getDomainConfiguration().setUserLimit(10);
        user.setActiveOrganization(organization);
        addUserToDomain(organization, user, UserRole.ROLE_ORG_ADMIN);

        List<Invitation> existingInvitations = new ArrayList<Invitation>();
        existingInvitations.add(new Invitation());
        when(invitationService.getAll(invitationForm.getEmail(), user.getActiveOrganization().getId()) ).thenReturn(existingInvitations);

        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        boolean invitationExistsErrorExists = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.organization.invitationExists".equalsIgnoreCase(error.getCode())) {
                invitationExistsErrorExists = true;
                break;
            }
        }
        assertTrue(invitationExistsErrorExists);
    }

    @Test
    public void testUserAlreadyBelongsToOrganization() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ADMIN);

        Organization organization = createOrganization(null);
        organization.getDomainConfiguration().setUserLimit(10);
        user.setActiveOrganization(organization);
        addUserToDomain(organization, user, UserRole.ROLE_ORG_ADMIN);

        when(invitationService.getAll(invitationForm.getEmail(), user.getActiveOrganization().getId()) ).thenReturn(null);

        when(userService.getUserFromSecurityContext()).thenReturn(user);
        when(userService.getByEmail(invitationForm.getEmail())).thenReturn(new User());
        when(userDomainService.get(any(User.class), anyLong())).thenReturn(new UserDomain());

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        boolean userExistsErrorExists = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.organization.userExists".equalsIgnoreCase(error.getCode())) {
                userExistsErrorExists = true;
                break;
            }
        }
        assertTrue(userExistsErrorExists);
    }

    @Test
    public void testEmptyGroupsError() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);
        invitationForm.setGroupUserRole(UserRole.ROLE_GROUP_ADMIN);

        Organization organization = createOrganization(null);
        user.setActiveOrganization(organization);

        when(invitationService.getAll(invitationForm.getEmail(), user.getActiveOrganization().getId())).thenReturn(null);
        when(userService.getUserFromSecurityContext()).thenReturn(user);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("groupIds"));
        boolean groupIdsEmptyErrorExists = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.groupIds.invalid".equalsIgnoreCase(error.getCode())) {
                groupIdsEmptyErrorExists = true;
                break;
            }
        }
        assertTrue(groupIdsEmptyErrorExists);
    }

    @Test
    public void testGroupInviteNotGroupAdminError() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);
        invitationForm.setGroupUserRole(UserRole.ROLE_GROUP_ADMIN);

        Group group1 = new Group();
        group1.setName("testGroup1");
        group1.setId(1L);
        Group group2 = new Group();
        group2.setName("testGroup");
        group2.setId(2L);
        List<Group> groups = new ArrayList<Group>();
        groups.add(group1);
        groups.add(group2);
        Organization organization = createOrganization(groups);

        List<Long> formGroupIds = new ArrayList<Long>();
        formGroupIds.add(group2.getId());
        invitationForm.setGroupIds(formGroupIds);

        addUserToDomain(group1, user, UserRole.ROLE_GROUP_USER);
        user.setActiveOrganization(organization);

        when(invitationService.getAll(invitationForm.getEmail(), user.getActiveOrganization().getId())).thenReturn(null);
        when(userService.getUserFromSecurityContext()).thenReturn(user);
        when(userService.getAdministeredGroups(any(User.class), any(SortOrder.class))).thenReturn(null);

        validator.validate(invitationForm, errors);
        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("groupIds"));
        boolean administeredGroupsEmptyErrorExists = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.groupIds.invalid".equalsIgnoreCase(error.getCode())) {
                administeredGroupsEmptyErrorExists = true;
                break;
            }
        }
        assertTrue(administeredGroupsEmptyErrorExists);

        errors = new BeanPropertyBindingResult(invitationForm, "invitationForm");

        List<Group> administeredGroups = new ArrayList<Group>();
        administeredGroups.add(group1);

        when(userService.getAdministeredGroups(any(User.class), any(SortOrder.class))).thenReturn(administeredGroups);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("groupIds"));
        boolean groupSecurityErrorExists = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.security.group.invalid".equalsIgnoreCase(error.getCode())) {
                groupSecurityErrorExists = true;
                break;
            }
        }
        assertTrue(groupSecurityErrorExists);
    }

    @Test
    public void testInvitationAlreadyExistsForGroup() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);
        invitationForm.setGroupUserRole(UserRole.ROLE_GROUP_ADMIN);

        Group group1 = new Group();
        group1.setName("testGroup1");
        group1.setId(1L);
        Group group2 = new Group();
        group2.setName("testGroup");
        group2.setId(2L);
        List<Group> groups = new ArrayList<Group>();
        groups.add(group1);
        groups.add(group2);
        Organization organization = createOrganization(groups);
        user.setActiveOrganization(organization);
        for(Group group : groups) {
            addUserToDomain(group, user, UserRole.ROLE_GROUP_ADMIN);
        }

        List<Long> formGroupIds = new ArrayList<Long>();
        formGroupIds.add(group2.getId());
        invitationForm.setGroupIds(formGroupIds);

        List<Invitation> existingInvitations = new ArrayList<Invitation>();
        existingInvitations.add(new Invitation());
        when(invitationService.getAll(invitationForm.getEmail(), group2.getId()) ).thenReturn(existingInvitations);

        when(userService.getUserFromSecurityContext()).thenReturn(user);
        when(userService.getAdministeredGroups(any(User.class), any(SortOrder.class))).thenReturn(groups);
        when(groupService.get(group2.getId())).thenReturn(group2);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        boolean invitationExistsErrorExists = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.group.invitationExists".equalsIgnoreCase(error.getCode())) {
                invitationExistsErrorExists = true;
                break;
            }
        }
        assertTrue(invitationExistsErrorExists);
    }

    @Test
    public void testUserAlreadyBelongsToGroup() {
        invitationForm.setEmail("invitee@knappsack.com");
        invitationForm.setOrganizationUserRole(UserRole.ROLE_ORG_GUEST);
        invitationForm.setGroupUserRole(UserRole.ROLE_GROUP_ADMIN);

        Group group1 = new Group();
        group1.setName("testGroup1");
        group1.setId(1L);
        Group group2 = new Group();
        group2.setName("testGroup");
        group2.setId(2L);
        List<Group> groups = new ArrayList<Group>();
        groups.add(group1);
        groups.add(group2);
        Organization organization = createOrganization(groups);
        user.setActiveOrganization(organization);
        for(Group group : groups) {
            addUserToDomain(group, user, UserRole.ROLE_GROUP_ADMIN);
        }

        List<Long> formGroupIds = new ArrayList<Long>();
        formGroupIds.add(group2.getId());
        invitationForm.setGroupIds(formGroupIds);

        when(invitationService.getAll(invitationForm.getEmail(), group2.getId())).thenReturn(null);

        when(userService.getUserFromSecurityContext()).thenReturn(user);
        when(userService.getAdministeredGroups(any(User.class), any(SortOrder.class))).thenReturn(groups);
        when(userService.getByEmail(invitationForm.getEmail())).thenReturn(new User());
        when(userDomainService.get(any(User.class), anyLong())).thenReturn(new UserDomain());
        when(groupService.get(group2.getId())).thenReturn(group2);

        validator.validate(invitationForm, errors);

        assertTrue(errors.hasErrors());
        boolean userExistsError = false;
        for (ObjectError error : errors.getAllErrors()) {
            if ("invitationValidator.group.userExists".equalsIgnoreCase(error.getCode())) {
                userExistsError = true;
                break;
            }
        }
        assertTrue(userExistsError);
    }

    private Organization createOrganization(List<Group> groups) {
        Organization organization = new Organization();
        organization.setName("Test Organziation");
        organization.setDomainConfiguration(new DomainConfiguration());
        organization.setId(2L);
        if (!CollectionUtils.isEmpty(groups)) {
            for (Group group : groups) {
                group.setOrganization(organization);
            }
            organization.getGroups().addAll(groups);
        }

        return organization;
    }

    private UserDomain addUserToDomain(Domain domain, User user, UserRole userRole) {
        UserDomain userDomain = new UserDomain();
        userDomain.setId(1L);
        userDomain.setUser(user);
        userDomain.setDomain(domain);
        Role role = new Role();
        role.setAuthority(userRole.name());
        userDomain.setRole(role);
        user.getUserDomains().add(userDomain);

        return userDomain;
    }
}
