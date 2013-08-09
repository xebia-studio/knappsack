package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("groupDao")
public class GroupDaoImpl extends BaseDao implements GroupDao {

    QGroup group = QGroup.group;
    QUserDomain userDomain = QUserDomain.userDomain;
    QDomain domain = QDomain.domain;
    QOrganization organization = QOrganization.organization;

    @Override
    public void add(Group group) {
        getEntityManager().persist(group);
    }

    @Override
    public Group get(Long id) {
        return getEntityManager().find(Group.class, id);
    }

    @Override
    public Group get(String name, long organizationId) {
        return query().from(group).where(group.name.equalsIgnoreCase(name).and(group.organization.id.eq(organizationId))).uniqueResult(group);
    }

    @Override
    public void delete(Group group) {
        getEntityManager().remove(group);
    }

    @Override
    public void update(Group group) {
        getEntityManager().merge(group);
    }

    @Override
    public List<Group> getAll() {
        return query().from(group).list(group);
    }

    @Override
    public List<Group> getAllGuestGroups(long applicationVersionId) {
        return query().from(group).where(group.guestApplicationVersions.any().id.eq(applicationVersionId)).list(group);
    }

    @Override
    public Group getOwnedGroup(Application application) {
        return query().from(group).where(group.ownedApplications.contains(application)).uniqueResult(group);
    }

    @Override
    public Group getGroupByUUID(String accessCode) {
        return query().from(group).where(group.uuid.eq(accessCode)).uniqueResult(group);
    }

    @Override
    public List<Group> getAdministeredGroupsForUser(User user) {

        // All UserDomains for User with UserRole.ROLE_ORG_ADMIN
        JPASubQuery userDomainsOrgAdmin = subQuery().from(userDomain)
                .where(userDomain.user.eq(user),
                        userDomain.role.authority.eq(UserRole.ROLE_ORG_ADMIN.toString()));

        // All UserDomains for User with UserRole.ROLE_GROUP_ADMIN
        JPASubQuery userDomainsGroupAdmin = subQuery().from(userDomain)
                .where(userDomain.user.eq(user),
                        userDomain.role.authority.eq(UserRole.ROLE_GROUP_ADMIN.toString()));

        // GroupAdmin domains
        JPASubQuery groupAdminDomains = subQuery().from(domain)
                .where(domain.in(
                        userDomainsGroupAdmin.list(userDomain.domain)
                ));

        // All Organizations which the User is an admin
        JPASubQuery adminOrganizations = subQuery().from(organization)
                .where(organization.in(
                        subQuery().from(domain)
                                .where(domain.in(
                                        userDomainsOrgAdmin.list(userDomain.domain))
                                ).list(domain.as(organization.getClass()))
                ).and(getActiveOrganizationBooleanExpression(organization, user)));

        // All groups which are part of an organization which the users is an admin of
        JPASubQuery adminOrganizationGroups = subQuery().from(group)
                .where(group.organization.in(
                        adminOrganizations.list(organization)
                ));

        // Main Query - All Groups which the user is a direct admin (ROLE_GROUP_ADMIN) or through organizations which the user is an admin (ROLE_ORG_ADMIN)
        JPAQuery mainQuery = cacheableQuery().from(group)
                .where((group.in(
                        adminOrganizationGroups.list(group))
                        .or(group.in(groupAdminDomains.list(domain.as(group.getClass()))))).and(getActiveOrganizationBooleanExpression(group.organization, user))
                );

        return mainQuery.distinct().list(group);
    }

    @Override
    public List<Group> getGroupsForUser(User user) {
        JPASubQuery userGroups = getAllGroupsForUser(user);

        // Main query - All Groups which the user is a direct member (ROLE_GROUP_ADMIN or ROLE_GROUP_USER) or through organizations which the user is an admin (ROLE_ORG_ADMIN)
        JPAQuery mainQuery = cacheableQuery().from(group)
                .where(group.in(
                        userGroups.list(group)));

        return mainQuery.distinct().list(group);
    }

    @Override
    public List<Group> getGroupsForUserActiveOrganization(User user) {
        JPASubQuery userGroups = getAllGroupsForUser(user);

        // Main query - All Groups which the user is a direct member (ROLE_GROUP_ADMIN or ROLE_GROUP_USER) or through organizations which the user is an admin (ROLE_ORG_ADMIN)
        JPAQuery mainQuery = query().from(group)
                .join(group.organization, organization)
                .where(group.in(userGroups.list(group))
                        .and(getActiveOrganizationBooleanExpression(organization, user)));

        return mainQuery.distinct().list(group);
    }
}
