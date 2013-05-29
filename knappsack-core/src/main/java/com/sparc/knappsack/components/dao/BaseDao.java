package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPQLTemplates;
import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.EntityPath;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class BaseDao extends AbstractBaseDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected final EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @return JPAQuery with the default JPQLTemplate to provide operator patterns for JPQL
     */
    public JPAQuery query() {
        return new JPAQuery(getEntityManager(), JPQLTemplates.DEFAULT);
    }

    /**
     * @return JPASubQuery
     */
    public JPASubQuery subQuery() {
        return new JPASubQuery();
    }

    /**
     * @return JPAQuery with a hint to set the query as cacheable
     */
    public JPAQuery cacheableQuery() {
        JPAQuery query = query();
        query.setHint("org.hibernate.cacheable", true);
        return query;
    }

    /**
     *
     * @param entityPath EntityPath
     * @return EntityPath
     */
    public JPADeleteClause deleteClause(EntityPath<?> entityPath) {
        return new JPADeleteClause(getEntityManager(), entityPath);
    }
}
