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
     * @param userRole UserRole
     * @return UserDomain
     */
    UserDomain getUserDomain(User user, Long domainId, UserRole userRole);

    /**
     * @param user User
     * @param domainId Long
     * @return UserDomain
     */
    UserDomain getUserDomain(User user, Long domainId);

    /**
     * @param user User
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomains(User user);

    /**
     * @param user User
     * @param domainTypes DomainTypes...
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomains(User user, DomainType... domainTypes);

    /**
     * @param domainId Long
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomainsForDomain(Long domainId);

    /**
     * @param domainId Long
     * @param users List<User>
     * @return List<UserDomain>
     */
    List<UserDomain> getUserDomainsForDomain(Long domainId, List<User> users);

    /**
     * @param domainId
     * @param userRole
     * @return
     */
    List<UserDomain> getUserDomainsForDomainAndRoles(Long domainId, UserRole... userRole);

    /**
     * @param domainId Long
     */
    void removeAllFromDomain(Long domainId);
    
    /**
     * @param user User
     * @param userRole UserRole
     * @return long - total amount of UserDomains of a specific type and role tied to this user
     */
    long countDomains(User user, UserRole userRole);
    
}
