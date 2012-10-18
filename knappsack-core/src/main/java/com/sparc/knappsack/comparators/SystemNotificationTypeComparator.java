package com.sparc.knappsack.comparators;

import com.sparc.knappsack.enums.SystemNotificationType;

import java.util.Comparator;

public class SystemNotificationTypeComparator implements Comparator<SystemNotificationType> {
    @Override
    public int compare(SystemNotificationType type1, SystemNotificationType type2) {
        Integer obj1SortOrder = type1.getSortOrder();
        Integer obj2SortOrder = type2.getSortOrder();

        return obj1SortOrder.compareTo(obj2SortOrder);
    }
}
