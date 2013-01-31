package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("applicationVersionUserStatisticDao")
public class ApplicationVersionUserStatisticDaoImpl extends BaseDao implements ApplicationVersionUserStatisticDao {

    QApplicationVersionUserStatistic applicationVersionUserStatistic = QApplicationVersionUserStatistic.applicationVersionUserStatistic;
    QApplicationVersion applicationVersion = QApplicationVersion.applicationVersion;

    @Override
    public void add(ApplicationVersionUserStatistic applicationVersionUserStatistic) {
        getEntityManager().persist(applicationVersionUserStatistic);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApplicationVersionUserStatistic get(Long id) {
        return getEntityManager().find(ApplicationVersionUserStatistic.class, id);
    }

    @Override
    public void delete(ApplicationVersionUserStatistic applicationVersionUserStatistic) {
        getEntityManager().remove(applicationVersionUserStatistic);
    }

    @Override
    public void update(ApplicationVersionUserStatistic applicationVersionUserStatistic) {
        getEntityManager().merge(applicationVersionUserStatistic);
    }

    public List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion, User user) {
        return query().from(applicationVersionUserStatistic).where(applicationVersionUserStatistic.applicationVersion.eq(applicationVersion).
                and(applicationVersionUserStatistic.user.eq(user))).
                list(applicationVersionUserStatistic);
    }

    public List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion) {
        return query().from(applicationVersionUserStatistic).where(applicationVersionUserStatistic.applicationVersion.eq(applicationVersion)).list(applicationVersionUserStatistic);
    }

    public List<ApplicationVersionUserStatistic> get(User user) {
        return query().from(applicationVersionUserStatistic).where(applicationVersionUserStatistic.user.eq(user)).list(applicationVersionUserStatistic);
    }

    public List<ApplicationVersionUserStatistic> get(Application application) {
        if(application.getApplicationVersions().isEmpty()) {
            return new ArrayList<ApplicationVersionUserStatistic>();
        }
        return query().from(applicationVersionUserStatistic).join(applicationVersionUserStatistic.applicationVersion, applicationVersion).
                where(applicationVersionUserStatistic.applicationVersion.
                        in(application.getApplicationVersions())).
                list(applicationVersionUserStatistic);
    }

    @Override
    public long deleteAllForApplicationVersion(ApplicationVersion applicationVersion) {
        return deleteClause(applicationVersionUserStatistic).where(applicationVersionUserStatistic.applicationVersion.eq(applicationVersion)).execute();
    }
}
