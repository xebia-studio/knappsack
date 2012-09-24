package com.sparc.knappsack.components.events;

import com.sparc.knappsack.enums.EventType;

public interface EventDeliveryFactory {
    EventDelivery getEventDelivery(EventType eventType);
}
