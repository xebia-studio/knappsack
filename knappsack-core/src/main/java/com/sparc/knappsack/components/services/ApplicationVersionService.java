package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import com.sparc.knappsack.models.ApplicationVersionModel;

import java.util.List;

public interface ApplicationVersionService extends EntityService<ApplicationVersion> {

    /**
     * @param uploadApplicationVersion UploadApplicationVersion
     * @return ApplicationVersion - populated with data from the UploadApplicationVersion and added to the specified Application
     */
    ApplicationVersion saveApplicationVersion(UploadApplicationVersion uploadApplicationVersion);

    /**
     * @param appVersionId Long - The ID of the ApplicationVersion to update
     * @param appState AppState - the new AppState (changes the visibility of the ApplicationVersion)
     */
    void updateAppState(Long appVersionId, AppState appState);

    /**
     * @param organizationId Id of the organization
     * @return List<ApplicationVersion> - get all ApplicationVersions for the given Organization
     */
    List<ApplicationVersion> getAll(Long organizationId);

    /**
     * @param organizationId Id of the organization
     * @param appStates AppState...
     * @return List<ApplicationVersion> - get all ApplicationVersions for a given Organization and specific AppStates
     */
    List<ApplicationVersion> getAll(Long organizationId, AppState... appStates);

    /**
     * @param group Group
     * @param appStates AppState...
     * @return List<ApplicationVersion> - get all ApplicationVersions for a given Group and specific AppStates
     */
    List<ApplicationVersion> getAll(Group group, AppState... appStates);

    /**
     * @param application Application
     * @param appStates AppState...
     * @return List<ApplicationVersion> - get all versions of an Application with specified AppStates
     */
    List<ApplicationVersion> getAll(Application application, AppState... appStates);

    /**
     * @param applicationVersionId
     * @return ApplicationVersionModel
     */
    ApplicationVersionModel createApplicationVersionModel(Long applicationVersionId);
}
