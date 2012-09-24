package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;

import java.util.List;

public interface UserDomainService extends EntityService<UserDomain> {

    UserDomain get(User user, Long domainId, DomainType domainType, UserRole userRole);

    UserDomain get(User user, Long domainId, DomainType domainType);

    List<UserDomain> getAll(User user, DomainType domainType);

    List<UserDomain> getAll(User user, DomainType domainType, UserRole userRole);

    List<UserDomain> getAll(Long domainId, DomainType domainType);

    List<UserDomain> getAll(Long domainId, DomainType domainType, UserRole userRole);

    void removeUserDomainFromDomain(Long domainId, DomainType domainType, Long userId);

    void updateUserDomainRole(Long userId, Long domainId, DomainType domainType, UserRole userRole);

    void removeAllFromDomain(Long domainId, DomainType domainType);
}
