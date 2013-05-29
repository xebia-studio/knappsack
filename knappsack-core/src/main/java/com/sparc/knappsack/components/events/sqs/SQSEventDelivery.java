package com.sparc.knappsack.components.events.sqs;

import com.sparc.knappsack.models.EmailModel;

public interface SQSEventDelivery {

    boolean sendNotifications(EmailModel emailModel);

}
