package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.EventWatch;
import com.sparc.knappsack.components.entities.Notifiable;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.NotifiableType;

import java.util.List;

public interface EventWatchDao extends Dao<EventWatch> {

    /**
     * @param user User to search on
     * @param notifiableId Long notifiableId to search on
     * @param notifiableType NotifiableType to search on
     *
     * @return specific EventWatch object for a user
     */
    public EventWatch get(User user, Long notifiableId, NotifiableType notifiableType);

    /**
     * @param user User to search on
     *
     * @return List of all EventWatch objects for a user
     */
    public List<EventWatch> getAll(User user);

    /**
     * @param notifiableId ID of the notifiable to search on
     * @param eventType EventType to search on
     *
     * @return List of all EventWatch objects for a given Notifiable and EventType
     */
    public List<EventWatch> getAll(Long notifiableId, EventType eventType);

    /**
     * @param notifiable Notifiable to search on
     * @return List of all EventWatch objects for a given Notifiable
     */
    public List<EventWatch> getAll(Notifiable notifiable);

}
