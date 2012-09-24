package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPQLTemplates;
import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class BaseDao {

    @PersistenceContext
    private EntityManager entityManager;

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
     *
     * @param entityPath EntityPath
     * @return EntityPath
     */
    public JPADeleteClause deleteClause(EntityPath<?> entityPath) {
        return new JPADeleteClause(getEntityManager(), entityPath);
    }
}
