package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.enums.DomainType;

/**
 * This DAO is a bit different because it is dealing with the Domain interface rather than an Entity.
 */
public interface DomainDao {
    /**
     * @param id Long - primary key of the domain Entity
     * @param domainType DomainType
     * @return Domain of the given id and DomainType
     */
    Domain get(Long id, DomainType domainType);
}
