package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.DomainRequestService;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.enums.DeviceType;
import com.sparc.knappsack.enums.Language;
import com.sparc.knappsack.forms.DomainRequestForm;
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

import static junit.framework.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DomainRequestValidatorTest {

    private static final String EMAIL_PATTERN = "[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9-]+(.[A-Za-z0-9-]+)*";

    @Mock
    private DomainService domainService;

    @Mock
    private DomainRequestService domainRequestService;

    @InjectMocks
    private DomainRequestValidator validator = new DomainRequestValidator();

    private DomainRequestForm form;
    private Errors errors;

    @Before
    public void setup() {
        form = new DomainRequestForm();
        errors = new BeanPropertyBindingResult(form, "domainRegistrationForm");
        ReflectionTestUtils.setField(validator, "emailPattern", EMAIL_PATTERN);
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(form.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {
        Domain domain = new Organization();
        domain.setId(1L);
        domain.getUuid();

        form.setDomainUUID(domain.getUuid());
        form.setAddress("testAddress");
        form.setCompanyName("testCompany");
        form.setEmailAddress("test@test.com");
        form.setFirstName("firstName");
        form.setLastName("lastName");
        form.setRegion(1L);
        form.setDeviceType(DeviceType.IPAD_1);
        form.setPhoneNumber("1231231234");
        form.getLanguages().add(Language.ENGLISH);

        Mockito.when(domainService.getByUUID(form.getDomainUUID())).thenReturn(domain);
        Mockito.when(domainService.getDomainForRegion(form.getRegion())).thenReturn(domain);
        Mockito.when(domainRequestService.doesDomainRequestExist(domain.getId(), form.getEmailAddress())).thenReturn(false);

        validator.validate(form, errors);
        assertEquals(errors.getErrorCount(), 0);

    }

    @Test
    public void testInvalid() {
        Domain domain = new Organization();
        domain.getUuid();

        Mockito.when(domainService.getByUUID(form.getDomainUUID())).thenReturn(null);
        Mockito.when(domainService.getDomainForRegion(form.getRegion())).thenReturn(null);
        Mockito.when(domainRequestService.doesDomainRequestExist(domain.getId(), form.getEmailAddress())).thenReturn(true);

        validator.validate(form, errors);
        assertEquals(errors.getErrorCount(), 10);

        setup();

        Mockito.when(domainService.getByUUID(form.getDomainUUID())).thenReturn(null);
        Mockito.when(domainService.getDomainForRegion(form.getRegion())).thenReturn(null);
        Mockito.when(domainRequestService.doesDomainRequestExist(domain.getId(), form.getEmailAddress())).thenReturn(true);

        validator.validate(form, errors);
        assertEquals(errors.getErrorCount(), 10);
    }

    @Test
    public void testRequestExists() {
        Domain domain = new Organization();
        domain.setId(1L);
        domain.getUuid();

        form.setDomainUUID(domain.getUuid());
        form.setAddress("testAddress");
        form.setCompanyName("testCompany");
        form.setEmailAddress("test@test.com");
        form.setFirstName("firstName");
        form.setLastName("lastName");
        form.setRegion(1L);
        form.setDeviceType(DeviceType.IPAD_1);
        form.setPhoneNumber("1231231234");
        form.getLanguages().add(Language.ENGLISH);

        Mockito.when(domainService.getByUUID(form.getDomainUUID())).thenReturn(domain);
        Mockito.when(domainService.getDomainForRegion(form.getRegion())).thenReturn(domain);
        Mockito.when(domainRequestService.doesDomainRequestExist(domain.getId(), form.getEmailAddress())).thenReturn(true);

        validator.validate(form, errors);
        assertEquals(errors.getErrorCount(), 1);
        assertEquals(errors.getGlobalErrorCount(), 1);
    }

}
