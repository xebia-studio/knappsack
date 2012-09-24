package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.GroupUserRequestModel;
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
import java.util.UUID;

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
    private GroupUserRequestService groupUserRequestService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private RoleService roleService;

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
    public void sendGroupAccessRequestEmailTest() throws MessagingException {
        User user = getUser();
        Group group = getGroup(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_ADMIN);

        GroupUserRequest groupUserRequest = new GroupUserRequest();
        groupUserRequest.setGroup(group);
        groupUserRequest.setUser(user);
        groupUserRequest.setStatus(Status.PENDING);
        groupUserRequestService.add(groupUserRequest);
        List<GroupUserRequest> groupUserRequests = groupUserRequestService.getAll(group.getId());
        assertTrue(groupUserRequests.size() == 1);
        emailService.sendGroupAccessRequestEmail(groupUserRequest);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Group Access Request", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendActivationEmailTest() throws MessagingException {
        User user = getUser();
        emailService.sendActivationEmail(user);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Account Activation", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendInvitationEmailTest() throws MessagingException {
        User user = getUser();

        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(localStorageConfiguration);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        organizationService.getAll();

        emailService.sendInvitationEmail(user, "notifications@knappsack.com", organization.getName(), DomainType.ORGANIZATION);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Domain Invitation", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendPasswordChangedEmailTest() throws MessagingException {
        User user = getUser();
        emailService.sendPasswordResetEmail(user, "password");
        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Password Changed", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendGroupAccessConfirmationEmailTest() throws MessagingException {
        User user = getUser();
        Group group = getGroup(UserRole.ROLE_ORG_USER, UserRole.ROLE_GROUP_ADMIN);

        GroupUserRequest groupUserRequest = new GroupUserRequest();
        groupUserRequest.setGroup(group);
        groupUserRequest.setUser(user);
        groupUserRequest.setStatus(Status.ACCEPTED);
        groupUserRequestService.add(groupUserRequest);
        List<GroupUserRequest> groupUserRequests = groupUserRequestService.getAll(group.getId());
        assertTrue(groupUserRequests.size() == 1);

        GroupUserRequestModel model = new GroupUserRequestModel();
        UserModel userModel = new UserModel();
        userModel.setId(user.getId());
        userModel.setEmail(user.getEmail());
        userModel.setUserName(user.getUsername());
        userModel.setFirstName(user.getFirstName());
        userModel.setLastName(user.getLastName());
        model.setUser(userModel);
        model.setId(groupUserRequest.getId());
        model.setStatus(groupUserRequest.getStatus());
        GroupModel groupModel = new GroupModel();
        groupModel.setId(groupUserRequest.getGroup().getId());
        groupModel.setName(groupUserRequest.getGroup().getName());
        model.setGroup(groupModel);
        emailService.sendGroupAccessConfirmationEmail(model);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Group Access Request Accepted", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
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
        emailService.sendApplicationPublishRequestEmail(group.getOwnedApplications().get(0).getApplicationVersions().get(0), userModel);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Application Publish Request", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
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
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals("notifications@knappsack.com",
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }*/

    private Group getGroup(UserRole organizationUserRole, UserRole groupUserRole) {
        User user = getUser();

        Role groupRole = roleService.getRoleByAuthority(groupUserRole.name());
        Role orgRole = roleService.getRoleByAuthority(organizationUserRole.name());

        user.getRoles().add(groupRole);
        user.getRoles().add(orgRole);

        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization 2");
        organizationService.add(organization);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.save(group);

        Application application = new Application();
        application.setName("Test Application");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);

        group.getOwnedApplications().add(application);

        organization.getGroups().add(group);

        userService.save(user);

        organizationService.getAll();

        UserDomain userDomainGroup = new UserDomain();
        userDomainGroup.setUser(user);
        userDomainGroup.setDomainId(group.getId());
        userDomainGroup.setDomainType(DomainType.GROUP);
        userDomainGroup.setRole(groupRole);
        userDomainGroup.setDomainId(group.getId());

        UserDomain userDomainOrg = new UserDomain();
        userDomainOrg.setUser(user);
        userDomainOrg.setDomainId(group.getId());
        userDomainOrg.setDomainType(DomainType.ORGANIZATION);
        userDomainOrg.setRole(orgRole);
        userDomainOrg.setDomainId(organization.getId());

        user.getUserDomains().add(userDomainGroup);
        user.getUserDomains().add(userDomainOrg);

        userService.save(user);

        return group;
    }
}
