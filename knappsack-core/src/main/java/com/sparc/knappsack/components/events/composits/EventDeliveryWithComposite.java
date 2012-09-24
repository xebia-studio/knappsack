package com.sparc.knappsack.components.events.composits;

public interface EventDeliveryWithComposite<Notifiable, EventComposite> {
    boolean sendNotifications(Notifiable notifiable, EventComposite eventComposite);
}
