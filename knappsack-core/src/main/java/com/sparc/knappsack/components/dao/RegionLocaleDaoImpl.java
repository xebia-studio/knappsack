package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.RegionLocale;
import org.springframework.stereotype.Repository;

@Repository("regionLocaleDao")
public class RegionLocaleDaoImpl extends BaseDao implements RegionLocaleDao {
    @Override
    public void add(RegionLocale regionLocale) {
        getEntityManager().persist(regionLocale);
    }

    @Override
    public RegionLocale get(Long id) {
        return getEntityManager().find(RegionLocale.class, id);
    }

    @Override
    public void delete(RegionLocale regionLocale) {
        getEntityManager().remove(regionLocale);
    }

    @Override
    public void update(RegionLocale regionLocale) {
        getEntityManager().merge(regionLocale);
    }
}
