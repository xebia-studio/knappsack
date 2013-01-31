package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.DomainType;

import java.util.List;

/**
 * This DAO is a bit different because it is dealing with the Domain interface rather than an Entity.
 */
public interface DomainDao {
    /**
     * @param id Long - primary key of the domain Entity
     * @return Domain of the given id
     */
    Domain get(Long id);

    /**
     * @param ids - collection of primary keys for domain Entities
     * @return List of domains with the given ids
     */
    List<Domain> get(Long... ids);

    /**
     * @param uuid String - UUID of the Domain
     * @return Domain with the given UUID
     */
    Domain getByUUID(String uuid);

    /**
     * @param regionId Long - region Id
     * @return Domain which owns the given region
     */
    Domain getByRegion(long regionId);

    /**
     * @param region Region to use for lookup
     * @return Domain which owns the given region
     */
    Domain getByRegion(Region region);

    /**
     * @param domainId Id of the domain to search on
     * @param regionName Name of the Region to search for
     * @return
     */
    boolean doesDomainContainRegionWithName(long domainId, String regionName);

    /**
     * @param user User
     * @param domainTypes Specific types of domains that this user belongs to
     * @return List of domains that this user belongs to
     */
    List<Domain> getAll(User user, DomainType... domainTypes);
}
