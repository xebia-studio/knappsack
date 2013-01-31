package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.components.services.RegionService;
import com.sparc.knappsack.forms.DomainRegionForm;
import com.sparc.knappsack.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("domainRegionValidator")
public class DomainRegionValidator implements Validator {

    @Value("${email.pattern}")
    private String emailPattern;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("regionService")
    @Autowired(required = true)
    private RegionService regionService;

    private static final String NAME_FIELD = "name";
    private static final String EMAILS_FIELD = "emails";

    @Override
    public boolean supports(Class<?> clazz) {
        return DomainRegionForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DomainRegionForm form = (DomainRegionForm) target;

        boolean isEdit = form.getId() != null && form.getId() > 0;

        if (!ValidationUtils.doesBindingErrorExist(errors, NAME_FIELD) && !StringUtils.hasText(form.getName())) {
            errors.rejectValue(NAME_FIELD, "domainRegionValidator.name.empty");
        }

        boolean domainRegionNameExists = domainService.doesDomainContainRegionWithName(form.getDomainId(), form.getName());
        if (isEdit && domainRegionNameExists) {
            Region region = regionService.get(form.getId());
            if (!region.getName().equals(form.getName())) {
                errors.rejectValue(NAME_FIELD, "domainRegionValidator.name.exists");
            }
        } else if (!ValidationUtils.doesFieldErrorExist(errors, NAME_FIELD) && domainRegionNameExists) {
            errors.rejectValue(NAME_FIELD, "domainRegionValidator.name.exists");
        }

        if (!ValidationUtils.doesBindingErrorExist(errors, EMAILS_FIELD)) {
            for (String email : form.getEmails()) {
                if (!ValidationUtils.doesRegexMatch(email, emailPattern)) {
                    errors.rejectValue(NAME_FIELD, "domainRegionValidator.emails.invalid");
                    break;
                }
            }
        }
    }
}
