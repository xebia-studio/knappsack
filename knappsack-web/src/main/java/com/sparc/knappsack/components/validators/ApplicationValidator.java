package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.DomainConfiguration;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.forms.ApplicationForm;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.List;

@Component("applicationValidator")
public class ApplicationValidator implements Validator {

    private static final String APPLICATION_TYPE_FIELD = "applicationType";
    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String CATEGORY_ID_FIELD = "categoryId";
    private static final String ICON_FIELD = "icon";

    @Qualifier("imageValidator")
    @Autowired(required = true)
    private ImageValidator imageValidator;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("applicationVersionValidator")
    @Autowired(required = true)
    private ApplicationVersionValidator applicationVersionValidator;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ApplicationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ApplicationForm applicationForm = (ApplicationForm) target;

        validateApplicationLimit(applicationForm, errors);

        if (applicationForm.getApplicationType() == null) {
            errors.rejectValue(APPLICATION_TYPE_FIELD, "applicationValidator.emptyApplicationType");
        }

        if (applicationForm.getName() == null || !StringUtils.hasText(applicationForm.getName())) {
            errors.rejectValue(NAME_FIELD, "applicationValidator.emptyName");
        }

        if (applicationForm.getDescription() == null || !StringUtils.hasText(applicationForm.getDescription())) {
            errors.rejectValue(DESCRIPTION_FIELD, "applicationValidator.emptyDescription");
        }

        if (applicationForm.getCategoryId() == null) {
            errors.rejectValue(CATEGORY_ID_FIELD, "applicationValidator.categoryId");
        }

        BufferedImage bufferedImage = imageValidator.createBufferedImage(applicationForm.getIcon());

        if (applicationForm.getIcon() != null && !imageValidator.isValidImageSize(applicationForm.getIcon(), 819200 /*Bytes: 800 KB*/)) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconSize");
        }

        if (applicationForm.getIcon() != null && !imageValidator.isValidImageType(applicationForm.getIcon())) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconType");
        }

        if (applicationForm.getIcon() != null && (!imageValidator.isValidMinDimensions(bufferedImage, 72, 72) || !imageValidator.isSquare(bufferedImage))) {
            errors.rejectValue(ICON_FIELD, "validator.invalidIconDimension");
        }

        validateScreenShots(applicationForm, errors);

        validateApplicationVersion(applicationForm, errors);
    }

    private void validateApplicationLimit(ApplicationForm applicationForm, Errors errors) {
        boolean editing = applicationForm.getId() != null && applicationForm.getId() > 0;
        if(!editing && applicationForm.getGroupId() != null) {
            Organization organization = organizationService.getForGroupId(applicationForm.getGroupId());
            validateOrganizationApplicationLimit(organization, errors);
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

    private void validateScreenShots(ApplicationForm applicationForm, Errors errors) {
        List<MultipartFile> screenShots = applicationForm.getScreenshots();
        for (int i = 0; i < screenShots.size(); i++) {
            MultipartFile screenShot = screenShots.get(i);
            if (!imageValidator.isValidImageSize(screenShot, 819200 /*Bytes: 800 KB*/)) {
                errors.rejectValue("screenshots[" + i + "]", "applicationValidator.invalidScreenshotSize");
            }

            if (!imageValidator.isValidImageType(screenShot)) {
                errors.rejectValue("screenshots[" + i + "]", "applicationValidator.invalidScreenshotType");
            }
        }
    }

    private void validateApplicationVersion(ApplicationForm applicationForm, Errors errors) {
        if (applicationForm.getId() == null || applicationForm.getId() <= 0) {
            ApplicationVersionForm versionForm = applicationForm.getApplicationVersion();
            User user = userService.getUserFromSecurityContext();

            Errors versionErrors = new BeanPropertyBindingResult(versionForm, "applicationVersionForm");

            applicationVersionValidator.validateVersionName(versionErrors, versionForm);
            applicationVersionValidator.validateRecentChanges(versionErrors, versionForm);
            applicationVersionValidator.validateOrganizationLimits(versionErrors, versionForm, null, user.getActiveOrganization());
            applicationVersionValidator.validateInstallFile(versionErrors, versionForm, applicationForm.getApplicationType());
            applicationVersionValidator.validateResign(versionForm, groupService.get(applicationForm.getGroupId()), applicationForm.getApplicationType(), versionErrors);

            // Add all applicationVersion errors to application errors object
            if (versionErrors.hasErrors()) {
                for (FieldError error : versionErrors.getFieldErrors()) {
                    errors.rejectValue(String.format("applicationVersion.%s", error.getField()), error.getCode());
                }
                for (ObjectError error : versionErrors.getGlobalErrors()) {
                    errors.reject(error.getCode());
                }
            }
        }
    }
}
