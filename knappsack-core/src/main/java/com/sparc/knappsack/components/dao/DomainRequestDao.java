package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.DomainRequest;

import java.util.List;

public interface DomainRequestDao extends Dao<DomainRequest> {

    /**
     * @param domainId Id of the domain to use for lookup
     * @return List of all requests for the given domain
     */
    List<DomainRequest> getAllForDomain(long domainId);

    boolean doesDomainRequestExist(long domainId, String emailAddress);

    long countAll(Long domainId);

}
