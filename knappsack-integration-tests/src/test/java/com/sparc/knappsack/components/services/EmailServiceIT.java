package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.UserModel;
import com.sparc.knappsack.util.MailTestUtils;
import com.sparc.knappsack.util.WebRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EmailServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private EmailService emailService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private DomainUserRequestService domainUserRequestService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private RoleService roleService;

    @Autowired(required = true)
    private CategoryService categoryService;

    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Autowired(required = true)
    private InvitationService invitationService;

    @Value("${dev.mail.server.port}")
    private int mailPort;

    private Wiser wiser;

    @Before
    public void setup() {
        super.setup();
        WebRequest.getInstance("http", "localhost", 8080, "knappsack");

        wiser = new Wiser();
        wiser.setPort(mailPort);
        wiser.start();

        MailTestUtils.reconfigureMailSenders(applicationContext, mailPort);
    }

    @After
    public void after() {
        wiser.stop();
    }

    @Test
    public void sendDomainUserAccessRequestEmailTest() throws MessagingException {
        User user = getUser();
        Group group = getGroup(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_ADMIN);

        DomainUserRequest domainUserRequest = new DomainUserRequest();
        domainUserRequest.setDomain(group);
        domainUserRequest.setUser(user);
        domainUserRequest.setStatus(Status.PENDING);
        domainUserRequestService.add(domainUserRequest);
        List<DomainUserRequest> domainUserRequests = domainUserRequestService.getAll(group.getId());
        assertTrue(domainUserRequests.size() == 1);
        emailService.sendDomainUserAccessRequestEmail(domainUserRequest.getId());

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Domain Access Request", msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("john_doe@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendActivationEmailTest() throws MessagingException {
        User user = getUser();
        emailService.sendActivationEmail(user.getId());

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Account Activation", msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("john_doe@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendInvitationEmailTest() throws MessagingException {
        User user = getUser();

        Organization organization = getOrganization();

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        categoryService.add(category);

        Invitation invitation = new Invitation();
        invitation.setDomain(organization);
        invitation.setEmail("john_doe@test.com");

        Role role = new Role();
        role.setAuthority(UserRole.ROLE_ORG_ADMIN.name());
        invitation.setRole(role);

        invitationService.add(invitation);

        emailService.sendInvitationEmail(user.getId(), invitation.getId());

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals(String.format("%s: Invitation to Knappsack", organization.getName()), msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("john_doe@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendPasswordChangedEmailTest() throws MessagingException {
        User user = getUser();
        emailService.sendPasswordResetEmail(user.getId(), "password");
        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Password Changed", msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("john_doe@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendDomainAccessConfirmationEmailTest() throws MessagingException {
        User user = getUser();
        Group group = getGroup(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_ADMIN);

        DomainUserRequest domainUserRequest = new DomainUserRequest();
        domainUserRequest.setDomain(group);
        domainUserRequest.setUser(user);
        domainUserRequest.setStatus(Status.ACCEPTED);
        domainUserRequestService.add(domainUserRequest);
        List<DomainUserRequest> domainUserRequests = domainUserRequestService.getAll(group.getId());
        assertTrue(domainUserRequests.size() == 1);

        DomainUserRequestModel model = new DomainUserRequestModel();
        UserModel userModel = new UserModel();
        userModel.setId(user.getId());
        userModel.setEmail(user.getEmail());
        userModel.setUserName(user.getUsername());
        userModel.setFirstName(user.getFirstName());
        userModel.setLastName(user.getLastName());
        model.setUser(userModel);
        model.setId(domainUserRequest.getId());
        model.setStatus(domainUserRequest.getStatus());
        GroupModel groupModel = new GroupModel();
        groupModel.setId(domainUserRequest.getDomain().getId());
        groupModel.setName(domainUserRequest.getDomain().getName());
        model.setDomain(groupModel);
        emailService.sendDomainUserAccessConfirmationEmail(model);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Domain Access Request Accepted", msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("john_doe@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendApplicationPublishRequestEmailTest() throws MessagingException {
        User user = getUser();
        UserModel userModel = new UserModel();
        userModel.setEmail(user.getEmail());
        userModel.setFirstName(user.getFirstName());
        userModel.setLastName(user.getLastName());
        userModel.setId(user.getId());
        userModel.setUserName(user.getUsername());
        Group group = getGroup(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_ADMIN);
        emailService.sendApplicationPublishRequestEmail(group.getOwnedApplications().get(0).getApplicationVersions().get(0).getId(), userModel);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Application Publish Request", msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("john_doe@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    /*@Test
    public void sendBatchInvitationEmailTest() throws MessagingException {
        User user = getUser();
        Group group = getGroup(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_ADMIN);
        List<String> toEmails = new ArrayList<String>();
        toEmails.add("test1@sparcedge.com");
        toEmails.add("test2@sparcedge.com");

        wiser.getMessages().clear();
        HttpServletRequest httpServletRequest = getHttpServletRequest("/knappsack/auth/register?email=");
        emailService.sendBatchInvitationEmail(user, toEmails, group.getName(), DomainType.GROUP, httpServletRequest);

        assertEquals(2, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Application Publish Request", msg.getSubject());
            assertEquals("test@test.com", msg.getFrom()[0].toString());
            assertEquals("test@test.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }*/

    private Organization getOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.getStorageConfigurations().add(localStorageConfiguration);
        orgStorageConfig.setPrefix("testPrefix");
        orgStorageConfig.setOrganization(organization);
        organization.setOrgStorageConfig(orgStorageConfig);

        organizationService.add(organization);

        return organization;
    }

    private Group getGroup(UserRole organizationUserRole, UserRole groupUserRole) {
        User user = getUser();

        Role groupRole = roleService.getRoleByAuthority(groupUserRole.name());
        Role orgRole = roleService.getRoleByAuthority(organizationUserRole.name());

        user.getRoles().add(groupRole);
        user.getRoles().add(orgRole);

        Organization organization = getOrganization();

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        categoryService.add(category);

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.save(group);

        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setOwnedGroup(group);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);
        applicationVersion.setApplication(application);

        application.getApplicationVersions().add(applicationVersion);

        applicationService.add(application);

        group.getOwnedApplications().add(application);
        application.setOwnedGroup(group);

        organization.getGroups().add(group);

        userService.save(user);

        organizationService.getAll();

        UserDomain userDomainGroup = new UserDomain();
        userDomainGroup.setUser(user);
        userDomainGroup.setDomain(group);
        userDomainGroup.setRole(groupRole);
        group.getUserDomains().add(userDomainGroup);
        userDomainService.add(userDomainGroup);

        UserDomain userDomainOrg = new UserDomain();
        userDomainOrg.setUser(user);
        userDomainOrg.setDomain(organization);
        userDomainOrg.setRole(orgRole);
        organization.getUserDomains().add(userDomainOrg);
        userDomainService.add(userDomainOrg);

        user.getUserDomains().add(userDomainGroup);
        user.getUserDomains().add(userDomainOrg);

        userService.save(user);

        return group;
    }
}
