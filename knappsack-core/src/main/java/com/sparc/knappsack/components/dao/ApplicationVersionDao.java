package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.AppState;

import java.util.List;

public interface ApplicationVersionDao extends Dao<ApplicationVersion> {

    long deleteAllForApplication(Application application);

    List<ApplicationVersion> getAll();

    List<ApplicationVersion> getAllByOrganizations(List<Organization> organizations, AppState... appStates);

    List<ApplicationVersion> getAllByOrganization(Long organizationId, AppState... appStates);

    List<ApplicationVersion> getAllByApplication(Long applicationId, AppState... appStates);

    List<ApplicationVersion> getAllByApplicationForUser(long applicationId, User user);

    boolean doesVersionExistForApplication(long applicationId, String versionName);
}
