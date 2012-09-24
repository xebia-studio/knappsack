package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.enums.DomainType;

public interface DomainService {

    /**
     * @param domainId Long - the ID of the Domain to lookup
     * @param domainType DomainType
     * @return Domain - the domain that matches the ID and specific DomainType
     */
    Domain get(Long domainId, DomainType domainType);
}
