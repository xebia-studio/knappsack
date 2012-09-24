package com.sparc.knappsack.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;


public class MailTestUtils {
    private static final Logger log = LoggerFactory.getLogger(MailTestUtils.class);

    public static void reconfigureMailSenders(ApplicationContext applicationContext, int port) {
        Map<String, JavaMailSenderImpl> ofType = applicationContext.getBeansOfType(org.springframework.mail.javamail.JavaMailSenderImpl.class);

        for (Map.Entry<String, JavaMailSenderImpl> bean : ofType.entrySet()) {
            log.info(String.format("Configuring mail sender %s to use local Wiser SMTP on port %d", bean.getKey(), port));
            JavaMailSenderImpl mailSender = bean.getValue();
            mailSender.setHost("localhost");
            mailSender.setPort(port);
        }
    }
}
