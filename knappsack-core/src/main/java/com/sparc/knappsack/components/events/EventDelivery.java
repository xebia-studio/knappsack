package com.sparc.knappsack.components.events;

public interface EventDelivery<Notifiable> {

    boolean sendNotifications(Notifiable notifiable);

}
