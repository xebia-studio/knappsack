package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.CustomBranding;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.CustomBrandingService;
import com.sparc.knappsack.components.services.OrgStorageConfigService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.forms.OrganizationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.awt.image.BufferedImage;

@Component("organizationValidator")
public class OrganizationValidator implements Validator {

    private static final String NAME_FIELD = "name";
    private static final String STORAGE_PREFIX_FIELD = "storagePrefix";
    private static final String STORAGE_CONFIGURATION_ID_FIELD = "storageConfigurationId";
    private static final String LOGO_FIELD = "logo";
    private static final String SUBDOMAIN_FIELD = "subdomain";
    public static final int MAX_IMAGE_BYTES = 819200;

    @Value("${email.pattern}")
    private String emailPattern;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private OrgStorageConfigService orgStorageConfigService;

    @Autowired(required = true)
    private CustomBrandingService customBrandingService;

    @Qualifier("imageValidator")
    @Autowired(required = true)
    private ImageValidator imageValidator;

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

        if(organizationForm.getSubdomain() != null && !organizationForm.getSubdomain().isEmpty()) {
            CustomBranding customBranding = customBrandingService.getBySubdomain(organizationForm.getSubdomain());
            if(customBranding != null && !organization.getCustomBranding().getId().equals(customBranding.getId())) {
                errors.rejectValue(SUBDOMAIN_FIELD, "organizationValidator.subdomain");
            }
        }

        if (organizationForm.getLogo() != null) {
            BufferedImage bufferedImage = imageValidator.createBufferedImage(organizationForm.getLogo());

            boolean logoErrorExists = false;

            if (!imageValidator.isValidImageSize(organizationForm.getLogo(), MAX_IMAGE_BYTES /*Bytes: 800 KB*/)) {
                errors.rejectValue(LOGO_FIELD, "organizationValidator.logo.invalidSize", new Object[]{MAX_IMAGE_BYTES * 0.0009765625 /*Convert bytes to KiloBytes*/}, "");
                logoErrorExists = true;
            }

            if (!imageValidator.isValidImageType(organizationForm.getLogo())) {
                errors.rejectValue(LOGO_FIELD, "organizationValidator.logo.invalidType");
                logoErrorExists = true;
            }

            if (bufferedImage == null || !imageValidator.isValidMaxDimensions(bufferedImage, 150, 50)) {
                errors.rejectValue(LOGO_FIELD, "organizationValidator.logo.invalidDimensions", new Object[]{Long.toString(150), Long.toString(50)}, "");
                logoErrorExists = true;
            }

            if (logoErrorExists) {
                organizationForm.setLogo(null);
            }
        }
    }
}
