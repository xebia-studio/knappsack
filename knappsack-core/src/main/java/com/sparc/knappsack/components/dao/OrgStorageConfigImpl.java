package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.QOrgStorageConfig;
import org.springframework.stereotype.Repository;

@Repository("orgStorageConfigDao")
public class OrgStorageConfigImpl extends BaseDao implements OrgStorageConfigDao {

    QOrgStorageConfig orgStorageConfig = QOrgStorageConfig.orgStorageConfig;

    @Override
    public void add(OrgStorageConfig orgStorageConfig) {
        getEntityManager().persist(orgStorageConfig);
    }

    @Override
    public OrgStorageConfig get(Long id) {
        return getEntityManager().find(OrgStorageConfig.class, id);
    }

    @Override
    public OrgStorageConfig get(String prefix) {
        return query().from(orgStorageConfig).where(orgStorageConfig.prefix.eq(prefix)).uniqueResult(orgStorageConfig);
    }

    @Override
    public void delete(OrgStorageConfig orgStorageConfig) {
        getEntityManager().remove(orgStorageConfig);
    }

    @Override
    public void update(OrgStorageConfig orgStorageConfig) {
        getEntityManager().merge(getEntityManager().merge(orgStorageConfig));
    }
}
