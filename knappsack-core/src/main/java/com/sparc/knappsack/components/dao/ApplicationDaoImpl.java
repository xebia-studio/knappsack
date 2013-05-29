package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.ApplicationType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("applicationDao")
public class ApplicationDaoImpl extends BaseDao implements ApplicationDao {

    QApplication application = QApplication.application;
    QOrganization organization = QOrganization.organization;
    QDomain domain = QDomain.domain;

    public void add(Application application) {
        getEntityManager().persist(application);
    }

    public List<Application> getAll() {
        return query().from(application).list(application);
    }

    public Application get(Long id) {
        return getEntityManager().find(Application.class, id);
    }

    public void delete(Application application) {
        getEntityManager().remove(application) ;
    }

    public List<Application> getAll(ApplicationType applicationType) {
        return query().from(application).where(application.applicationType.eq(applicationType)).list(application);
    }

    /*
     *  Used to get a list of all applications case-insensitive, wildcard matching a specified criteria.
     *  Searches on either application Name or Application Description.
     *  The below translates to the following traditional sql statement.
     *  Select * from ApplicationsObject where (NAME like '%Criteria%') OR (DESCRIPTION like '%Criteria%');
     *
     *  @param Category category used to search on
     *  @return list of applications
     *
     *  @see <a href="http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html">Hibernate Criteria</a>
     */
    @Override
    public List<Application> getByNameAndDescription(String searchCriteria) {
        return query().from(application).where(application.name.containsIgnoreCase(searchCriteria).or(application.description.containsIgnoreCase(searchCriteria))).list(application);
    }

    /*
     *  Used to get a list of all applications with a specified category type.
     *  The below translates to the following traditional sql statement.
     *  Select * from ApplicationsObject where CATEGORY = 'Specified Category';
     *
     *  @param Category category used to search on
     *  @return list of applications
     *
     *  @see <a href="http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html">Hibernate Criteria</a>
     */
    @Override
    public List<Application> getByCategory(Category category) {
        return cacheableQuery().from(application).where(application.category.eq(category)).list(application);
    }

    @Override
    public List<Application> getByCategoryAndApplicationType(Category category, ApplicationType applicationType) {
        return cacheableQuery().from(application).where(application.category.eq(category).and(application.applicationType.eq(applicationType))).list(application);
    }

    @Override
    public List<Application> getByGroup(Group group, ApplicationType... applicationTypes) {
        if(applicationTypes == null || applicationTypes.length == 0) {
            return cacheableQuery().from(application).where(application.ownedGroup.eq(group)).list(application);
        }

        return cacheableQuery().from(application).where(application.ownedGroup.eq(group).and(application.applicationType.in(applicationTypes))).list(application);
    }

    @Override
    public List<Application> getAllForUser(User user, ApplicationType... applicationTypes) {

        JPAQuery query = cacheableQuery().from(application)
                .where(getApplicationForUserBooleanExpression(user, applicationTypes));

        return query.listDistinct(application);
    }

    public void update(Application application) {
        getEntityManager().merge(application);
    }

    @Override
    public long countAll() {
        return query().from(application).count();
    }
}
