package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.CustomBranding;
import com.sparc.knappsack.components.entities.QCustomBranding;
import org.springframework.stereotype.Repository;

@Repository("customBrandingDao")
public class CustomBrandingDaoImpl extends BaseDao implements CustomBrandingDao {

    private QCustomBranding customBranding = QCustomBranding.customBranding;

    @Override
    public void add(CustomBranding customBranding) {
        getEntityManager().persist(customBranding);
    }

    @Override
    public CustomBranding get(Long id) {
        return getEntityManager().find(CustomBranding.class, id);
    }

    @Override
    public void delete(CustomBranding customBranding) {
        getEntityManager().remove(customBranding);
    }

    @Override
    public void update(CustomBranding customBranding) {
        getEntityManager().merge(customBranding);
    }

    public CustomBranding getBySubdomain(String subdomain) {
        return query().from(customBranding).where(customBranding.subdomain.eq(subdomain)).uniqueResult(customBranding);
    }
}
