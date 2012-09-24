package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;

/**
 * A domain refers to a collection of users.
 */
public interface Domain {

    /**
     * @return Long - the primary key of the entity
     */
    Long getId();

    /**
     * @return String - The name of the domain
     */
    String getName();

    /**
     * @return DomainType - the specific domain type
     */
    DomainType getDomainType();

    /**
     * @return String - this is a String representation of the domain's UUID
     */
    String getAccessCode();

    /**
     * @return DomainConfiguration - preferences associated with this domain
     */
    DomainConfiguration getDomainConfiguration();
}
