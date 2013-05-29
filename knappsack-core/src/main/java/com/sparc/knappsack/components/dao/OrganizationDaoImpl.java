package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.ListSubQuery;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    QUser user = QUser.user;

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
        return cacheableQuery().from(organization).innerJoin(organization.groups, group).where(organization.id.eq(orgId)).count();
    }

    @Override
    public long countOrganizationUsers(Long orgId, boolean includeGroups) {

        List<BooleanExpression> expressions = new ArrayList<BooleanExpression>();
        expressions.add(userDomain.domain.id.eq(orgId));

        if (includeGroups) {
            JPASubQuery groupSubQuery = subQuery().from(group).where(group.organization.id.eq(orgId));
            JPASubQuery groupUserDomainsSubQuery = subQuery().from(userDomain).where(userDomain.domain.in(groupSubQuery.list(group)));

            expressions.add(userDomain.in(groupUserDomainsSubQuery.list(userDomain)));
        }

        JPASubQuery userIdSubQuery = subQuery().from(userDomain).where(BooleanExpression.anyOf(expressions.toArray(new BooleanExpression[expressions.size()]))).groupBy(userDomain.user.id);
        ListSubQuery<Long> userIds = userIdSubQuery.list(userDomain.user.id);

        return cacheableQuery().from(user)
                .where(user.id.in(userIds))
                .countDistinct();
    }

    @Override
    public long countOrganizationApps(Long orgId) {
        return cacheableQuery().from(organization, application, category).where(organization.id.eq(orgId).and(category.in(organization.categories)).and(category.eq(application.category))).count();
    }

    @Override
    public long countOrganizationAppVersions(Long orgId) {
        return cacheableQuery().from(organization, application, applicationVersion, category).where(organization.id.eq(orgId).and(category.in(organization.categories).and(category.eq(application.category).and(application.eq(applicationVersion.application))))).count();
    }

    @Override
    public List<Organization> getAllForCreateDateRange(Date minDate, Date maxDate) {
        return query().from(organization).where(organization.createDate.between(minDate, maxDate)).list(organization);
    }

    @Override
    public List<Organization> getAdministeredOrganizationsForUser(User user) {
        return cacheableQuery().from(organization)
                .where(organization.in(
                        subQuery().from(userDomain)
                                .where(userDomain.user.eq(user), userDomain.role.authority.eq(UserRole.ROLE_ORG_ADMIN.toString()))
                                .list(userDomain.domain.as(organization.getClass()))
                )).list(organization);
    }

    @Override
    public Organization getForGroupId(long groupId) {
        return query().from(group)
                .where(group.id.eq(groupId)).uniqueResult(group.organization);
    }
}
