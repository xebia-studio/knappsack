package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.OrgStorageConfigService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.forms.OrganizationForm;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static junit.framework.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationValidatorTest {

    private static final String EMAIL_PATTERN = "[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9-]+(.[A-Za-z0-9-]+)*";

    @Mock
    private OrgStorageConfigService orgStorageConfigService;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private OrganizationValidator validator = new OrganizationValidator();

    private Errors errors;
    private OrganizationForm organizationForm;

    @Before
    public void setup() {
        organizationForm = new OrganizationForm();
        errors = new BeanPropertyBindingResult(organizationForm, "organizationForm");
        ReflectionTestUtils.setField(validator, "emailPattern", EMAIL_PATTERN);

    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(organizationForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {
        organizationForm.setName("name");
        organizationForm.setStorageConfigurationId(1L);
        organizationForm.setStoragePrefix("prefix");
//        organizationForm.setAdminEmail("valid@valid.com");
        organizationForm.setId(1L);

        Organization organization = new Organization();
        organization.setId(1L);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.setOrganization(organization);

        Mockito.when(organizationService.getByName(organizationForm.getName())).thenReturn(organization);
        Mockito.when(orgStorageConfigService.getByPrefix(organizationForm.getStoragePrefix())).thenReturn(orgStorageConfig);

        validator.validate(organizationForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testAllFieldsEmpty() {
        Mockito.when(organizationService.getByName(Matchers.anyString())).thenReturn(null);
        Mockito.when(orgStorageConfigService.getByPrefix(Matchers.anyString())).thenReturn(null);

        validator.validate(organizationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("storagePrefix"));
        assertNotNull(errors.getFieldError("storageConfigurationId"));

        setup();

        organizationForm.setName("");
        organizationForm.setStoragePrefix("");
        organizationForm.setStorageConfigurationId(0L);

        Mockito.when(organizationService.getByName(Matchers.anyString())).thenReturn(null);
        Mockito.when(orgStorageConfigService.getByPrefix(Matchers.anyString())).thenReturn(null);

        validator.validate(organizationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("storagePrefix"));
        assertNotNull(errors.getFieldError("storageConfigurationId"));

        setup();

        organizationForm.setEditing(true);
        Mockito.when(organizationService.getByName(Matchers.anyString())).thenReturn(null);
        Mockito.when(orgStorageConfigService.getByPrefix(Matchers.anyString())).thenReturn(null);

        validator.validate(organizationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("name"));
//        assertNotNull(errors.getFieldError("storagePrefix"));
    }

    @Test
    public void testNameAlreadyExists() {

        //Adding new organization
        organizationForm.setName("name");
        organizationForm.setStoragePrefix("prefix");
        organizationForm.setStorageConfigurationId(1L);

        Organization organization = new Organization();
        organization.setId(1L);

        Mockito.when(organizationService.getByName(organizationForm.getName())).thenReturn(organization);
        Mockito.when(orgStorageConfigService.getByPrefix(Matchers.anyString())).thenReturn(null);

        validator.validate(organizationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("name"));

        setup();

        //Editing
        organizationForm.setName("name");
        organizationForm.setStoragePrefix("prefix");
        organizationForm.setStorageConfigurationId(1L);
        organizationForm.setId(2L);

        organization = new Organization();
        organization.setId(1L);

        Mockito.when(organizationService.getByName(organizationForm.getName())).thenReturn(organization);
        Mockito.when(orgStorageConfigService.getByPrefix(Matchers.anyString())).thenReturn(null);

        validator.validate(organizationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("name"));
    }

    @Test
    public void testPrefixAlreadyExists() {
        organizationForm.setName("name");
        organizationForm.setStorageConfigurationId(1L);
        organizationForm.setStoragePrefix("prefix");

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        Organization organization = new Organization();
        organization.setId(1L);
        orgStorageConfig.setOrganization(organization);

        Mockito.when(organizationService.getByName(organizationForm.getName())).thenReturn(null);
        Mockito.when(orgStorageConfigService.getByPrefix(organizationForm.getStoragePrefix())).thenReturn(orgStorageConfig);

        validator.validate(organizationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("storagePrefix"));
    }

//    @Test
//        public void testInvalidEmailAddress() {
//            organizationForm.setAdminEmail("invalidEmailAddress");
//            validator.validate(organizationForm, errors);
//
//            assertTrue(errors.hasErrors());
//            assertNotNull(errors.getFieldError("adminEmail"));
//        }
}
