package com.sparc.knappsack.comparators;

import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;

import java.util.Comparator;

public class SystemNotificationSeverityComparator implements Comparator<SystemNotificationSeverity> {
    @Override
        public int compare(SystemNotificationSeverity o1, SystemNotificationSeverity o2) {
            Integer obj1SortOrder = o1.getSortOrder();
            Integer obj2SortOrder = o2.getSortOrder();

            return obj1SortOrder.compareTo(obj2SortOrder);
        }
}
