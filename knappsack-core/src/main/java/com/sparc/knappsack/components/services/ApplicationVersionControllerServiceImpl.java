package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.EntityState;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("applicationVersionControllerService")
public class ApplicationVersionControllerServiceImpl implements ApplicationVersionControllerService {

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Override
    public boolean saveApplicationVersion(UploadApplicationVersion applicationVersion) {
        boolean success = false;

        if (applicationVersion != null && applicationVersion.getParentId() != null) {

            AppState currentAppState = determineCurrentAppState(applicationVersion.getId());

            ApplicationVersion savedApplicationVersion = applicationVersionService.saveApplicationVersion(applicationVersion);
            if (savedApplicationVersion != null && savedApplicationVersion.getId() != null && savedApplicationVersion.getId() > 0) {

                if ((currentAppState != null && !currentAppState.equals(savedApplicationVersion.getAppState()))
                        || EntityState.NEWLY_PERSISTED.equals(savedApplicationVersion.getState())) {
                    sendNotifications(savedApplicationVersion, EventType.APPLICATION_VERSION_STATE_CHANGED);
                }

                success = true;
            }
        }

        return success;
    }

    @Override
    public boolean updateApplicationVersionState(Long applicationVersionId, AppState appState) {
        boolean success = false;

        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);

        if (applicationVersion != null && appState != null && !appState.equals(applicationVersion.getAppState())) {
            applicationVersionService.updateAppState(applicationVersionId, appState);

            ApplicationVersion updatedApplicationVersion = applicationVersionService.get(applicationVersionId);
            if (updatedApplicationVersion != null && appState.equals(updatedApplicationVersion.getAppState())) {
                success = true;

                if (!AppState.DISABLED.equals(updatedApplicationVersion.getAppState())) {
                    sendNotifications(updatedApplicationVersion, EventType.APPLICATION_VERSION_STATE_CHANGED);
                }
            }
        }

        return success;
    }

    private boolean sendNotifications(ApplicationVersion applicationVersion, EventType eventType) {
        boolean success = false;

        EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(eventType);
        if (deliveryMechanism != null) {
            success = deliveryMechanism.sendNotifications(applicationVersion);
        }

        return success;
    }

    private AppState determineCurrentAppState(Long applicationVersionId) {
        AppState currentAppState = null;
        ApplicationVersion existingApplicationVersion = applicationVersionService.get(applicationVersionId);
        if (existingApplicationVersion != null) {
            currentAppState = existingApplicationVersion.getAppState();
        }

        return currentAppState;
    }
}
