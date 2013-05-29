package com.sparc.knappsack.util;

import com.sparc.knappsack.components.entities.User;
import org.springframework.util.StringUtils;

import java.util.UUID;


public class LoggingUtil {

    public static String generateLogErrorMessage() {
        return "Error ID: " + generateErrorId();
    }

    public static String generateLogErrorMessage(User user) {
        if (user == null) {
            return generateLogErrorMessage();
        }
        return "Error ID: " + generateErrorId() + "\n" + "User ID: " + user.getId() + "\n";
    }

    public static String generateLogErrorMessage(String errorId) {
        if (!StringUtils.hasText(errorId)) {
            return generateLogErrorMessage();
        }

        return "Error ID: " + errorId;
    }

    public static String generateLogErrorMessage(User user, String errorId) {
        if (user == null && !StringUtils.hasText(errorId)) {
            return generateLogErrorMessage();
        } else if (user != null && !StringUtils.hasText(errorId)) {
            return generateLogErrorMessage(user);
        } else if (user == null && StringUtils.hasText(errorId)) {
            return generateLogErrorMessage(errorId);
        }

        return "Error ID: " + errorId + "\n" + "User ID: " + user.getId() + "\n";
    }

    public static String generateErrorId() {
        return UUID.randomUUID().toString();
    }

}
