package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("organizationDao")
public class OrganizationDaoImpl extends BaseDao implements OrganizationDao {

    QOrganization organization = QOrganization.organization;
    QApplication application = QApplication.application;
    QApplicationVersion applicationVersion = QApplicationVersion.applicationVersion;
    QCategory category = QCategory.category;
    QGroup group = QGroup.group;
    QUserDomain userDomain = QUserDomain.userDomain;

    @Override
    public void add(Organization organization) {
        getEntityManager().persist(organization);
    }

    @Override
    public Organization get(Long id) {
        return getEntityManager().find(Organization.class, id);
    }

    @Override
    public List<Organization> get(List<Long> organizationIds) {
        return query().from(organization).where(organization.id.in(organizationIds)).list(organization);
    }

    @Override
    public void delete(Organization organization) {
        getEntityManager().remove(getEntityManager().merge(organization));
    }

    @Override
    public void update(Organization organization) {
        getEntityManager().merge(organization);
    }

    @Override
    public List<Organization> getAll() {
        return query().from(organization).list(organization);
    }

    @Override
    public Organization get(String name) {
        return query().from(organization).where(organization.name.equalsIgnoreCase(name)).uniqueResult(organization);
    }

    @Override
    public long countAll() {
        return query().from(organization).count();
    }

    @Override
    public long countOrganizationGroups(Long orgId) {
        return query().from(organization).innerJoin(organization.groups, group).where(organization.id.eq(orgId)).count();
    }

    @Override
    public long countOrganizationUsers(Long orgId) {
        return query().from(organization, userDomain).where(organization.id.eq(orgId).and(userDomain.domain.id.eq(orgId))).count();
    }

    @Override
    public long countOrganizationApps(Long orgId) {
        return query().from(organization, application, category).where(organization.id.eq(orgId).and(category.in(organization.categories)).and(category.eq(application.category))).count();
    }

    @Override
    public long countOrganizationAppVersions(Long orgId) {
        return query().from(organization, application, applicationVersion, category).where(organization.id.eq(orgId).and(category.in(organization.categories).and(category.eq(application.category).and(application.eq(applicationVersion.application))))).count();
    }

    @Override
    public List<Organization> getAllForCreateDateRange(Date minDate, Date maxDate) {
        return query().from(organization).where(organization.createDate.between(minDate, maxDate)).list(organization);
    }
}
