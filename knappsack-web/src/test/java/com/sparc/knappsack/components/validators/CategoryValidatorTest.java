package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.forms.CategoryForm;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static junit.framework.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryValidatorTest {
    @Mock
    private ImageValidator mockImageValidator;

    @InjectMocks
    private CategoryValidator validator = new CategoryValidator();

    @Mock private MockMultipartFile mockMultipartFile;

    private Errors errors;
    private CategoryForm categoryForm;

    @Before
    public void setup() {
        categoryForm = new CategoryForm();
        errors = new BeanPropertyBindingResult(categoryForm, "categoryForm");
    }

    @Test
    public void testValidatorSupportsClass() {
        assertTrue(validator.supports(categoryForm.getClass()));
    }

    @Test
    public void testValidatorNotSupportsClass() {
        assertFalse(validator.supports(String.class));
    }

    @Test
    public void testValid() {
        categoryForm.setName("name");
        categoryForm.setDescription("description");
        categoryForm.setIcon(mockMultipartFile);

        Mockito.when(mockImageValidator.isValidIconDimension(mockMultipartFile)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageSize(mockMultipartFile)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageType(mockMultipartFile)).thenReturn(true);

        validator.validate(categoryForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testValidEditing() {
        categoryForm.setEditing(true);
        categoryForm.setName("name");
        categoryForm.setDescription("description");
        categoryForm.setIcon(mockMultipartFile);

        Mockito.when(mockImageValidator.isValidIconDimension(mockMultipartFile)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageSize(mockMultipartFile)).thenReturn(true);
        Mockito.when(mockImageValidator.isValidImageType(mockMultipartFile)).thenReturn(true);

        validator.validate(categoryForm, errors);

        assertFalse(errors.hasErrors());

        setup();

        categoryForm.setEditing(true);
        categoryForm.setName("name");
        categoryForm.setDescription("description");
        categoryForm.setIcon(mockMultipartFile);

        Mockito.when(mockMultipartFile.isEmpty()).thenReturn(true);

        validator.validate(categoryForm, errors);

        assertFalse(errors.hasErrors());

        setup();

        categoryForm.setEditing(true);
        categoryForm.setName("name");
        categoryForm.setDescription("description");

        validator.validate(categoryForm, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testAllFieldsInvalid() {
        validator.validate(categoryForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("icon"));
        assertNotNull(errors.getFieldError("description"));

        setup();

        categoryForm.setName("");
        categoryForm.setDescription("");
        categoryForm.setIcon(mockMultipartFile);

        Mockito.when(mockMultipartFile.isEmpty()).thenReturn(true);

        validator.validate(categoryForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        assertNotNull(errors.getFieldError("name"));
        assertNotNull(errors.getFieldError("icon"));
        assertNotNull(errors.getFieldError("description"));
    }

    @Test
    public void testInvalidIcon() {
        categoryForm.setName("name");
        categoryForm.setDescription("description");
        categoryForm.setIcon(mockMultipartFile);

        Mockito.when(mockImageValidator.isValidIconDimension(mockMultipartFile)).thenReturn(false);
        Mockito.when(mockImageValidator.isValidImageSize(mockMultipartFile)).thenReturn(false);
        Mockito.when(mockImageValidator.isValidImageType(mockMultipartFile)).thenReturn(false);

        validator.validate(categoryForm, errors);

        assertTrue(errors.hasErrors());
        Assert.assertEquals(errors.getErrorCount(), 3);
        Assert.assertEquals(errors.getFieldErrorCount("icon"), 3);
    }
}
