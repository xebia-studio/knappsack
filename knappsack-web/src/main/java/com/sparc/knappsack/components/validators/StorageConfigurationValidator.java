package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.components.services.StorageConfigurationService;
import com.sparc.knappsack.forms.StorageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("storageConfigurationValidator")
public class StorageConfigurationValidator implements Validator {

    private static final String NAME_FIELD = "name";
    private static final String BASE_LOCATION_FIELD = "baseLocation";
    private static final String STORAGE_TYPE_FIELD = "storageType";

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Override
    public boolean supports(Class<?> clazz) {
        return StorageForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StorageForm storageForm = (StorageForm) target;
        validateStorage(storageForm, errors);
    }

    private void validateStorage(StorageForm storageForm, Errors errors) {
        if (storageForm.isEditing()) {
            StorageConfiguration storageConfiguration = storageConfigurationService.get(storageForm.getId());
            if (storageConfiguration == null) {
                errors.reject("storageConfigurationValidator.generic.error");
            }
        }

        StorageConfiguration storageConfiguration = storageConfigurationService.getByName(storageForm.getName());
        if(storageConfiguration != null && !storageConfiguration.getId().equals(storageForm.getId())) {
            errors.rejectValue(NAME_FIELD, "storageConfigurationValidator.nameEquals");
        }

        if (storageForm.getName() == null || "".equals(storageForm.getName().trim())) {
            errors.rejectValue(NAME_FIELD, "storageConfigurationValidator.emptyName");
        }

        if (storageForm.getBaseLocation() == null || "".equals(storageForm.getBaseLocation().trim())) {
            errors.rejectValue(BASE_LOCATION_FIELD, "storageConfigurationValidator.emptyLocation");
        }

        if (storageForm.getStorageType() == null && !storageForm.isEditing()) {
            errors.rejectValue(STORAGE_TYPE_FIELD, "storageConfigurationValidator.emptyStorageType");
        }

        if(storageForm.isEditing() && storageConfiguration != null) {
            if(!storageConfiguration.getBaseLocation().equals(storageForm.getBaseLocation())) {
                storageForm.setBaseLocation(storageConfiguration.getBaseLocation());
                errors.rejectValue(BASE_LOCATION_FIELD, "storageConfigurationValidator.baseLocation.edited");
            }

            if(!storageConfiguration.getStorageType().equals(storageForm.getStorageType())) {
                storageForm.setStorageType(storageConfiguration.getStorageType());
                errors.rejectValue(STORAGE_TYPE_FIELD, "storageConfigurationValidator.storageType.edited");
            }
        }
    }
}

