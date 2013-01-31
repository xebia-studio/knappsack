package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.OrgStorageConfigService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.forms.OrganizationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("organizationValidator")
public class OrganizationValidator implements Validator {

    private static final String NAME_FIELD = "name";
    private static final String STORAGE_PREFIX_FIELD = "storagePrefix";
    private static final String STORAGE_CONFIGURATION_ID_FIELD = "storageConfigurationId";
    private static final String ADMIN_EMAIL_FIELD = "adminEmail";

    @Value("${email.pattern}")
    private String emailPattern;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private OrgStorageConfigService orgStorageConfigService;

    @Override
    public boolean supports(Class<?> aClass) {
        return OrganizationForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        OrganizationForm organizationForm = (OrganizationForm) o;
        if (organizationForm.getName() == null || "".equals(organizationForm.getName().trim())) {
            errors.rejectValue(NAME_FIELD, "organizationValidator.emptyName");
        }

        if ((organizationForm.getStorageConfigurationId() == null || organizationForm.getStorageConfigurationId() <= 0) && !organizationForm.isEditing()) {
            errors.rejectValue(STORAGE_CONFIGURATION_ID_FIELD, "organizationValidator.emptyStorageConfigurationId");
        }

        if (!organizationForm.isEditing() && (organizationForm.getStoragePrefix() == null || "".equals(organizationForm.getStoragePrefix().trim()))) {
            errors.rejectValue(STORAGE_PREFIX_FIELD, "organizationValidator.emptyPrefix");
        }

        Organization organization = organizationService.getByName(organizationForm.getName());
        if (organization != null && !organization.getId().equals(organizationForm.getId())) {
            errors.rejectValue(NAME_FIELD, "organizationValidator.nameEquals");
        }

        if (!organizationForm.isEditing()) {
            OrgStorageConfig orgStorageConfig = orgStorageConfigService.getByPrefix(organizationForm.getStoragePrefix());
            if (orgStorageConfig != null && !orgStorageConfig.getOrganization().getId().equals(organizationForm.getId())) {
                errors.rejectValue(STORAGE_PREFIX_FIELD, "organizationValidator.prefix");
            }
        }

    }
}
