package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.enums.EventType;

public interface SQSEventDeliveryFactory {
    SQSEventDelivery getEventDelivery(EventType eventType);
}
