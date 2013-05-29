package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.forms.CategoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;

@Component("categoryValidator")
public class CategoryValidator implements Validator {

    private static final String ICON_FIELD = "icon";
    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";

    @Autowired
    private ImageValidator imageValidator;

    @Override
    public boolean supports(Class<?> clazz) {
        return CategoryForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CategoryForm category = (CategoryForm) target;
        if (category.getName() == null || category.getName().isEmpty()) {
            errors.rejectValue(NAME_FIELD, "categoryValidator.emptyName");
        }

        if (category.getDescription() == null || category.getDescription().isEmpty()) {
            errors.rejectValue(DESCRIPTION_FIELD, "categoryValidator.emptyDescription");
        }

        if (category.isEditing()) {
            if (category.getIcon() != null && !category.getIcon().isEmpty()) {
                validateImageAttributes(category.getIcon(), errors);
            }
        } else {
            if (category.getIcon() == null || category.getIcon().isEmpty()) {
                errors.rejectValue(ICON_FIELD, "categoryValidator.emptyIcon");
            } else {
                validateImageAttributes(category.getIcon(), errors);
            }
        }
    }

    private void validateImageAttributes(MultipartFile icon, Errors errors) {

        BufferedImage bufferedImage = imageValidator.createBufferedImage(icon);

        if (!imageValidator.isValidImageSize(icon, 819200 /*Bytes: 800 KB*/)) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconSize");
        }

        if (!imageValidator.isValidImageType(icon)) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconType");
        }

        if (!imageValidator.isValidMinDimensions(bufferedImage, 72, 72) || !imageValidator.isSquare(bufferedImage)) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconDimension");
        }
    }
}
