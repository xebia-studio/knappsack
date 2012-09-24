package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import org.springframework.stereotype.Repository;

@Repository("applicationVersionDao")
public class ApplicationVersionDaoImpl extends BaseDao implements ApplicationVersionDao {

    @Override
    public ApplicationVersion get(Long id) {
        return getEntityManager().find(ApplicationVersion.class, id);
    }

    @Override
    public void add(ApplicationVersion applicationVersion) {
        getEntityManager().persist(applicationVersion);
    }

    @Override
    public void delete(ApplicationVersion applicationVersion) {
        getEntityManager().remove(applicationVersion);
    }

    @Override
    public void update(ApplicationVersion applicationVersion) {
        getEntityManager().merge(applicationVersion);
    }
}
