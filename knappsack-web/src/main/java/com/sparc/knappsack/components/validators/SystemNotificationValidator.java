package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.forms.SystemNotificationForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("systemNotificationValidator")
public class SystemNotificationValidator implements Validator {

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String MESSAGE = "message";
    private static final String NOTIFICATION_TYPE = "notificationType";
    private static final String NOTIFICATION_SEVERITY = "notificationSeverity";

    @Override
    public boolean supports(Class<?> aClass) {
        return SystemNotificationForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SystemNotificationForm systemNotificationForm = (SystemNotificationForm) o;

        if (!doesBindingErrorExist(errors, START_DATE) && systemNotificationForm.getStartDate() == null) {
            errors.rejectValue(START_DATE, "systemNotificationValidator.startDate");
        }

        if (!doesBindingErrorExist(errors, END_DATE) && systemNotificationForm.getStartDate() != null && systemNotificationForm.getEndDate() != null && !systemNotificationForm.getEndDate().after(systemNotificationForm.getStartDate())) {
            errors.rejectValue(END_DATE, "systemNotificationValidator.endDate");
        }

        if (!doesBindingErrorExist(errors, MESSAGE) && !StringUtils.hasText(systemNotificationForm.getMessage())) {
            errors.rejectValue(MESSAGE, "systemNotificationValidator.message");
        }

        if (!doesBindingErrorExist(errors, NOTIFICATION_TYPE) && systemNotificationForm.getNotificationType() == null) {
            errors.rejectValue(NOTIFICATION_TYPE, "systemNotificationValidator.notificationType");
        }

        if (!doesBindingErrorExist(errors, NOTIFICATION_SEVERITY) && systemNotificationForm.getNotificationSeverity() == null) {
            errors.rejectValue(NOTIFICATION_SEVERITY, "systemNotificationValidator.notificationSeverity");
        }
    }

    private boolean doesBindingErrorExist(Errors errors, String fieldName) {
        boolean bindingErrorExists = false;
        if (errors != null && StringUtils.hasText(fieldName)) {
            bindingErrorExists = (errors.getFieldError(fieldName) != null && errors.getFieldError(fieldName).isBindingFailure());
        }
        return bindingErrorExists;
    }
}
