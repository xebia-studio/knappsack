package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationVersionValidatorTest {

  @Mock
  private ApplicationVersionService applicationVersionService;
  @Mock
  private ApplicationService applicationService;
  @Mock
  private UserService userService;
  @InjectMocks
  private ApplicationVersionValidator validator = new ApplicationVersionValidator();
  @Mock
  private MockMultipartFile mockMultipartFile;
  @Mock
  private GroupService groupService;
  @Mock
  private OrganizationService organizationService;
  private Errors errors;
  private ApplicationVersionForm applicationVersionForm;
  private User user = mock(User.class);

  @Before
  public void setup() {
    applicationVersionForm = new ApplicationVersionForm();
    errors = new BeanPropertyBindingResult(applicationVersionForm, "applicationVersionForm");
    when(userService.getUserFromSecurityContext()).thenReturn(user);
  }

  @Test
  public void testValidatorSupportsClass() {
    assertTrue(validator.supports(applicationVersionForm.getClass()));
  }

  @Test
  public void testValidatorNotSupportsClass() {
    assertFalse(validator.supports(String.class));
  }

  @Test
  public void testValid() {
    applicationVersionForm.setVersionName("name");
    applicationVersionForm.setRecentChanges("changes");
    applicationVersionForm.setAppState(AppState.GROUP_PUBLISH);
    applicationVersionForm.setAppFile(mockMultipartFile);

    Organization organization = new Organization();
    organization.setId(1l);
    organization.setDomainConfiguration(new DomainConfiguration());

    Group group = new Group();
    group.setId(1l);
    group.setDomainConfiguration(new DomainConfiguration());
    group.setOrganization(organization);

    Application parentApplication = new Application();
    parentApplication.setApplicationType(ApplicationType.ANDROID);

    when(applicationService.get(Matchers.anyLong())).thenReturn(parentApplication);
    when(mockMultipartFile.getOriginalFilename()).thenReturn(".apk");
    when(organizationService.getForGroupId(group.getId())).thenReturn(organization);
    when(user.getActiveOrganization()).thenReturn(organization);

//        Mockito.when(organizationService.get(group.getOrganization().getId())).thenReturn(organization);

    validator.validate(applicationVersionForm, errors);

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
    applicationVersionForm.setVersionName("");
    applicationVersionForm.setRecentChanges("");
    applicationVersionForm.setAppFile(mockMultipartFile);

    Organization organization = new Organization();
    organization.setId(1l);
    organization.setDomainConfiguration(new DomainConfiguration());

    Group group = new Group();
    group.setId(1l);
    group.setDomainConfiguration(new DomainConfiguration());
    group.setOrganization(organization);

    when(mockMultipartFile.isEmpty()).thenReturn(true);
    when(organizationService.getForGroupId(group.getId())).thenReturn(organization);
    when(user.getActiveOrganization()).thenReturn(organization);

    Application application = mock(Application.class);
    when(application.getApplicationType()).thenReturn(ApplicationType.ANDROID);
    when(applicationService.get(anyLong())).thenReturn(application);

    validator.validate(applicationVersionForm, errors);

    assertTrue(errors.hasErrors());
    Assert.assertEquals(errors.getErrorCount(), 4);
    assertNotNull(errors.getFieldError("versionName"));
    assertNotNull(errors.getFieldError("recentChanges"));
    assertNotNull(errors.getFieldError("appState"));
    assertNotNull(errors.getFieldError("appFile"));
  }

  @Test
  public void testAllFieldsInvalidEditing() {
    applicationVersionForm.setEditing(true);
//        validator.validate(uploadApplicationVersion, errors);

//        assertTrue(errors.hasErrors());
//        Assert.assertEquals(errors.getErrorCount(), 3);
//        assertNotNull(errors.getFieldError("versionName"));
//        assertNotNull(errors.getFieldError("recentChanges"));
//        assertNotNull(errors.getFieldError("appState"));

    setup();
    applicationVersionForm.setEditing(true);
    applicationVersionForm.setAppFile(mockMultipartFile);

    Organization organization = new Organization();
    organization.setId(1l);
    organization.setDomainConfiguration(new DomainConfiguration());

    Group group = new Group();
    group.setId(1l);
    group.setDomainConfiguration(new DomainConfiguration());
    group.setOrganization(organization);

    ApplicationVersion appVersion = mock(ApplicationVersion.class);
    appVersion.setAppState(AppState.GROUP_PUBLISH);

    when(applicationVersionService.get(applicationVersionForm.getId())).thenReturn(appVersion);
    when(mockMultipartFile.isEmpty()).thenReturn(true);
    when(organizationService.getForGroupId(group.getId())).thenReturn(organization);
    when(user.getActiveOrganization()).thenReturn(organization);

    Application application = mock(Application.class);
    when(application.getApplicationType()).thenReturn(ApplicationType.ANDROID);
    when(applicationService.get(anyLong())).thenReturn(application);

    validator.validate(applicationVersionForm, errors);

    assertTrue(errors.hasErrors());
  }

  @Test
  public void testInvalidAppFileExtensions() {
    for (ApplicationType applicationType : ApplicationType.values()) {
      if (ApplicationType.OTHER.equals(applicationType)) {
        continue;
      }
      applicationVersionForm.setAppFile(mockMultipartFile);

      Application parentApplication = new Application();
      parentApplication.setApplicationType(applicationType);


      Organization organization = new Organization();
      organization.setId(1l);
      organization.setDomainConfiguration(new DomainConfiguration());

      Group group = new Group();
      group.setId(1l);
      group.setDomainConfiguration(new DomainConfiguration());
      group.setOrganization(organization);


      when(applicationService.get(Matchers.anyLong())).thenReturn(parentApplication);
      when(mockMultipartFile.getOriginalFilename()).thenReturn(".invalid");
      when(organizationService.getForGroupId(group.getId())).thenReturn(organization);
      when(user.getActiveOrganization()).thenReturn(organization);

      validator.validate(applicationVersionForm, errors);

      assertTrue(errors.hasErrors());
      assertNotNull(errors.getFieldError("appFile"));

      setup();
    }
  }

  @Test
  public void testInvalidAppFileExtensionsEditing() {
    for (ApplicationType applicationType : ApplicationType.values()) {
      if (ApplicationType.OTHER.equals(applicationType)) {
        continue;
      }
      applicationVersionForm.setEditing(true);
      applicationVersionForm.setAppFile(mockMultipartFile);

      Application parentApplication = new Application();
      parentApplication.setApplicationType(applicationType);


      Organization organization = new Organization();
      organization.setId(1l);
      organization.setDomainConfiguration(new DomainConfiguration());

      Group group = new Group();
      group.setId(1l);
      group.setDomainConfiguration(new DomainConfiguration());
      group.setOrganization(organization);

      ApplicationVersion appVersion = mock(ApplicationVersion.class);
      appVersion.setAppState(AppState.GROUP_PUBLISH);
      when(applicationVersionService.get(applicationVersionForm.getId())).thenReturn(appVersion);


      when(applicationService.get(Matchers.anyLong())).thenReturn(parentApplication);
      when(mockMultipartFile.getOriginalFilename()).thenReturn(".invalid");
      when(organizationService.getForGroupId(group.getId())).thenReturn(organization);

      validator.validate(applicationVersionForm, errors);

      assertTrue(errors.hasErrors());
      assertNotNull(errors.getFieldError("appFile"));

      setup();
    }
  }
}
