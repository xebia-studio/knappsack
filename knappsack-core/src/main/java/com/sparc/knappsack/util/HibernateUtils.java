package com.sparc.knappsack.util;

import org.hibernate.ejb.EntityManagerFactoryImpl;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;

import javax.persistence.EntityManagerFactory;

public class HibernateUtils implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtils.class);

    private static ApplicationContext ctx = null;

    public static Statistics getStatistics() {
        EntityManagerFactoryInfo entityManagerFactoryInfo = (EntityManagerFactoryInfo) ctx.getBean("entityManagerFactory");
        EntityManagerFactory emf = entityManagerFactoryInfo.getNativeEntityManagerFactory();
        EntityManagerFactoryImpl emfImp = (EntityManagerFactoryImpl) emf;
        return emfImp.getSessionFactory().getStatistics();
    }

    public static void logStatisticsToInfo() {
        Statistics stats = getStatistics();
        if(stats != null) {
            log.info(stats.toString());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
