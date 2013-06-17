package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.QCategory;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.ApplicationType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("categoryDao")
public class CategoryDaoImpl extends BaseDao implements CategoryDao {
    QCategory category = QCategory.category;

    public void add(Category category) {
        getEntityManager().persist(category);
    }

    public List<Category> getAll() {
        return query().from(category).list(category);
    }

    public Category get(Long id) {
        return getEntityManager().find(Category.class, id);
    }

    public void delete(Category category) {
        getEntityManager().remove(getEntityManager().merge(category));
    }

    public void update(Category category) {
        getEntityManager().merge(category);
    }

    @Override
    public List<Category> getAllForUser(User user, ApplicationType deviceType) {

        JPASubQuery applicationsForUser = getApplicationsForUser(user, ApplicationType.getAllForUserDeviceType(deviceType).toArray(new ApplicationType[]{}));

        return cacheableQuery().from(category)
                .where(category.in(applicationsForUser.list(application.category)))
                .distinct().list(category);
    }
}
