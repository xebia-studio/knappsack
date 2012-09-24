package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static junit.framework.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationVersionValidatorTest {

    @Mock
    private ApplicationVersionService applicationVersionService;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationVersionValidator validator = new ApplicationVersionValidator();

    @Mock
    private MockMultipartFile mockMultipartFile;

    @Mock
    private GroupService groupService;

    @Mock
    private OrganizationService organizationService;

    private Errors errors;
    private UploadApplicationVersion uploadApplicationVersion;

    @Before
    public void setup() {
        uploadApplicationVersion = new UploadApplicationVersion();
        errors = new BeanPropertyBindingResult(uploadApplicationVersion, "uploadApplicationVersion");
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(uploadApplicationVersion.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {
        uploadApplicationVersion.setVersionName("name");
        uploadApplicationVersion.setRecentChanges("changes");
        uploadApplicationVersion.setAppState(AppState.GROUP_PUBLISH);
        uploadApplicationVersion.setAppFile(mockMultipartFile);
        uploadApplicationVersion.setGroupId(1l);

        Organization organization = new Organization();
        organization.setId(1l);
        organization.setDomainConfiguration(new DomainConfiguration());

        Group group = new Group();
        group.setId(1l);
        group.setDomainConfiguration(new DomainConfiguration());
        group.setOrganization(organization);

        Application parentApplication = new Application();
        parentApplication.setApplicationType(ApplicationType.ANDROID);

        Mockito.when(applicationService.get(Matchers.anyLong())).thenReturn(parentApplication);
        Mockito.when(mockMultipartFile.getOriginalFilename()).thenReturn(".apk");
        Mockito.when(groupService.get(uploadApplicationVersion.getGroupId())).thenReturn(group);
//        Mockito.when(organizationService.get(group.getOrganization().getId())).thenReturn(organization);

        validator.validate(uploadApplicationVersion, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testAllFieldsInvalid() {
//        validator.validate(uploadApplicationVersion, errors);
//
//        assertTrue(errors.hasErrors());
//        Assert.assertEquals(errors.getErrorCount(), 4);
//        assertNotNull(errors.getFieldError("versionName"));
//        assertNotNull(errors.getFieldError("recentChanges"));
//        assertNotNull(errors.getFieldError("appState"));
//        assertNotNull(errors.getFieldError("appFile"));

        setup();
        uploadApplicationVersion.setVersionName("");
        uploadApplicationVersion.setRecentChanges("");
        uploadApplicationVersion.setAppFile(mockMultipartFile);
        uploadApplicationVersion.setGroupId(1l);

        Organization organization = new Organization();
        organization.setId(1l);
        organization.setDomainConfiguration(new DomainConfiguration());

        Group group = new Group();
        group.setId(1l);
        group.setDomainConfiguration(new DomainConfiguration());
        group.setOrganization(organization);

        Mockito.when(groupService.get(uploadApplicationVersion.getGroupId())).thenReturn(group);
        Mockito.when(mockMultipartFile.isEmpty()).thenReturn(true);

        validator.validate(uploadApplicationVersion, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 4);
        assertNotNull(errors.getFieldError("versionName"));
        assertNotNull(errors.getFieldError("recentChanges"));
        assertNotNull(errors.getFieldError("appState"));
        assertNotNull(errors.getFieldError("appFile"));
    }

    @Test
    public void testAllFieldsInvalidEditing() {
        uploadApplicationVersion.setEditing(true);
//        validator.validate(uploadApplicationVersion, errors);

//        assertTrue(errors.hasErrors());
//        Assert.assertEquals(errors.getErrorCount(), 3);
//        assertNotNull(errors.getFieldError("versionName"));
//        assertNotNull(errors.getFieldError("recentChanges"));
//        assertNotNull(errors.getFieldError("appState"));

        setup();
        uploadApplicationVersion.setEditing(true);
        uploadApplicationVersion.setAppFile(mockMultipartFile);
        uploadApplicationVersion.setGroupId(1l);

        Organization organization = new Organization();
        organization.setId(1l);
        organization.setDomainConfiguration(new DomainConfiguration());

        Group group = new Group();
        group.setId(1l);
        group.setDomainConfiguration(new DomainConfiguration());
        group.setOrganization(organization);

        ApplicationVersion appVersion = Mockito.mock(ApplicationVersion.class);
        appVersion.setAppState(AppState.GROUP_PUBLISH);
        Mockito.when(applicationVersionService.get(uploadApplicationVersion.getId())).thenReturn(appVersion);

        Mockito.when(groupService.get(uploadApplicationVersion.getGroupId())).thenReturn(group);

        Mockito.when(mockMultipartFile.isEmpty()).thenReturn(true);

        validator.validate(uploadApplicationVersion, errors);

        assertTrue(errors.hasErrors());
    }

    @Test
    public void testInvalidAppFileExtensions() {
        for (ApplicationType applicationType : ApplicationType.values()) {
            uploadApplicationVersion.setAppFile(mockMultipartFile);

            Application parentApplication = new Application();
            parentApplication.setApplicationType(applicationType);

            uploadApplicationVersion.setGroupId(1l);

            Organization organization = new Organization();
            organization.setId(1l);
            organization.setDomainConfiguration(new DomainConfiguration());

            Group group = new Group();
            group.setId(1l);
            group.setDomainConfiguration(new DomainConfiguration());
            group.setOrganization(organization);

            Mockito.when(groupService.get(uploadApplicationVersion.getGroupId())).thenReturn(group);

            Mockito.when(applicationService.get(Matchers.anyLong())).thenReturn(parentApplication);
            Mockito.when(mockMultipartFile.getOriginalFilename()).thenReturn(".invalid");

            validator.validate(uploadApplicationVersion, errors);

            assertTrue(errors.hasErrors());
            assertNotNull(errors.getFieldError("appFile"));

            setup();
        }
    }

    @Test
    public void testInvalidAppFileExtensionsEditing() {
        for (ApplicationType applicationType : ApplicationType.values()) {
            uploadApplicationVersion.setEditing(true);
            uploadApplicationVersion.setAppFile(mockMultipartFile);

            Application parentApplication = new Application();
            parentApplication.setApplicationType(applicationType);

            uploadApplicationVersion.setGroupId(1l);

            Organization organization = new Organization();
            organization.setId(1l);
            organization.setDomainConfiguration(new DomainConfiguration());

            Group group = new Group();
            group.setId(1l);
            group.setDomainConfiguration(new DomainConfiguration());
            group.setOrganization(organization);

            ApplicationVersion appVersion = Mockito.mock(ApplicationVersion.class);
            appVersion.setAppState(AppState.GROUP_PUBLISH);
            Mockito.when(applicationVersionService.get(uploadApplicationVersion.getId())).thenReturn(appVersion);

            Mockito.when(groupService.get(uploadApplicationVersion.getGroupId())).thenReturn(group);

            Mockito.when(applicationService.get(Matchers.anyLong())).thenReturn(parentApplication);
            Mockito.when(mockMultipartFile.getOriginalFilename()).thenReturn(".invalid");

            validator.validate(uploadApplicationVersion, errors);

            assertTrue(errors.hasErrors());
            assertNotNull(errors.getFieldError("appFile"));

            setup();
        }
    }
}
