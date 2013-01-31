package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.EventWatch;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.EmailService;
import com.sparc.knappsack.components.services.EventWatchService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("applicationVersionStateChangedEvent")
public class ApplicationVersionStateChangedEvent implements EventDelivery<ApplicationVersion> {

    @Qualifier("eventWatchService")
    @Autowired(required = true)
    private EventWatchService eventWatchService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("emailDeliveryService")
    @Autowired(required = true)
    private EmailService emailService;

    @Qualifier("applicationVersionPublishRequestEvent")
    @Autowired(required = true)
    private ApplicationVersionPublishRequestEvent applicationVersionPublishRequestEvent;

    @Qualifier("applicationVersionErrorEvent")
    @Autowired(required = true)
    private ApplicationVersionErrorEvent applicationVersionErrorEvent;

    @Override
    public boolean sendNotifications(ApplicationVersion applicationVersion) {
        boolean success = false;

        if (applicationVersion != null) {

            switch (applicationVersion.getAppState()) {
                case ORG_PUBLISH_REQUEST:
                    success = applicationVersionPublishRequestEvent.sendNotifications(applicationVersion);
                    break;
                case ORGANIZATION_PUBLISH:
                    success = sendNotificationsToSubscribedUsers(applicationVersion);
                    break;
                case GROUP_PUBLISH:
                    success = sendNotificationsToSubscribedUsers(applicationVersion);
                    break;
                case DISABLED:
                    success = true;
                    break;
                case ERROR:
                    success = applicationVersionErrorEvent.sendNotifications(applicationVersion);
                    break;
            }

        }

        return success;
    }

    private List<User> getAllSubscribedUsers(Application application) {
        List<User> users = new ArrayList<User>();
        if (application != null) {
            for (EventWatch eventWatch : eventWatchService.getAll(application, EventType.APPLICATION_VERSION_BECOMES_AVAILABLE)) {
                if (!users.contains(eventWatch.getUser())) {
                    users.add(eventWatch.getUser());
                }
            }
        }

        return users;
    }

    private boolean isApplicationVersionVisibleToUser(ApplicationVersion applicationVersion, User user) {
        boolean isVisible = false;

        if (applicationVersion != null && applicationVersion.getApplication() != null && user != null) {
            List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(user, applicationVersion.getApplication().getId(), null, applicationVersion.getAppState());
            isVisible = applicationVersions.contains(applicationVersion);
        }

        return isVisible;
    }

    private boolean sendNotificationsToSubscribedUsers(ApplicationVersion applicationVersion) {
        boolean success = false;
        if (applicationVersion != null) {
            List<User> usersToNotify = new ArrayList<User>();

            for (User user : getAllSubscribedUsers(applicationVersion.getApplication())) {
                if (isApplicationVersionVisibleToUser(applicationVersion, user) && !usersToNotify.contains(user)) {
                    usersToNotify.add(user);
                }
            }

            List<Long> userIds = new ArrayList<Long>();
            for (User user : usersToNotify) {
                userIds.add(user.getId());
            }

            success = emailService.sendApplicationVersionBecameVisibleEmail(applicationVersion.getId(), userIds);
        }
        return success;
    }
}
