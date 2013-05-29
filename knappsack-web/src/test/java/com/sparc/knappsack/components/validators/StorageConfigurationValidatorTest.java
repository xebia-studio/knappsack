package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.S3StorageConfiguration;
import com.sparc.knappsack.components.services.StorageConfigurationService;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;
import junit.framework.Assert;
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
public class StorageConfigurationValidatorTest {
    @Mock
    private StorageConfigurationService storageConfigurationService;

    @InjectMocks
    private StorageConfigurationValidator validator = new StorageConfigurationValidator();

    private Errors errors;
    private StorageForm storageForm;

    @Before
    public void setup() {
        storageForm = new StorageForm();
        errors = new BeanPropertyBindingResult(storageForm, "storageForm");
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(storageForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValidLocal() {
        storageForm.setName("name");
        storageForm.setBaseLocation("baseLocation");
        storageForm.setStorageType(StorageType.LOCAL);
        storageForm.setId(1L);
        storageForm.setEditing(false);

        LocalStorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        ReflectionTestUtils.setField(storageConfiguration, "id", 1L);

        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(storageConfiguration);

        validator.validate(storageForm, errors);

        assertFalse(errors.hasErrors());

    }

    @Test
    public void testInvalidLocal() {
        Mockito.when(storageConfigurationService.getByName((storageForm.getName()))).thenReturn(null);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("baseLocation"));
        assertNotNull(errors.getFieldError("storageType"));

        setup();

        storageForm.setName("");
        storageForm.setBaseLocation("");

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("baseLocation"));
        assertNotNull(errors.getFieldError("storageType"));
    }

    @Test
    public void testNameAlreadyExists() {
        storageForm.setName("name");
        storageForm.setBaseLocation("baseLocation");
        storageForm.setStorageType(StorageType.LOCAL);

        LocalStorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        storageConfiguration.setBaseLocation("baseLocation");
        storageConfiguration.setName("name");
        storageConfiguration.setStorageType(StorageType.LOCAL);
        ReflectionTestUtils.setField(storageConfiguration, "id", 1L);

        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(storageConfiguration);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("name"));
    }

    @Test
    public void testEdit() {
        storageForm.setId(1l);
        storageForm.setName("name");
        storageForm.setBaseLocation("new/baselocation");
        storageForm.setStorageType(StorageType.LOCAL);
        storageForm.setEditing(true);

        LocalStorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        storageConfiguration.setId(1l);
        storageConfiguration.setBaseLocation("baseLocation");
        storageConfiguration.setName("name");
        storageConfiguration.setStorageType(StorageType.LOCAL);

        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(storageConfiguration);
        Mockito.when(storageConfigurationService.get(storageForm.getId())).thenReturn(storageConfiguration);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
        assertNotNull(errors.getFieldError("baseLocation"));
    }

    @Test
    public void testValidAmazon() {
        storageForm.setName("name");
        storageForm.setBaseLocation("baseLocation");
        storageForm.setStorageType(StorageType.AMAZON_S3);
        storageForm.setAccessKey("accessKey");
        storageForm.setSecretKey("secretKey");
        storageForm.setBucketName("bucketName");

        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(null);

        validator.validate(storageForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testEditAmazon() {
        storageForm.setId(1l);
        storageForm.setName("name");
        storageForm.setBaseLocation("baseLocation");
        storageForm.setStorageType(StorageType.AMAZON_S3);
        storageForm.setBucketName("New Bucket Name");
        storageForm.setAccessKey("123");
        storageForm.setSecretKey("123");
        storageForm.setEditing(true);

        S3StorageConfiguration storageConfiguration = new S3StorageConfiguration();
        storageConfiguration.setId(1l);
        storageConfiguration.setBaseLocation("baseLocation");
        storageConfiguration.setName("name");
        storageConfiguration.setBucketName("Bucket Name");
        storageConfiguration.setAccessKey("123");
        storageConfiguration.setSecretKey("123");
        storageConfiguration.setStorageType(StorageType.LOCAL);

        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(storageConfiguration);
        Mockito.when(storageConfigurationService.get(storageForm.getId())).thenReturn(storageConfiguration);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 1);
//        assertNotNull(errors.getFieldError("bucketName"));
        assertNotNull(errors.getFieldError("storageType"));
    }

    @Test
    public void testInvalidAmazon() {
        storageForm.setStorageType(StorageType.AMAZON_S3);
        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(null);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 5);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("baseLocation"));
        assertNotNull(errors.getFieldError("accessKey"));
        assertNotNull(errors.getFieldError("secretKey"));
        assertNotNull(errors.getFieldError("bucketName"));

        setup();

        storageForm.setStorageType(StorageType.AMAZON_S3);
        storageForm.setName("");
        storageForm.setBaseLocation("");
        storageForm.setAccessKey("");
        storageForm.setSecretKey("");
        storageForm.setBucketName("");
        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(null);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 5);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("baseLocation"));
        assertNotNull(errors.getFieldError("accessKey"));
        assertNotNull(errors.getFieldError("secretKey"));
        assertNotNull(errors.getFieldError("bucketName"));
    }

    @Test
    public void testInvalidEdit() {
        storageForm.setId(1l);
        storageForm.setName("name");
        storageForm.setBaseLocation("baseLocation");
        storageForm.setStorageType(StorageType.AMAZON_S3);
        storageForm.setBucketName("New Bucket Name");
        storageForm.setAccessKey("123");
        storageForm.setSecretKey("123");
        storageForm.setEditing(true);

        Mockito.when(storageConfigurationService.getByName(storageForm.getName())).thenReturn(null);
        Mockito.when(storageConfigurationService.get(storageForm.getId())).thenReturn(null);

        validator.validate(storageForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(errors.getGlobalErrorCount(), 1);
    }
}
