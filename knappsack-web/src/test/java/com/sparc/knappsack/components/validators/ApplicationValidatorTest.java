package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.ApplicationForm;
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
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationValidatorTest {

    @Mock
    private ImageValidator mockImageValidator;

    @Mock
    private UserService mockUserService;

    @Mock
    private OrganizationService mockOrganizationService;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private ApplicationVersionValidator mockApplicationVersionValidator;

    @InjectMocks
    private ApplicationValidator validator = new ApplicationValidator();

    @Mock private MockMultipartFile mockMultipartFile;

    private Errors errors;
    private ApplicationForm applicationForm;
    private User user = mock(User.class);

    @Before
    public void setup() {
        applicationForm = new ApplicationForm();
        errors = new BeanPropertyBindingResult(applicationForm, "applicationForm");
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(applicationForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {
        applicationForm.setApplicationType(ApplicationType.ANDROID);
        applicationForm.setCategoryId(1L);
        applicationForm.setDescription("test description");
        applicationForm.setName("name");
        applicationForm.setIcon(mockMultipartFile);
        BufferedImage mockBufferedImage = mock(BufferedImage.class);

        when(mockImageValidator.createBufferedImage(Matchers.any(MultipartFile.class))).thenReturn(mockBufferedImage);
        when(mockImageValidator.isValidImageSize(Matchers.any(MultipartFile.class), Matchers.anyLong())).thenReturn(true);
        when(mockImageValidator.isValidImageType(Matchers.any(MultipartFile.class))).thenReturn(true);
        when(mockImageValidator.isValidMinDimensions(Matchers.any(BufferedImage.class), Matchers.anyLong(), Matchers.anyLong())).thenReturn(true);
        when(mockImageValidator.isSquare(Matchers.any(BufferedImage.class))).thenReturn(true);
        when(mockUserService.getUserFromSecurityContext()).thenReturn(user);
        when(mockGroupService.get(anyLong())).thenReturn(new Group());
        doNothing().when(mockApplicationVersionValidator).validateVersionName(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateRecentChanges(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateOrganizationLimits(any(Errors.class), any(ApplicationVersionForm.class), any(Application.class), any(Organization.class));
        doNothing().when(mockApplicationVersionValidator).validateInstallFile(any(Errors.class), any(ApplicationVersionForm.class), any(ApplicationType.class));
        doNothing().when(mockApplicationVersionValidator).validateResign(any(ApplicationVersionForm.class), any(Group.class), any(ApplicationType.class), any(Errors.class));

        validator.validate(applicationForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testAllFieldsInvalid() {
        when(mockUserService.getUserFromSecurityContext()).thenReturn(user);
        when(mockGroupService.get(anyLong())).thenReturn(null);
        doNothing().when(mockApplicationVersionValidator).validateVersionName(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateRecentChanges(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateOrganizationLimits(any(Errors.class), any(ApplicationVersionForm.class), any(Application.class), any(Organization.class));
        doNothing().when(mockApplicationVersionValidator).validateInstallFile(any(Errors.class), any(ApplicationVersionForm.class), any(ApplicationType.class));
        doNothing().when(mockApplicationVersionValidator).validateResign(any(ApplicationVersionForm.class), any(Group.class), any(ApplicationType.class), any(Errors.class));

        validator.validate(applicationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 4);
        assertNotNull(errors.getFieldError("applicationType"));
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("description"));
        assertNotNull(errors.getFieldError("categoryId"));

        //Reset test
        setup();

        applicationForm.setName("");
        applicationForm.setDescription("");

        validator.validate(applicationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 4);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("description"));
        assertNotNull(errors.getFieldError("categoryId"));
        assertNotNull(errors.getFieldError("applicationType"));

    }

    @Test
    public void testInvalidIcon() {
        when(mockUserService.getUserFromSecurityContext()).thenReturn(user);
        when(mockGroupService.get(anyLong())).thenReturn(null);
        doNothing().when(mockApplicationVersionValidator).validateVersionName(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateRecentChanges(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateOrganizationLimits(any(Errors.class), any(ApplicationVersionForm.class), any(Application.class), any(Organization.class));
        doNothing().when(mockApplicationVersionValidator).validateInstallFile(any(Errors.class), any(ApplicationVersionForm.class), any(ApplicationType.class));
        doNothing().when(mockApplicationVersionValidator).validateResign(any(ApplicationVersionForm.class), any(Group.class), any(ApplicationType.class), any(Errors.class));

        applicationForm.setApplicationType(ApplicationType.ANDROID);
        applicationForm.setCategoryId(1L);
        applicationForm.setDescription("test description");
        applicationForm.setName("name");
        applicationForm.setIcon(mockMultipartFile);

        BufferedImage mockBufferedImage = mock(BufferedImage.class);

        when(mockImageValidator.createBufferedImage(Matchers.any(MultipartFile.class))).thenReturn(mockBufferedImage);
        when(mockImageValidator.isValidImageSize(Matchers.any(MultipartFile.class), Matchers.anyLong())).thenReturn(false);
        when(mockImageValidator.isValidImageType(Matchers.any(MultipartFile.class))).thenReturn(false);
        when(mockImageValidator.isValidMinDimensions(Matchers.any(BufferedImage.class), Matchers.anyLong(), Matchers.anyLong())).thenReturn(false);

        validator.validate(applicationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        Assert.assertEquals(errors.getFieldErrorCount("icon"), 3);
    }

    @Test
    public void testInvalidScreenShot() {
        when(mockUserService.getUserFromSecurityContext()).thenReturn(user);
        when(mockGroupService.get(anyLong())).thenReturn(null);
        doNothing().when(mockApplicationVersionValidator).validateVersionName(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateRecentChanges(any(Errors.class), any(ApplicationVersionForm.class));
        doNothing().when(mockApplicationVersionValidator).validateOrganizationLimits(any(Errors.class), any(ApplicationVersionForm.class), any(Application.class), any(Organization.class));
        doNothing().when(mockApplicationVersionValidator).validateInstallFile(any(Errors.class), any(ApplicationVersionForm.class), any(ApplicationType.class));
        doNothing().when(mockApplicationVersionValidator).validateResign(any(ApplicationVersionForm.class), any(Group.class), any(ApplicationType.class), any(Errors.class));

        MockMultipartFile screenShot1 = mock(MockMultipartFile.class);
        MockMultipartFile screenShot2 = mock(MockMultipartFile.class);
        MockMultipartFile screenShot3 = mock(MockMultipartFile.class);

        applicationForm.setApplicationType(ApplicationType.ANDROID);
        applicationForm.setCategoryId(1L);
        applicationForm.setDescription("test description");
        applicationForm.setName("name");
        applicationForm.getScreenshots().add(screenShot1);
        applicationForm.getScreenshots().add(screenShot2);
        applicationForm.getScreenshots().add(screenShot3);

        BufferedImage mockBufferedImage = mock(BufferedImage.class);

        when(mockImageValidator.createBufferedImage(Matchers.any(MultipartFile.class))).thenReturn(mockBufferedImage);
        when(mockImageValidator.isValidImageSize(screenShot1, 819200)).thenReturn(true);
        when(mockImageValidator.isValidImageType(screenShot1)).thenReturn(true);
        when(mockImageValidator.isValidImageSize(screenShot2, 819200)).thenReturn(false);
        when(mockImageValidator.isValidImageType(screenShot2)).thenReturn(false);
        when(mockImageValidator.isValidImageSize(screenShot3, 819200)).thenReturn(true);
        when(mockImageValidator.isValidImageType(screenShot3)).thenReturn(true);

        validator.validate(applicationForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 2);
        Assert.assertEquals(errors.getFieldErrorCount("screenshots[1]"), 2);
    }

}
