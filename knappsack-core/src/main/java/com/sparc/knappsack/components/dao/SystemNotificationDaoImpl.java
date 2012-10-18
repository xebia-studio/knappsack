package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.QSystemNotification;
import com.sparc.knappsack.components.entities.SystemNotification;
import com.sparc.knappsack.enums.SystemNotificationType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("systemNotificationDao")
public class SystemNotificationDaoImpl extends BaseDao implements SystemNotificationDao {
    QSystemNotification systemNotification = QSystemNotification.systemNotification;

    @Override
    public void add(SystemNotification systemNotification) {
        getEntityManager().persist(systemNotification);
    }

    @Override
    public SystemNotification get(Long id) {
        return getEntityManager().find(SystemNotification.class, id);
    }

    @Override
    public void delete(SystemNotification systemNotification) {
        getEntityManager().remove(getEntityManager().merge(systemNotification));
    }

    @Override
    public void update(SystemNotification systemNotification) {
        getEntityManager().merge(systemNotification);
    }

    @Override
    public List<SystemNotification> getAll() {
        return query().from(systemNotification).list(systemNotification);
    }

    @Override
    public List<SystemNotification> getAllForTypes(SystemNotificationType... types) {
        return query().from(systemNotification).where(systemNotification.notificationType.in(types)).list(systemNotification);
    }

    @Override
    public List<SystemNotification> getAllForAllPages() {
        return query().from(systemNotification).where(systemNotification.allPages.isTrue()).list(systemNotification);
    }
}
