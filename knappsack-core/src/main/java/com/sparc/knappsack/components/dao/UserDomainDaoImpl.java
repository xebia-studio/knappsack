package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.QUserDomain;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("userDomainDao")
public class UserDomainDaoImpl extends BaseDao implements UserDomainDao {

    QUserDomain userDomain = QUserDomain.userDomain;

    @Override
    public void add(UserDomain userDomain) {
        getEntityManager().persist(userDomain);
    }

    @Override
    public UserDomain get(Long id) {
        return getEntityManager().find(UserDomain.class, id);
    }

    @Override
    public void delete(UserDomain userDomain) {
        getEntityManager().remove(userDomain);
    }

    @Override
    public void update(UserDomain userDomain) {
        getEntityManager().merge(userDomain);
    }

    @Override
    public UserDomain getUserDomain(User user, Long domainId, UserRole userRole) {
        return query().from(userDomain).where(userDomain.domain.id.eq(domainId), userDomain.user.eq(user), userDomain.role.authority.eq(userRole.toString())).uniqueResult(userDomain);
    }

    @Override
    public UserDomain getUserDomain(User user, Long domainId) {
        return query().from(userDomain).where(userDomain.domain.id.eq(domainId), userDomain.user.eq(user)).uniqueResult(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomains(User user) {
        return query().from(userDomain).where(userDomain.user.eq(user)).list(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomains(User user, DomainType... domainTypes) {
        return query().from(userDomain).where(userDomain.user.eq(user).and(userDomain.domain.domainType.in(domainTypes))).list(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomainsForDomain(Long domainId) {
        return query().from(userDomain).where(userDomain.domain.id.eq(domainId)).listDistinct(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomainsForDomain(Long domainId, List<User> users) {
        return query().from(userDomain).where(userDomain.domain.id.eq(domainId), userDomain.user.in(users)).list(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomainsForDomainAndRoles(Long domainId, UserRole... userRoles) {
        Set<String> userRolesSet = new HashSet<String>();
        for (UserRole userRole : userRoles) {
            userRolesSet.add(userRole.toString());
        }
        return query().from(userDomain).where(userDomain.domain.id.eq(domainId).and(userDomain.role.authority.in(userRolesSet))).list(userDomain);
    }

    @Override
    public void removeAllFromDomain(Long domainId) {
        deleteClause(userDomain).where(userDomain.domain.id.eq(domainId)).execute();
    }

    @Override
    public long countDomains(User user, UserRole userRole) {
        return query().from(userDomain).where(userDomain.user.eq(user).and(userDomain.role.authority.eq(userRole.toString()))).countDistinct();
    }
}
