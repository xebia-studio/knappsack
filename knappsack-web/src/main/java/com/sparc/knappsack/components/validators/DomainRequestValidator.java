package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.services.DomainRequestService;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.forms.DomainRequestForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static com.sparc.knappsack.util.ValidationUtils.*;

@Component("domainRequestValidator")
public class DomainRequestValidator implements Validator {

    @Value("${email.pattern}")
    private String emailPattern;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("domainRequestService")
    @Autowired(required = true)
    private DomainRequestService domainRequestService;

    private static final String REGION_FIELD = "region";
    private static final String LANGUAGES_FIELD = "languages";
    private static final String FIRST_NAME_FIELD = "firstName";
    private static final String LAST_NAME_FIELD = "lastName";
    private static final String COMPANY_NAME_FIELD = "companyName";
    private static final String ADDRESS_FIELD = "address";
    private static final String PHONE_FIELD = "phoneNumber";
    private static final String EMAIL_ADDRESS_FIELD = "emailAddress";
    private static final String DEVICE_TYPE_FIELD = "deviceType";
    private static final String DOMAIN_UUID_FIELD = "domainUUID";

    @Override
    public boolean supports(Class<?> clazz) {
        return DomainRequestForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DomainRequestForm domainRequestForm = (DomainRequestForm) target;

        Domain domain = domainService.getByUUID(domainRequestForm.getDomainUUID());
        if (!doesBindingErrorExist(errors, DOMAIN_UUID_FIELD) && domain == null) {
            errors.reject(DOMAIN_UUID_FIELD, "domainRequestValidator.domainUUID.invalid");
        }

        if (!doesBindingErrorExist(errors, FIRST_NAME_FIELD) && !StringUtils.hasText(domainRequestForm.getFirstName())) {
            errors.rejectValue(FIRST_NAME_FIELD, "domainRequestValidator.firstName.empty");
        }

        if (!doesBindingErrorExist(errors, LAST_NAME_FIELD) && !StringUtils.hasText(domainRequestForm.getLastName())) {
            errors.rejectValue(LAST_NAME_FIELD, "domainRequestValidator.lastName.empty");
        }

        if (!doesBindingErrorExist(errors, COMPANY_NAME_FIELD) && !StringUtils.hasText(domainRequestForm.getCompanyName())) {
            errors.rejectValue(COMPANY_NAME_FIELD, "domainRequestValidator.companyName.empty");
        }

        if (!doesBindingErrorExist(errors, ADDRESS_FIELD) && !StringUtils.hasText(domainRequestForm.getAddress())) {
            errors.rejectValue(ADDRESS_FIELD, "domainRequestValidator.address.empty");
        }

        if (!doesBindingErrorExist(errors, PHONE_FIELD) && !StringUtils.hasText(domainRequestForm.getPhoneNumber())) {
            errors.rejectValue(PHONE_FIELD, "domainRequestValidator.phoneNumber.empty");
        }

        if (!doesBindingErrorExist(errors, EMAIL_ADDRESS_FIELD) && !StringUtils.hasText(domainRequestForm.getEmailAddress()) || !doesRegexMatch(domainRequestForm.getEmailAddress(), emailPattern)) {
            errors.rejectValue(EMAIL_ADDRESS_FIELD, "domainRequestValidator.emailAddress.invalid");
        }

        if (!doesBindingErrorExist(errors, DEVICE_TYPE_FIELD) && domainRequestForm.getDeviceType() == null) {
            errors.rejectValue(DEVICE_TYPE_FIELD, "domainRequestValidator.deviceType.empty");
        }

        if (!doesBindingErrorExist(errors, REGION_FIELD)) {
            Domain domainForRegion = domainService.getDomainForRegion(domainRequestForm.getRegion());
            if (domainForRegion == null || !domainForRegion.equals(domain)) {
                errors.rejectValue(REGION_FIELD, "domainRequestValidator.region.invalid");
            }
        }

        if (!doesFieldErrorExist(errors, EMAIL_ADDRESS_FIELD) && !doesFieldErrorExist(errors, DOMAIN_UUID_FIELD) && !doesFieldErrorExist(errors, REGION_FIELD) && domainRequestService.doesDomainRequestExist(domain.getId(), domainRequestForm.getEmailAddress())) {
            errors.reject("domainRequestValidator.request.exists");
        }

        if (!doesBindingErrorExist(errors, LANGUAGES_FIELD) && domainRequestForm.getLanguages().size() <= 0) {
            errors.rejectValue(LANGUAGES_FIELD, "domainRequestValidator.languages.empty");
        }

    }
}
