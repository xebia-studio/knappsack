package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("domainDao")
public class DomainDaoImpl extends BaseDao implements DomainDao {

    QDomain domain = QDomain.domain;
    QUserDomain userDomain = QUserDomain.userDomain;
    QRegion region = QRegion.region;
    QUser user = QUser.user;

    public Domain get(Long id) {
//        return getEntityManager().find(Domain.class, id);
        return cacheableQuery().from(domain).where(domain.id.eq(id)).uniqueResult(domain);
    }

    @Override
    public List<Domain> get(Long... ids) {
        JPAQuery query = query().from(domain).where(domain.id.in(ids));
        List<Domain> list = query.list(domain);
        return list;
    }

    @Override
    public Domain getByUUID(String uuid) {
        return query().from(domain).where(domain.uuid.eq(uuid)).uniqueResult(domain);
    }

    @Override
    public Domain getByRegion(long regionId) {
        return query().from(domain).where(domain.regions.contains(subQuery().from(region).where(region.id.eq(regionId)).unique(region))).uniqueResult(domain);
    }

    @Override
    public Domain getByRegion(Region region) {
        return query().from(domain).where(domain.regions.contains(region)).uniqueResult(domain);
    }

    @Override
    public boolean doesDomainContainRegionWithName(long domainId, String regionName) {
        return query().from(region).where(region.name.equalsIgnoreCase(regionName), region.in(subQuery().from(domain).where(domain.id.eq(domainId), domain.regions.contains(region)).list(region))).exists();
    }

    public List<Domain> getAll(User aUser, DomainType... domainTypes) {
        return cacheableQuery().from(domain).join(domain.userDomains, userDomain).where(domain.domainType.in(domainTypes).and(userDomain.user.eq(aUser))).list(domain);
    }
}
