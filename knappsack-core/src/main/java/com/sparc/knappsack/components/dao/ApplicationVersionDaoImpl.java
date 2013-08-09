package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository("applicationVersionDao")
public class ApplicationVersionDaoImpl extends BaseDao implements ApplicationVersionDao {

    QApplicationVersion applicationVersion = QApplicationVersion.applicationVersion;
    QApplication application = QApplication.application;
    QGroup group = QGroup.group;
    QOrganization organization = QOrganization.organization;
    QDomain domain = QDomain.domain;
    QUser user = QUser.user;

    @SuppressWarnings("unchecked")
    @Override
    public ApplicationVersion get(Long id) {
        return getEntityManager().find(ApplicationVersion.class, id);
    }

    @Override
    public void add(ApplicationVersion applicationVersion) {
        getEntityManager().persist(applicationVersion);
    }

    @Override
    public void delete(ApplicationVersion applicationVersion) {
        getEntityManager().remove(applicationVersion);
    }

    @Override
    public void update(ApplicationVersion applicationVersion) {
        getEntityManager().merge(applicationVersion);
    }

    @Override
    public long deleteAllForApplication(Application application) {
        return deleteClause(applicationVersion).where(applicationVersion.application.eq(application)).execute();
    }

    @Override
    public List<ApplicationVersion> getAll() {
        return query().from(applicationVersion).list(applicationVersion);
    }


    public List<ApplicationVersion> getAllByOrganizations(List<Organization> organizations, AppState... appStates) {
        return query().from(applicationVersion).join(applicationVersion.application, application)
                .join(application.ownedGroup, group)
                .join(group.organization, organization).where(organization.in(organizations).and(applicationVersion.appState.in(appStates))).distinct().list(applicationVersion);

    }

    public List<ApplicationVersion> getAllByOrganization(Long organizationId, AppState... appStates) {
        return cacheableQuery().from(applicationVersion).join(applicationVersion.application, application)
                .join(application.ownedGroup, group)
                .join(group.organization, organization)
                .where(organization.id.eq(organizationId).and(applicationVersion.appState.in(appStates))).distinct().list(applicationVersion);
    }

    @Override
    public List<ApplicationVersion> getAllByApplication(Long applicationId, AppState... appStates) {
        return cacheableQuery().from(applicationVersion).join(applicationVersion.application, application).where(application.id.eq(applicationId).and(applicationVersion.appState.in(appStates))).list(applicationVersion);
    }

    @Override
    public List<ApplicationVersion> getAllByApplicationForUser(long applicationId, User user) {

        List<AppState> downloadableAppStates = AppState.getAllDownloadable();

        JPASubQuery availableApplicationsForUser = subQuery().from(application).where(getApplicationForUserBooleanExpression(user));

        // ApplicationVersion belongs to a group which a users has access
        BooleanExpression applicationVersionOwnedGroup = applicationVersion.in(subQuery().from(applicationVersion).join(applicationVersion.application, application).where(application.ownedGroup.in(getAllGroupsForUser(user).list(group))).list(applicationVersion));

        // ApplicationVersion is shared to a Guest Group which a user belongs
        BooleanExpression applicationVersionGuestGroup = applicationVersion.in(subQuery().from(applicationVersion).join(applicationVersion.guestGroups, group).where(group.in(getAllGroupsForUser(user).list(group))).list(applicationVersion));

        BooleanExpression applicationVersionOrgPublish = applicationVersion.appState.eq(AppState.ORGANIZATION_PUBLISH)
                .and(applicationVersion.in(subQuery().from(applicationVersion)
                        .join(applicationVersion.application, application)
                        .join(application.ownedGroup, group)
                        .join(group.organization, organization)
                        .where(organization.in(getExplicitOrganizationsForUser(user).list(organization))).list(applicationVersion)));

        return cacheableQuery().from(applicationVersion)
                .where(applicationVersion.application.id.eq(applicationId) /* ApplicationVersion belongs to specified Application */
                        .and(applicationVersion.application.in(availableApplicationsForUser.list(application))) /* ApplicationVersion belongs to an application which the user has access */
                        .and(getApplicationVersionAppStateExpression(downloadableAppStates.toArray(new AppState[downloadableAppStates.size()]))) /* ApplicationVersion is in a downloadable AppState */
                        .and(applicationVersionOwnedGroup /* ApplicationVersion belongs to a group which a users has access */
                                .or(applicationVersionGuestGroup) /* ApplicationVersion is shared to a Guest Group which a user belongs */
                                .or(applicationVersionOrgPublish)
                        )
                ).distinct().list(applicationVersion);
    }

    @Override
    public boolean doesVersionExistForApplication(long applicationId, String versionName) {
        return query().from(applicationVersion).where(applicationVersion.application.id.eq(applicationId).and(applicationVersion.versionName.equalsIgnoreCase(StringUtils.trimTrailingWhitespace(versionName)))).exists();
    }
}
