package com.sparc.knappsack.components.events;

import com.sparc.knappsack.components.events.composits.EventDeliveryWithComposite;
import com.sparc.knappsack.enums.EventType;

public interface EventDeliveryWithCompositeFactory {
    EventDeliveryWithComposite getEventDelivery(EventType eventType);
}
