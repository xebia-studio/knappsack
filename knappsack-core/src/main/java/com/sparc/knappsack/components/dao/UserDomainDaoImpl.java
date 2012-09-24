package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.QUserDomain;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public UserDomain getUserDomain(User user, Long domainId, DomainType domainType, UserRole userRole) {
        return query().from(userDomain).where(userDomain.domainId.eq(domainId), userDomain.domainType.eq(domainType), userDomain.user.eq(user), userDomain.role.authority.eq(userRole.toString())).uniqueResult(userDomain);
    }

    @Override
    public UserDomain getUserDomain(User user, Long domainId, DomainType domainType) {
        return query().from(userDomain).where(userDomain.domainId.eq(domainId), userDomain.domainType.eq(domainType), userDomain.user.eq(user)).uniqueResult(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomains(User user, DomainType domainType) {
        return query().from(userDomain).where(userDomain.domainType.eq(domainType), userDomain.user.eq(user)).list(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomainsForDomain(Long domainId, DomainType domainType) {
        return query().from(userDomain).where(userDomain.domainId.eq(domainId), userDomain.domainType.eq(domainType)).listDistinct(userDomain);
    }

    @Override
    public List<UserDomain> getUserDomainsForDomain(Long domainId, DomainType domainType, List<User> users) {
        return query().from(userDomain).where(userDomain.domainId.eq(domainId), userDomain.domainType.eq(domainType), userDomain.user.in(users)).list(userDomain);
    }

    @Override
    public void removeAllFromDomain(Long domainId, DomainType domainType) {
        deleteClause(userDomain).where(userDomain.domainId.eq(domainId), userDomain.domainType.eq(domainType)).execute();
    }
}
