package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;

import java.util.List;

public interface UserDomainDao extends Dao<UserDomain> {
    /**
     * @param user User
     * @param domainId Long primary key of the Domain
     * @param domainType DomainType
     * @param userRole UserRole
     * @return UserDomain
     */
    UserDomain getUserDomain(User user, Long domainId, DomainType domainType, UserRole userRole);

    /**
     * @param user User
     * @param domainId Long
     * @param domainType DomainType
     * @return UserDomain
     */
    UserDomain getUserDomain(User user, Long domainId, DomainType domainType);

    /**
     * @param user User
     * @param domainType DomainType
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomains(User user, DomainType domainType);

    /**
     * @param domainId Long
     * @param domainType DomainType
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomainsForDomain(Long domainId, DomainType domainType);

    /**
     * @param domainId Long
     * @param domainType DomainType
     * @param users List<User>
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomainsForDomain(Long domainId, DomainType domainType, List<User> users);

    /**
     * @param domainId Long
     * @param domainType DomainType
     */
    void removeAllFromDomain(Long domainId, DomainType domainType);
}
