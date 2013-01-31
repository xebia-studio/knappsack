package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import org.springframework.stereotype.Repository;

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


    public List<ApplicationVersion> getAll(List<Organization> organizations, AppState... appStates) {
        return query().from(applicationVersion).join(applicationVersion.application, application)
                .join(application.ownedGroup, group)
                .join(group.organization, organization).where(organization.in(organizations).and(applicationVersion.appState.in(appStates))).listDistinct(applicationVersion);

    }

    public List<ApplicationVersion> getAll(Long organizationId, AppState... appStates) {
        return query().from(applicationVersion).join(applicationVersion.application, application)
                .join(application.ownedGroup, group)
                .join(group.organization, organization)
                .where(organization.id.eq(organizationId).and(applicationVersion.appState.in(appStates))).listDistinct(applicationVersion);
    }
}
