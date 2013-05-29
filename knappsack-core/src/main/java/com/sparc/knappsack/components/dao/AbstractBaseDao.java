package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.expr.BooleanExpression;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.UserRole;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBaseDao {

    QUserDomain userDomain = QUserDomain.userDomain;
    QDomain domain = QDomain.domain;
    QGroup group = QGroup.group;
    QOrganization organization = QOrganization.organization;
    QApplication application = QApplication.application;
    QApplicationVersion applicationVersion = QApplicationVersion.applicationVersion;

    protected abstract EntityManager getEntityManager();
    public abstract JPAQuery query();
    public abstract JPASubQuery subQuery();
    public abstract JPAQuery cacheableQuery();
    public abstract JPADeleteClause deleteClause(EntityPath<?> entityPath);

    // All UserDomains for user
    protected JPASubQuery getUserDomainsForUser(User user) {
        return subQuery().from(userDomain).where(userDomain.user.eq(user));
    }

    // All domains for the given user based off of UserDomains
    protected JPASubQuery getDomainsForUser(User user) {
        return subQuery().from(domain)
                .where(domain.in(
                        getUserDomainsForUser(user).list(userDomain.domain)
                ));
    }

    // All groups that a user explicitly belongs to
    protected JPASubQuery getExplicitGroupsForUser(User user) {
        return subQuery().from(group)
                .where(group.in(
                        getDomainsForUser(user)
                                .list(domain.as(group.getClass()))
                ));
    }

    // All organizations that a user explicitly belong to
    protected JPASubQuery getExplicitOrganizationsForUser(User user) {
        return subQuery().from(organization)
                .where(organization.in(
                        getDomainsForUser(user).list(domain.as(organization.getClass()))
                ));
    }

    // All UserDomains for user and UserRole
    protected JPASubQuery getUserDomainsForUserAndUserRole(User user, UserRole userRole) {
        return subQuery().from(userDomain)
                .where(userDomain.user.eq(user),
                        userDomain.role.authority.eq(userRole.toString()));
    }

    // All Organizations which a user is an admin of
    protected JPASubQuery getAdminOrganizationsForUser(User user) {
        return subQuery().from(organization).where(organization.in(
                subQuery().from(userDomain)
                        .where(userDomain.user.eq(user),
                                userDomain.role.authority.eq(UserRole.ROLE_ORG_ADMIN.toString()))
                        .list(userDomain.domain.as(organization.getClass()))
        ));
    }

    // All groups for the organizations which a user is an admin of
    protected JPASubQuery getAdminOrganizationGroupsForUser(User user) {
        return subQuery().from(group)
                .where(group.organization.in(getAdminOrganizationsForUser(user).list(organization)));
    }

    // All groups which a user belongs to (Either explicitly or by being an Org Admin)
    protected JPASubQuery getAllGroupsForUser(User user) {
        return subQuery().from(group)
                .where(group.in(getExplicitGroupsForUser(user).list(group))
                        .or(group.in(getAdminOrganizationGroupsForUser(user).list(group)))
                );
    }

    protected BooleanExpression getApplicationVersionAppStateExpression(AppState... appStates) {
        List<BooleanExpression> applicationVersionAppStateExpressions = new ArrayList<BooleanExpression>();
        for (AppState appState : appStates) {
            applicationVersionAppStateExpressions.add(applicationVersion.appState.eq(appState));
        }

        return BooleanExpression.anyOf(applicationVersionAppStateExpressions.toArray(new BooleanExpression[applicationVersionAppStateExpressions.size()]));
    }

    // All applications which have a version that is a guest of a group that a user is a part of (either explicitly or indirectly)
    protected JPASubQuery getGuestApplicationsForUser(User user) {

        List<AppState> downloadableAppStates = AppState.getAllDownloadable();

        return subQuery().from(application)
                .join(application.applicationVersions, applicationVersion)
                .join(applicationVersion.guestGroups, group)
                .where(group.in(getAllGroupsForUser(user).list(group))
                        .and(getApplicationVersionAppStateExpression(downloadableAppStates.toArray(new AppState[downloadableAppStates.size()])))
                );
    }

    // All applications with versions which are in the ORGANIZATION_PUBLISH AppState for the users organizations
    protected JPASubQuery getApplicationsWithVersionsInOrgPublishStateForUser(User user) {
        return subQuery().from(application)
                .where(application.in(
                        subQuery().from(applicationVersion).join(applicationVersion.application, application)
                                .join(application.ownedGroup, group)
                                .join(group.organization, organization)
                                .where(organization.in(
                                        getExplicitOrganizationsForUser(user).list(organization))
                                        .and(applicationVersion.appState.in(
                                                AppState.ORGANIZATION_PUBLISH)
                                        ))
                                .list(applicationVersion.application)
                ));
    }

    // All applications available to a user
    protected JPASubQuery getApplicationsForUser(User user, ApplicationType... applicationTypes) {
        return subQuery().from(application)
                .where(getApplicationForUserBooleanExpression(user, applicationTypes));
    }

    protected BooleanExpression getApplicationForUserBooleanExpression(User user, ApplicationType... applicationTypes) {
        List<BooleanExpression> applicationOrExpressions = new ArrayList<BooleanExpression>();
        applicationOrExpressions.add(application.ownedGroup.in(getExplicitGroupsForUser(user).list(group))); /*All apps which belong to a group which the user belongs to*/
        applicationOrExpressions.add(application.in(getApplicationsWithVersionsInOrgPublishStateForUser(user).list(application))); /*All apps which have versions published to organization for which user belongs to*/
        applicationOrExpressions.add(application.ownedGroup.in(getAdminOrganizationGroupsForUser(user).list(group))); /*All apps for organization admins which belong to groups of their organizations*/
        applicationOrExpressions.add(application.in(getGuestApplicationsForUser(user).list(application))); /*All apps which have a version with guest access to a group that a user belongs to*/

        BooleanExpression applicationOrExpression = BooleanExpression.anyOf(applicationOrExpressions.toArray(new BooleanExpression[applicationOrExpressions.size()]));

        // Applications with correct ApplicationTypes
        BooleanExpression applicationTypesExpression = null;
        if (applicationTypes != null && applicationTypes.length > 0) {
            applicationTypesExpression = application.applicationType.in(applicationTypes);
        }

        // Application Versions are not empty
        BooleanExpression applicationVersionsNotEmptyExpression = application.applicationVersions.isNotEmpty();

        // Application Versions with downloadable AppState
        List<AppState> downloadableAppStates = AppState.getAllDownloadable();
        BooleanExpression applicationVersionsDownloadableAppStateExpression = getApplicationVersionAppStateExpression(downloadableAppStates.toArray(new AppState[downloadableAppStates.size()]));

        // Applications with Versions in downloadable AppState
        BooleanExpression downloadableApplicationVersionsExpression = application.in(subQuery().from(applicationVersion).where(applicationVersionsDownloadableAppStateExpression).list(applicationVersion.application));

        // Both Application Versions are not empty and Application contains at least one version not disabled
        BooleanExpression applicationVersionsDownloadableExpression = BooleanExpression.allOf(applicationVersionsNotEmptyExpression, downloadableApplicationVersionsExpression);

        // Application belongs to Organization which is set as Active Organization on User
        BooleanExpression activeOrganizationBooleanExpression = getActiveOrganizationBooleanExpression(organization, user);
        BooleanExpression applicationActiveOrg = null;
        if(activeOrganizationBooleanExpression != null) {
            applicationActiveOrg = application.in(
                    subQuery().from(application)
                            .join(application.ownedGroup, group)
                            .join(group.organization, organization)
                            .where(activeOrganizationBooleanExpression)
                            .list(application));
        }

        return BooleanExpression.allOf(applicationTypesExpression, applicationOrExpression, applicationVersionsDownloadableExpression, applicationActiveOrg);
    }

    protected BooleanExpression getActiveOrganizationBooleanExpression(QOrganization qOrganization, User user) {
        return user.getActiveOrganization() != null ? qOrganization.eq(user.getActiveOrganization()) : null;
    }

}
