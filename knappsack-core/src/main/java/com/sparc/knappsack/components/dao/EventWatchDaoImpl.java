package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.EventWatch;
import com.sparc.knappsack.components.entities.QEventWatch;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.NotifiableType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("eventWatchDao")
public class EventWatchDaoImpl extends BaseDao implements EventWatchDao {

    QEventWatch eventWatch = QEventWatch.eventWatch;

    @Override
    public void add(EventWatch eventWatch) {
        getEntityManager().persist(eventWatch);
    }

    @Override
    public EventWatch get(Long id) {
        return getEntityManager().find(EventWatch.class, id);
    }

    @Override
    public void delete(EventWatch eventWatch) {
        getEntityManager().remove(eventWatch);
    }

    @Override
    public void update(EventWatch eventWatch) {
        getEntityManager().merge(eventWatch);
    }

    @Override
    public EventWatch get(User user, Long notifiableId, NotifiableType notifiableType) {
        return query().from(eventWatch).where(eventWatch.user.eq(user), eventWatch.notifiableId.eq(notifiableId), eventWatch.notifiableType.eq(notifiableType)).uniqueResult(eventWatch);
    }

    @Override
    public List<EventWatch> getAll(User user) {
        return query().from(eventWatch).where(eventWatch.user.eq(user)).list(eventWatch);
    }

    @Override
    public List<EventWatch> getAll(Long notifiableId, EventType eventType) {
        return query().from(eventWatch).where(eventWatch.notifiableId.eq(notifiableId), eventWatch.eventTypes.contains(eventType)).list(eventWatch);
    }

    @Override
    public Long batchDeleteEventWatch(Long notifiableId, NotifiableType notifiableType) {
        return deleteClause(eventWatch).where(eventWatch.notifiableId.eq(notifiableId), eventWatch.notifiableType.eq(notifiableType)).execute();
    }
}
