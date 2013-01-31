package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.QRegion;
import com.sparc.knappsack.components.entities.Region;
import org.springframework.stereotype.Repository;

@Repository("regionDao")
public class RegionDaoImpl extends BaseDao implements RegionDao {

    QRegion region = QRegion.region;

    @Override
    public void add(Region region) {
        getEntityManager().persist(region);
    }

    @Override
    public Region get(Long id) {
        return getEntityManager().find(Region.class, id);
    }

    @Override
    public void delete(Region region) {
        getEntityManager().remove(region);
    }

    @Override
    public void update(Region region) {
        getEntityManager().merge(region);
    }
}
