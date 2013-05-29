package com.sparc.knappsack.components.cache;

import com.hazelcast.config.*;
import com.sparc.knappsack.properties.SystemProperties;

public class KnappsackAbstractCache {

    protected Config getConfig() {
        String awsAccessKey = System.getProperty(SystemProperties.KNAPPSACK_ACCESS_KEY);
        String awsSecretKey = System.getProperty(SystemProperties.KNAPPSACK_SECRET_KEY);
        if(awsAccessKey != null && !awsAccessKey.isEmpty() && awsSecretKey != null && !awsSecretKey.isEmpty()) {
            return getProdConfig();
        } else {
            return getDevConfig();
        }
    }

    private Config getDevConfig() {
        Config config = new Config();
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setEnabled(true);
        managementCenterConfig.setUrl("http://localhost:8080/mancenter");
        config.setManagementCenterConfig(managementCenterConfig);

        return config;
    }

    private Config getProdConfig() {
        Config config = new Config();
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setEnabled(true);
        managementCenterConfig.setUrl(System.getProperty(SystemProperties.HAZELCAST_MANAGER_URL));
        config.setManagementCenterConfig(managementCenterConfig);
        Join join = new Join();
        AwsConfig awsConfig = new AwsConfig();
        awsConfig.setEnabled(true);
        awsConfig.setSecurityGroupName(System.getProperty(SystemProperties.EB_SECURITY_GROUP));
        String awsAccessKey = System.getProperty(SystemProperties.KNAPPSACK_ACCESS_KEY);
        String awsSecretKey = System.getProperty(SystemProperties.KNAPPSACK_SECRET_KEY);
        awsConfig.setAccessKey(awsAccessKey);
        awsConfig.setSecretKey(awsSecretKey);
        join.setAwsConfig(awsConfig);
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(false);
        join.setTcpIpConfig(tcpIpConfig);
        MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        join.setMulticastConfig(multicastConfig);
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setJoin(join);
        config.setNetworkConfig(networkConfig);
        String groupName = System.getProperty(SystemProperties.STACK_NAME) + "-cache";
        GroupConfig groupConfig = new GroupConfig(groupName, awsSecretKey);
        config.setGroupConfig(groupConfig);

        return config;
    }

}
