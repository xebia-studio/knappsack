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
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

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

    @Autowired(required = true)
    private DomainRequestService domainRequestService;

    @Autowired(required = true)
    private RegionService regionService;

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
        User user = getUserWithSecurityContext();
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
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals(user.getEmail(),
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendActivationEmail_Success_Test() throws MessagingException {
        User user = createUser("user1@knappsack.com", false, false);
        user.setActivated(false);

        emailService.sendActivationEmail(user.getId());

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Account Activation", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals(user.getEmail(),
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendActivationSuccessEmail_Success_Test() throws MessagingException {
        User user = createUser("user1@knappsack.com", true, false);

        boolean success = emailService.sendActivationSuccessEmail(user.getId());

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 1);
        assertEquals(wiser.getMessages().get(0).getEnvelopeReceiver(), user.getEmail());
    }

    @Test
    public void sendInvitationEmailTest() throws MessagingException, IOException {
        User user = getUserWithSecurityContext();

        Organization organization = getOrganization();
        DomainConfiguration domainConfiguration = new DomainConfiguration();
        domainConfiguration.setCustomBrandingEnabled(true);
        organization.setDomainConfiguration(domainConfiguration);
        CustomBranding branding = new CustomBranding();
        branding.setEmailFooter("Email footer");
        branding.setEmailHeader("Email header");
        organization.setCustomBranding(branding);

        user.setActiveOrganization(organization);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        categoryService.add(category);

        Invitation invitation = new Invitation();
        invitation.setDomain(organization);
        invitation.setEmail(user.getEmail());

        Role role = new Role();
        role.setAuthority(UserRole.ROLE_ORG_ADMIN.name());
        invitation.setRole(role);

        invitationService.add(invitation);


        List<Long> invitationIds = new ArrayList<Long>();
        invitationIds.add(invitation.getId());
        emailService.sendInvitationsEmail(user.getId(), invitationIds);

        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals(String.format("%s: Invitation to Knappsack", organization.getName()), msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals(user.getEmail(),
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
            String content = (String) msg.getContent();
            assertTrue(content.contains("Email footer"));
            assertTrue(content.contains("Email header"));
        }
    }

    @Test
    public void sendPasswordChangedEmailTest() throws MessagingException {
        User user = getUserWithSecurityContext();
        emailService.sendPasswordResetEmail(user.getId(), "password");
        assertEquals(1, wiser.getMessages().size());

        if (wiser.getMessages().size() > 0) {
            WiserMessage wMsg = wiser.getMessages().get(0);
            MimeMessage msg = wMsg.getMimeMessage();

            assertNotNull(msg);
            assertEquals("Knappsack: Password Changed", msg.getSubject());
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals(user.getEmail(),
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendDomainAccessConfirmationEmailTest() throws MessagingException {
        User user = getUserWithSecurityContext();
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
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals(user.getEmail(),
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendApplicationPublishRequestEmailTest() throws MessagingException {
        User user = getUserWithSecurityContext();
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
            assertEquals("notifications@knappsack.com", msg.getFrom()[0].toString());
            assertEquals(user.getEmail(),
                    msg.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        }
    }

    @Test
    public void sendOrganizationRegistrationEmailTest() throws MessagingException {
        wiser.getMessages().clear();

        Organization organization = getOrganization();
        User user = getUserWithSecurityContext();
        UserModel userModel = new UserModel();
        userModel.setEmail(user.getEmail());
        userModel.setFirstName(user.getFirstName());
        userModel.setLastName(user.getLastName());
        userModel.setId(user.getId());
        userModel.setUserName(user.getUsername());

        boolean success = emailService.sendOrganizationRegistrationEmail(organization.getId(), userModel);

        assertTrue(success);
    }

    @Test
    public void sendApplicationVersionBecameVisibleEmail_Success_Test() throws MessagingException {
        wiser.getMessages().clear();

        User user = getUserWithSecurityContext();
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(user.getId());
        Application application = getApplication();

        boolean success = emailService.sendApplicationVersionBecameVisibleEmail(application.getApplicationVersions().get(0).getId(), userIds);

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 1);
        assertEquals(wiser.getMessages().get(0).getEnvelopeReceiver(), user.getEmail());
    }

    @Test
    public void sendBandwidthLimitNotification_Success_Test() throws MessagingException {
        wiser.getMessages().clear();
        Organization organization = getOrganization();

        User user1 = getUserWithSecurityContext();
        UserModel userModel1 = new UserModel();
        userModel1.setEmail(user1.getEmail());
        userModel1.setUserName(user1.getUsername());
        userModel1.setFirstName(user1.getFirstName());
        userModel1.setLastName(user1.getLastName());
        userModel1.setId(user1.getId());

        User user2 = getUserWithSecurityContext();
        UserModel userModel2 = new UserModel();
        userModel2.setEmail(user2.getEmail());
        userModel2.setUserName(user2.getUsername());
        userModel2.setFirstName(user2.getFirstName());
        userModel2.setLastName(user2.getLastName());
        userModel2.setId(user2.getId());

        List<UserModel> userModels = new ArrayList<UserModel>();
        userModels.add(userModel1);
        userModels.add(userModel2);

        boolean success = emailService.sendBandwidthLimitNotification(organization.getId(), userModels);

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 2);
    }

    @Test
    public void sendApplicationVersionErrorEmail_Success_Test() throws MessagingException {
        wiser.getMessages().clear();
        Application application = getApplication();
        application.getApplicationVersions().get(0).setAppState(AppState.ERROR);

        applicationService.update(application);

        User user1 = createUser("user1@knappsack.com", true, false);
        User user2 = createUser("user2@knappsack.com", true, false);
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(user1.getId());
        userIds.add(user2.getId());

        boolean success = emailService.sendApplicationVersionErrorEmail(application.getApplicationVersions().get(0).getId(), userIds);

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 2);
        assertEquals(wiser.getMessages().get(0).getEnvelopeReceiver(), user1.getEmail());
        assertEquals(wiser.getMessages().get(1).getEnvelopeReceiver(), user2.getEmail());
    }

    @Test
    public void sendApplicationVersionResignCompleteEmail_Success_Test() throws MessagingException {
        wiser.getMessages().clear();

        Application application = getApplication();

        User user1 = createUser("user1@knappsack.com", true, false);
        User user2 = createUser("user2@knappsack.com", true, false);
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(user1.getId());
        userIds.add(user2.getId());

        boolean success = emailService.sendApplicationVersionResignCompleteEmail(application.getApplicationVersions().get(0).getId(), true, null, userIds);

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 2);
        assertEquals(wiser.getMessages().get(0).getEnvelopeReceiver(), user1.getEmail());
        assertEquals(wiser.getMessages().get(1).getEnvelopeReceiver(), user2.getEmail());
    }

    @Test
    public void sendDomainAccessRequestEmail_WithoutRegion_Success_Test() throws MessagingException {
        Organization organization = getOrganization();
        User admin1 = createUser("user1@knappsack.com", true, false);
        User admin2 = createUser("user2@knappsack.com", true, false);
        addAdminToOrganization(organization, admin1);
        addAdminToOrganization(organization, admin2);

        DomainRequest domainRequest = getDomainRequest(organization, null);

        boolean success = emailService.sendDomainAccessRequestEmail(domainRequest.getId());

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 2);
    }

    @Test
    public void sendDomainAccessRequestEmail_WithRegion_Success_Test() throws MessagingException {
        Organization organization = getOrganization();
        User admin1 = createUser("user1@knappsack.com", true, false);
        User admin2 = createUser("user2@knappsack.com", true, false);
        addAdminToOrganization(organization, admin1);
        addAdminToOrganization(organization, admin2);

        Region region = getRegion("region1@knappsack.com", "region2@knappsack.com", "region3@knappsack.com");

        DomainRequest domainRequest = getDomainRequest(organization, region);

        boolean success = emailService.sendDomainAccessRequestEmail(domainRequest.getId());

        assertTrue(success);
        assertEquals(wiser.getMessages().size(), 3);
    }

    /*@Test
    public void sendBatchInvitationEmailTest() throws MessagingException {
        User user = getUserWithSecurityContext();
        Group group = getGroup(UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_ADMIN);
        List<String> toEmails = new ArrayList<String>();
        toEmails.add("test1@sparcedge.com");
        toEmails.add("test2@sparcedge.com");

        wiser.getMessages().clear();


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

    private Application getApplication() {
        Organization organization = getOrganization();

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        group.setOrganization(organization);
        groupService.add(group);

        Application application = new Application();
        application.setName("Test Application");
        application.setDescription("This is a description.");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setStorageConfiguration(organization.getStorageConfigurations().get(0));
        application.setOwnedGroup(group);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);
        applicationService.add(application);

        group.getOwnedApplications().add(application);
        groupService.save(group);
        application.setOwnedGroup(group);

        Group group2 = new Group();
        group2.setName("Test Group 2");
        group2.setOrganization(organization);
        group2.setGuestApplicationVersions(new ArrayList<ApplicationVersion>());
        group2.getGuestApplicationVersions().add(applicationVersion);
        groupService.save(group2);

        organization.getGroups().add(group);
        organization.getGroups().add(group2);

        organizationService.getAll();

        return application;
    }

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
        User user = getUserWithSecurityContext();

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

    private void addAdminToOrganization(Organization organization, User user) {
        UserDomain userDomain = new UserDomain();
        userDomain.setDomain(organization);
        userDomain.setRole(roleService.getRoleByAuthority(UserRole.ROLE_ORG_ADMIN.name()));
        userDomain.setUser(user);

        userDomainService.add(userDomain);
    }

    private DomainRequest getDomainRequest(Domain domain, Region region) {
        DomainRequest domainRequest = new DomainRequest();
        domainRequest.setAddress("test address");
        domainRequest.setCompanyName("test company name");
        domainRequest.setDeviceType(DeviceType.IPAD_4);
        domainRequest.setDomain(domain);
        domainRequest.setEmailAddress("domainRequest@knappsack.com");
        domainRequest.setFirstName("FirstName");
        domainRequest.setLastName("LastName");
        domainRequest.setRegion(region);

        Set<Language> languageSet = new HashSet<Language>();
        languageSet.add(Language.ENGLISH);
        languageSet.add(Language.FRENCH);
        domainRequest.setLanguages(languageSet);

        domainRequestService.add(domainRequest);

        return domainRequest;
    }

    private Region getRegion(String... emails) {
        Region region = new Region();
        region.setName("test region");
        Collections.addAll(region.getEmails(), emails);

        regionService.add(region);

        return region;
    }
}
