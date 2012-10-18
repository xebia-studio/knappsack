package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.SystemNotification;
import com.sparc.knappsack.enums.SystemNotificationType;

import java.util.List;

public interface SystemNotificationDao extends Dao<SystemNotification> {

    List<SystemNotification> getAll();

    List<SystemNotification> getAllForTypes(SystemNotificationType... types);

    List<SystemNotification> getAllForAllPages();
}
