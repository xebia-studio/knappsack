package com.sparc.knappsack.util;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

    public static boolean doesBindingErrorExist(Errors errors, String fieldName) {
        boolean bindingErrorExists = false;
        if (errors != null && StringUtils.hasText(fieldName)) {
            bindingErrorExists = (errors.getFieldError(fieldName) != null && errors.getFieldError(fieldName).isBindingFailure());
        }
        return bindingErrorExists;
    }

    public static boolean doesFieldErrorExist(Errors errors, String fieldName) {
        boolean fieldErrorExists = false;
        if (errors != null && StringUtils.hasText(fieldName)) {
            List<FieldError> fieldErrors = errors.getFieldErrors(fieldName);
            fieldErrorExists = fieldErrors != null && fieldErrors.size() > 0;
        }

        return fieldErrorExists;
    }

    public static boolean doesRegexMatch(String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(value);
        return m.matches();
    }

}
