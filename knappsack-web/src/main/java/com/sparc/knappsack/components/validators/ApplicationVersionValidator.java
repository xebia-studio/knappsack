package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.ApplicationVersionService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ApplicationVersionValidator implements Validator {
    private static final double MEGABYTE_CONVERSION = 1048576;

    private static final String VERSION_NAME_FIELD = "versionName";
    private static final String RECENT_CHANGES_FIELD = "recentChanges";
    private static final String APP_STATE_FIELD = "appState";
    private static final String APP_FILE_FIELD = "appFile";

    @Autowired
    private ApplicationVersionService applicationVersionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UploadApplicationVersion.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UploadApplicationVersion version = (UploadApplicationVersion) target;

        if(!version.isEditing()) {
            validateApplicationVersionLimit(version, errors);
            Group group = groupService.get(version.getGroupId());
            validateGroupStorageLimit(group, version.getAppFile().getSize(), errors);
            validateOrganizationStorageLimit(group.getOrganization(), version.getAppFile().getSize(), errors);
        }

        if (version.getVersionName() == null || version.getVersionName().isEmpty()) {
            errors.rejectValue(VERSION_NAME_FIELD, "applicationVersionValidator.emptyVersion");
        }

        if (version.getRecentChanges() == null || version.getRecentChanges().isEmpty()) {
            errors.rejectValue(RECENT_CHANGES_FIELD, "applicationVersionValidator.emptyRecentChanges");
        }

        if (version.getAppState() == null) {
            errors.rejectValue(APP_STATE_FIELD, "applicationVersionValidator.emptyAppState");
        }

        if (version.isEditing()) {
            ApplicationVersion currentVersion = applicationVersionService.get(version.getId());
            if(!AppState.ORGANIZATION_PUBLISH.equals(currentVersion.getAppState()) && AppState.ORGANIZATION_PUBLISH.equals(version.getAppState())) {
                errors.rejectValue(APP_STATE_FIELD, "applicationVersionValidator.invalidAppState");
            }

            if (version.getAppFile() != null && !version.getAppFile().isEmpty()) {
                validateExtension(version, errors);
            }
        } else {
            if (version.getAppFile() == null || version.getAppFile().isEmpty()) {
                errors.rejectValue(APP_FILE_FIELD, "applicationVersionValidator.emptyInstallFile");
            } else {
                validateExtension(version, errors);
            }

            if(AppState.ORGANIZATION_PUBLISH.equals(version.getAppState())) {
                errors.rejectValue(APP_STATE_FIELD, "applicationVersionValidator.invalidAppState");
            }
        }
    }

    private void validateApplicationVersionLimit(UploadApplicationVersion uploadApplicationVersion, Errors errors) {
        Application application = applicationService.get(uploadApplicationVersion.getParentId());
        Group group = groupService.get(uploadApplicationVersion.getGroupId());
        if(group.getDomainConfiguration().isDisabledDomain()) {
            errors.reject("applicationVersionValidator.group.disabled");
        }

        if(!group.getDomainConfiguration().isDisableLimitValidations() && applicationService.isApplicationVersionLimit(application, group)) {
            errors.reject("applicationVersionValidator.group.limit");
        }

        Organization organization = group.getOrganization();
        if(organization.getDomainConfiguration().isDisabledDomain()) {
            errors.reject("applicationVersionValidator.organization.disabled");
        }

        if(!organization.getDomainConfiguration().isDisableLimitValidations() && applicationService.isApplicationVersionLimit(application, organization)) {
            errors.reject("applicationVersionValidator.organization.limit");
        }
    }

    private void validateGroupStorageLimit(Group group, long installationFileSize, Errors errors) {
        if(group.getDomainConfiguration().isDisableLimitValidations()) {
            return;
        }
        double installFileSizeMB = installationFileSize / MEGABYTE_CONVERSION;

        double totalAmount = groupService.getTotalMegabyteStorageAmount(group);
        long limit = group.getDomainConfiguration().getMegabyteStorageLimit();
        if((totalAmount + installFileSizeMB) > limit) {
            errors.reject("applicationVersionValidator.group.storageLimit");
        }
    }

    private void validateOrganizationStorageLimit(Organization organization, long installationFileSize, Errors errors) {
        if(organization.getDomainConfiguration().isDisableLimitValidations()) {
            return;
        }
        double installFileSizeMB = installationFileSize / MEGABYTE_CONVERSION;
        double totalAmount = organizationService.getTotalMegabyteStorageAmount(organization);
        long limit = organization.getDomainConfiguration().getMegabyteStorageLimit();
        if((totalAmount + installFileSizeMB) > limit) {
            errors.reject("applicationVersionValidator.organization.storageLimit");
        }
    }

    private void validateExtension(UploadApplicationVersion appVersion, Errors errors) {
        Application application = applicationService.get(appVersion.getParentId());
        String fileName = appVersion.getAppFile().getOriginalFilename();
        boolean isValid = false;

        ApplicationType type = application.getApplicationType();
        if (type.equals(ApplicationType.ANDROID)) {
            isValid = fileName.toLowerCase().endsWith(".apk");
        } else if (type.equals(ApplicationType.IOS) || type.equals(ApplicationType.IPAD) || type.equals(ApplicationType.IPHONE)) {
            isValid = fileName.toLowerCase().endsWith(".ipa");
        } else if (type.equals(ApplicationType.CHROME)) {
            isValid = fileName.toLowerCase().endsWith(".crx");
        } else if (type.equals(ApplicationType.FIREFOX)) {
            isValid = fileName.toLowerCase().endsWith(".xpi");
        } else if (type.equals(ApplicationType.BLACKBERRY)) {
            isValid = fileName.toLowerCase().endsWith(".alx");
        } else if (type.equals(ApplicationType.WINDOWSPHONE7)) {
            isValid = fileName.toLowerCase().endsWith(".xap");
        }

        if(!isValid) {
            errors.rejectValue(APP_FILE_FIELD, "applicationVersionValidator.invalidApplicationType");
        }
    }
}
