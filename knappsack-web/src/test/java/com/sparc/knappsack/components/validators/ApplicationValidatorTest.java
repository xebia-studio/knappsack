package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.UploadApplication;
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
import org.springframework.web.multipart.MultipartFile;

import static junit.framework.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationValidatorTest {

    @Mock
    private ImageValidator mockImageValidator;

    @InjectMocks
    private ApplicationValidator validator = new ApplicationValidator();

    @Mock private MockMultipartFile mockMultipartFile;

    private Errors errors;
    private UploadApplication uploadApplication;

    @Before
    public void setup() {
        uploadApplication = new UploadApplication();
        errors = new BeanPropertyBindingResult(uploadApplication, "uploadApplication");
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(uploadApplication.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {
        uploadApplication.setApplicationType(ApplicationType.ANDROID);
        uploadApplication.setCategoryId(1L);
        uploadApplication.setDescription("test description");
        uploadApplication.setName("name");
        uploadApplication.setIcon(mockMultipartFile);

        Mockito.when(mockImageValidator.isValidImageSize(Matchers.any(MultipartFile.class))).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageType(Matchers.any(MultipartFile.class))).thenReturn(true);
        Mockito.when(mockImageValidator.isValidIconDimension(Matchers.any(MultipartFile.class))).thenReturn(true);

        validator.validate(uploadApplication, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testAllFieldsInvalid() {
        validator.validate(uploadApplication, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 4);
        assertNotNull(errors.getFieldError("applicationType"));
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("description"));
        assertNotNull(errors.getFieldError("categoryId"));

        //Reset test
        setup();

        uploadApplication.setName("");
        uploadApplication.setDescription("");

        validator.validate(uploadApplication, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 4);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("description"));
        assertNotNull(errors.getFieldError("categoryId"));
        assertNotNull(errors.getFieldError("applicationType"));

    }

    @Test
    public void testInvalidIcon() {
        uploadApplication.setApplicationType(ApplicationType.ANDROID);
        uploadApplication.setCategoryId(1L);
        uploadApplication.setDescription("test description");
        uploadApplication.setName("name");
        uploadApplication.setIcon(mockMultipartFile);

        Mockito.when(mockImageValidator.isValidImageSize(Matchers.any(MultipartFile.class))).thenReturn(false);
        Mockito.when(mockImageValidator.isValidImageType(Matchers.any(MultipartFile.class))).thenReturn(false);
        Mockito.when(mockImageValidator.isValidIconDimension(Matchers.any(MultipartFile.class))).thenReturn(false);

        validator.validate(uploadApplication, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        Assert.assertEquals(errors.getFieldErrorCount("icon"), 3);
    }

    @Test
    public void testInvalidScreenShot() {
        MockMultipartFile screenShot1 = Mockito.mock(MockMultipartFile.class);
        MockMultipartFile screenShot2 = Mockito.mock(MockMultipartFile.class);
        MockMultipartFile screenShot3 = Mockito.mock(MockMultipartFile.class);

        uploadApplication.setApplicationType(ApplicationType.ANDROID);
        uploadApplication.setCategoryId(1L);
        uploadApplication.setDescription("test description");
        uploadApplication.setName("name");
        uploadApplication.getScreenShots().add(screenShot1);
        uploadApplication.getScreenShots().add(screenShot2);
        uploadApplication.getScreenShots().add(screenShot3);

        Mockito.when(mockImageValidator.isValidImageSize(screenShot1)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageType(screenShot1)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageSize(screenShot2)).thenReturn(false);
        Mockito.when(mockImageValidator.isValidImageType(screenShot2)).thenReturn(false);
        Mockito.when(mockImageValidator.isValidImageSize(screenShot3)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageType(screenShot3)).thenReturn(true);

        validator.validate(uploadApplication, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 2);
        Assert.assertEquals(errors.getFieldErrorCount("screenShots[1]"), 2);
    }

}
