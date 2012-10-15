package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.DomainConfiguration;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.forms.UploadApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component("applicationValidator")
public class ApplicationValidator implements Validator {

    private static final String APPLICATION_TYPE_FIELD = "applicationType";
    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String CATEGORY_ID_FIELD = "categoryId";
    private static final String ICON_FIELD = "icon";

    @Autowired
    private ImageValidator imageValidator;

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UploadApplication.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UploadApplication uploadApplication = (UploadApplication) target;

        validateApplicationLimit(uploadApplication, errors);

        if (uploadApplication.getApplicationType() == null) {
            errors.rejectValue(APPLICATION_TYPE_FIELD, "applicationValidator.emptyApplicationType");
        }

        if (uploadApplication.getName() == null || !StringUtils.hasText(uploadApplication.getName())) {
            errors.rejectValue(NAME_FIELD, "applicationValidator.emptyName");
        }

        if (uploadApplication.getDescription() == null || !StringUtils.hasText(uploadApplication.getDescription())) {
            errors.rejectValue(DESCRIPTION_FIELD, "applicationValidator.emptyDescription");
        }

        if (uploadApplication.getCategoryId() == null) {
            errors.rejectValue(CATEGORY_ID_FIELD, "applicationValidator.categoryId");
        }

        if (uploadApplication.getIcon() != null && !imageValidator.isValidImageSize(uploadApplication.getIcon())) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconSize");
        }

        if (uploadApplication.getIcon() != null && !imageValidator.isValidImageType(uploadApplication.getIcon())) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconType");
        }

        if (uploadApplication.getIcon() != null && !imageValidator.isValidIconDimension(uploadApplication.getIcon())) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconDimension");
        }

        validateScreenShots(uploadApplication, errors);
    }

    private void validateApplicationLimit(UploadApplication uploadApplication, Errors errors) {
        if(!uploadApplication.isEditing() && uploadApplication.getGroupId() != null) {
            Group group = groupService.get(uploadApplication.getGroupId());
            validateGroupApplicationLimit(group, errors);
            validateOrganizationApplicationLimit(group.getOrganization(), errors);
        }
    }

    private void validateGroupApplicationLimit(Group group, Errors errors) {
        DomainConfiguration domainConfiguration = group.getDomainConfiguration();
        if(domainConfiguration.isDisabledDomain()) {
            errors.reject("applicationValidator.group.disabled");
        }

        if(domainConfiguration.isDisableLimitValidations()) {
            return;
        }

        if(groupService.isApplicationLimit(group)) {
            errors.reject("applicationValidator.group.applicationLimit");
        }
    }

    private void validateOrganizationApplicationLimit(Organization organization, Errors errors) {
        DomainConfiguration domainConfiguration = organization.getDomainConfiguration();
        if(domainConfiguration.isDisabledDomain()) {
            errors.reject("applicationValidator.organization.disabled");
        }

        if(domainConfiguration.isDisableLimitValidations()) {
            return;
        }

        if(organizationService.isApplicationLimit(organization)) {
            errors.reject("applicationValidator.organization.applicationLimit");
        }
    }

    private void validateScreenShots(UploadApplication uploadApplication, Errors errors) {
        List<MultipartFile> screenShots = uploadApplication.getScreenShots();
        for (int i = 0; i < screenShots.size(); i++) {
            MultipartFile screenShot = screenShots.get(i);
            if (!imageValidator.isValidImageSize(screenShot)) {
                errors.rejectValue("screenShots[" + i + "]", "applicationValidator.invalidScreenShotSize");
            }

            if (!imageValidator.isValidImageType(screenShot)) {
                errors.rejectValue("screenShots[" + i + "]", "applicationValidator.invalidScreenShotType");
            }
        }
    }
}
