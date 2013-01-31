package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
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
     * @param applicationVersion ApplicationVersion - The ApplicationVersion to update.
     * @param appState AppState - the new AppState (changes the visibility of the ApplicationVersion)
     */
    void updateAppState(ApplicationVersion applicationVersion, AppState appState);

    List<ApplicationVersion> getAll();

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

    List<ApplicationVersion> getAll(List<Organization> organizations, AppState... appStates);

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
     * @param applicationVersionId Long
     * @return ApplicationVersionModel
     */
    ApplicationVersionModel createApplicationVersionModel(Long applicationVersionId);

    /**
     * @param applicationVersion
     * @return ApplicationVersionModel
     */
    ApplicationVersionModel createApplicationVersionModel(ApplicationVersion applicationVersion);

    void deleteAllForApplication(Application application);
}
