package com.sparc.knappsack.components.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.ManagementCenterConfig;

public class KnappsackAbstractCache {

    protected Config getConfig() {
        return getDevConfig();
    }

    private Config getDevConfig() {
        Config config = new Config();
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setEnabled(true);
        managementCenterConfig.setUrl("http://localhost:8080/mancenter");
        config.setManagementCenterConfig(managementCenterConfig);

        return config;
    }

}
