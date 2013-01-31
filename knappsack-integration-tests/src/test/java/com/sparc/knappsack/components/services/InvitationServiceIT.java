package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.InviteeForm;
import com.sparc.knappsack.util.MailTestUtils;
import com.sparc.knappsack.util.WebRequest;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.subethamail.wiser.Wiser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertTrue;

public class InvitationServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private InvitationControllerService invitationControllerService;

    @Autowired(required = true)
    private InvitationService invitationService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private DomainService domainService;

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
    public void addTest() {
        Invitation invitation = getInvitation();
        invitationService.add(invitation);
        List<Invitation> invitations = invitationService.getByEmail(getUser().getEmail());
        assertTrue(invitations.size() == 1);
    }

    @Test
    public void deleteTest() {
        Invitation invitation = getInvitation();
        invitationService.add(invitation);
        List<Invitation> invitations = invitationService.getByEmail(getUser().getEmail());
        assertTrue(invitations.size() == 1);
        invitationService.delete(invitation.getId());
        invitations = invitationService.getByEmail(getUser().getEmail());
        assertTrue(invitations.size() == 0);
    }

    @Test
    public void updateTest() {
        Invitation invitation = getInvitation();
        invitationService.add(invitation);
        List<Invitation> invitations = invitationService.getByEmail(getUser().getEmail());
        assertTrue(invitations.size() == 1);
        invitation = invitations.get(0);
        invitation.setEmail("updated@sparcedge.com");
        invitationService.update(invitation);
        invitations = invitationService.getByEmail("updated@sparcedge.com");
        assertTrue(invitations.size() == 1);
        invitations = invitationService.getByEmail(getUser().getEmail());
        assertTrue(invitations.isEmpty());
    }

    @Test
    public void getAllTest() {
        Invitation invitation = getInvitation();
        invitationService.add(invitation);
        List<Invitation> invitations = invitationService.getAll(invitation.getDomain().getId());
        assertTrue(invitations.size() == 1);

        invitations = invitationService.getAll(getUser().getEmail(), invitation.getDomain().getId());
        assertTrue(invitations.size() == 1);
    }

    @Test
    public void inviteUsersTest() {
        Invitation invitation = getInvitation();

        InviteeForm inviteeForm = new InviteeForm();
        inviteeForm.setEmail("invitee@sparcedge.com");
        inviteeForm.setName("New Guy");
        inviteeForm.setUserRole(UserRole.ROLE_ORG_USER);

        invitationControllerService.inviteUser(inviteeForm, invitation.getDomain().getId(), true);
        List<Invitation> invitations = invitationService.getByEmail("invitee@sparcedge.com");
        assertTrue(invitations.size() == 1);
    }

//    @Test
//    public void inviteBatchUsersTest() {
//        Invitation invitation = getInvitation();
//
//        InviteeForm inviteeForm = new InviteeForm();
//        inviteeForm.setEmail("invitee@sparcedge.com");
//        inviteeForm.setName("New Guy");
//        inviteeForm.setUserRole(UserRole.ROLE_ORG_USER);
//        List<InviteeForm> inviteeForms = new ArrayList<InviteeForm>();
//        inviteeForms.add(inviteeForm);
//        invitationService.inviteUsers(getUser(), inviteeForms, invitation.getDomainId(), invitation.getDomainType());
//        List<Invitation> invitations = invitationService.getByEmail("invitee@sparcedge.com");
//        assertTrue(invitations.size() == 1);
//    }

    @Test
    public void googleCSVTest() throws IOException {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Name,Given Name,Additional Name,Family Name,Yomi Name,Given Name Yomi,Additional Name Yomi,Family Name Yomi,Name Prefix,Name Suffix,Initials,Nickname,Short Name,Maiden Name,Birthday,Gender,Location,Billing Information,Directory Server,Mileage,Occupation,Hobby,Sensitivity,Priority,Subject,Notes,Group Membership,E-mail 1 - Type,E-mail 1 - Value,Phone 1 - Type,Phone 1 - Value\n");
        csvBuilder.append("John Doe,John,,Doe,,,,,,,,,,,,,,,,,,,,,,,,* Other,john.doe@sparcedge.com,Work,843-555-5555");
        InputStream is = new ByteArrayInputStream(csvBuilder.toString().getBytes());

        MultipartFile mockFile = new MockMultipartFile("googleContacts", "googleContacts.csv", "plain/text", is);
        List<InviteeForm> inviteeForms = invitationService.parseContactsGoogle(mockFile);
        assertTrue(inviteeForms.size() == 1);
        assertTrue(inviteeForms.get(0).getEmail().equals("john.doe@sparcedge.com"));
    }

    @Test
    public void outlookCSVTest() throws IOException {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("First Name,Middle Name,Last Name,Title,Suffix,Initials,Web Page,Gender,Birthday,Anniversary,Location,Language,Internet Free Busy,Notes,E-mail Address,E-mail 2 Address,E-mail 3 Address,Primary Phone,Home Phone,Home Phone 2,Mobile Phone,Pager,Home Fax,Home Address,Home Street,Home Street 2,Home Street 3,Home Address PO Box,Home City,Home State,Home Postal Code,Home Country,Spouse,Children,Manager's Name,Assistant's Name,Referred By,Company Main Phone,Business Phone,Business Phone 2,Business Fax,Assistant's Phone,Company,Job Title,Department,Office Location,Organizational ID Number,Profession,Account,Business Address,Business Street,Business Street 2,Business Street 3,Business Address PO Box,Business City,Business State,Business Postal Code,Business Country,Other Phone,Other Fax,Other Address,Other Street,Other Street 2,Other Street 3,Other Address PO Box,Other City,Other State,Other Postal Code,Other Country,Callback,Car Phone,ISDN,Radio Phone,TTY/TDD Phone,Telex,User 1,User 2,User 3,User 4,Keywords,Mileage,Hobby,Billing Information,Directory Server,Sensitivity,Priority,Private,Categories\n");
        csvBuilder.append("John,,Doe,,,,,,,,,,,,john.doe@sparcedge.com,,,,,,,,,,,,,,,,,,,,,,,,843-55-5555,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Normal,,Most Contacted,");
        InputStream is = new ByteArrayInputStream(csvBuilder.toString().getBytes());

        MultipartFile mockFile = new MockMultipartFile("outlookContacts", "outlookContacts.csv", "plain/text", is);
        List<InviteeForm> inviteeForms = invitationService.parseContactsOutlook(mockFile);
        assertTrue(inviteeForms.size() == 1);
        assertTrue(inviteeForms.get(0).getEmail().equals("john.doe@sparcedge.com"));
    }

    private Invitation getInvitation() {
        User user = getUser();

        Organization organization = getOrganization();

        Domain orgDomain = domainService.get(organization.getId());
        Assert.assertNotNull(orgDomain);
        Assert.assertTrue(orgDomain.getName().equals("Test Organization"));

        Invitation invitation = new Invitation();
        invitation.setDomain(orgDomain);
        invitation.setEmail(user.getEmail());
        Role role = roleService.getRoleByAuthority(UserRole.ROLE_ORG_USER.toString());
        invitation.setRole(role);
        return invitation;
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
}
