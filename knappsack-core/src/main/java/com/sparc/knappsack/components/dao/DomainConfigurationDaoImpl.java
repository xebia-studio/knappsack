package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.DomainConfiguration;
import org.springframework.stereotype.Repository;

@Repository("domainConfigurationDao")
public class DomainConfigurationDaoImpl extends BaseDao implements DomainConfigurationDao {

    @Override
    public void add(DomainConfiguration domainConfiguration) {
        getEntityManager().persist(domainConfiguration);
    }

    @Override
    public DomainConfiguration get(Long id) {
        return getEntityManager().find(DomainConfiguration.class, id);
    }

    @Override
    public void delete(DomainConfiguration domainConfiguration) {
        getEntityManager().refresh(getEntityManager().merge(domainConfiguration));
    }

    @Override
    public void update(DomainConfiguration domainConfiguration) {
        getEntityManager().merge(domainConfiguration);
    }
}
