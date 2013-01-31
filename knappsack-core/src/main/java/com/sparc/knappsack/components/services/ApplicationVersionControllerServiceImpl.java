package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.components.events.EventDeliveryWithCompositeFactory;
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

    @Qualifier("eventDeliveryWithCompositeFactory")
    @Autowired(required = true)
    private EventDeliveryWithCompositeFactory eventDeliveryWithCompositeFactory;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Override
    public boolean saveApplicationVersion(UploadApplicationVersion applicationVersionForm, boolean sendNotifications) {
        boolean success = false;

        if (applicationVersionForm != null && applicationVersionForm.getParentId() != null) {

            ApplicationVersion currentApplicationVersion = applicationVersionService.get(applicationVersionForm.getId());

            // Check whether we are editing or not.
            boolean editing = false;
            if (currentApplicationVersion != null) {
                editing = true;
            }

            // Check what the current AppState of the applicationVersion is if the version already exists and is being edited.
            AppState currentAppState = determineCurrentAppState(currentApplicationVersion);

            // Make a copy of the original requested AppState in case the application version is to be resigned.
            AppState requestedAppState = applicationVersionForm.getAppState();

            Domain parentDomain = domainService.get(applicationVersionForm.getGroupId());

            // Only continue if the parent domain exists.
            if (parentDomain != null) {

                // Save the applicationVersion
                ApplicationVersion savedApplicationVersion = applicationVersionService.saveApplicationVersion(applicationVersionForm);

                if (savedApplicationVersion != null && savedApplicationVersion.getId() != null && savedApplicationVersion.getId() > 0) {

                    // Update the application version form so that the user can be notified if an error occurs during the resigning staging process.
                    applicationVersionForm.setId(savedApplicationVersion.getId());
                    applicationVersionForm.setEditing(true);

                    // Send notifications only if the application version is newly persisted or it was being edited and the AppState changed
                    if (sendNotifications && ((currentAppState != null && !currentAppState.equals(savedApplicationVersion.getAppState()))
                            || (EntityState.NEWLY_PERSISTED.equals(savedApplicationVersion.getState()) || !editing))) {
                        sendNotifications(savedApplicationVersion, EventType.APPLICATION_VERSION_STATE_CHANGED);
                    }

                    success = true;
                }
            }
        }

        return success;
    }

    @Override
    public boolean updateApplicationVersionState(Long applicationVersionId, AppState appState, boolean sendNotifications) {
        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);

        return updateApplicationVersionState(applicationVersion, appState, sendNotifications);
    }

    private boolean updateApplicationVersionState(ApplicationVersion applicationVersion, AppState appState, boolean sendNotifications) {
        boolean success = false;

        if (applicationVersion != null && appState != null && !appState.equals(applicationVersion.getAppState())) {
            applicationVersionService.updateAppState(applicationVersion, appState);

            ApplicationVersion updatedApplicationVersion = applicationVersionService.get(applicationVersion.getId());
            if (updatedApplicationVersion != null && appState.equals(updatedApplicationVersion.getAppState())) {
                success = true;

                if (sendNotifications && !AppState.DISABLED.equals(updatedApplicationVersion.getAppState())) {
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

    private AppState determineCurrentAppState(ApplicationVersion applicationVersion) {
        AppState currentAppState = null;
        if (applicationVersion != null) {
            currentAppState = applicationVersion.getAppState();
        }

        return currentAppState;
    }

}
