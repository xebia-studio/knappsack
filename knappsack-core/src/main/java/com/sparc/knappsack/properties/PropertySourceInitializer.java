package com.sparc.knappsack.properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public class PropertySourceInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        MigrateProperties migrateProperties = new MigrateProperties();
        migrateProperties.migrate();

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        propertySources.addFirst(getPropertySource());
        setSystemProperties(applicationContext);
    }

    private PropertySource getPropertySource() {
        S3PropertySource s3PropertySource = new S3PropertySource(new S3Repository());
        return s3PropertySource;
    }

    private void setSystemProperties(ConfigurableWebApplicationContext applicationContext) {
        //Set the variables on the System in order to be accessed by non Spring managed classes
        String ebSecurityGroup = applicationContext.getEnvironment().getProperty(SystemProperties.EB_SECURITY_GROUP);
        String hazelcastManagerURL = applicationContext.getEnvironment().getProperty(SystemProperties.HAZELCAST_MANAGER_URL);
        if (ebSecurityGroup != null) {
            System.setProperty(SystemProperties.EB_SECURITY_GROUP, ebSecurityGroup);
        }
        if (hazelcastManagerURL != null) {
            System.setProperty(SystemProperties.HAZELCAST_MANAGER_URL, hazelcastManagerURL);
        }
    }

}
