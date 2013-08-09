package com.sparc.knappsack.components.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.HazelcastCacheRegionFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.*;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;

import java.util.Properties;

public class KnappsackIntegrationTestCacheRegionFactory extends KnappsackAbstractCache implements RegionFactory {

    private static final long serialVersionUID = -5507109519171389464L;

    protected HazelcastCacheRegionFactory hazelcastCacheRegionFactory;

    public KnappsackIntegrationTestCacheRegionFactory() {
        HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName("integration-test-instance");
        if(instance == null) {
            Config config = getConfig();
            this.hazelcastCacheRegionFactory = new HazelcastCacheRegionFactory(Hazelcast.newHazelcastInstance(config));
        } else {
            this.hazelcastCacheRegionFactory = new HazelcastCacheRegionFactory(instance);
        }
    }

    protected Config getConfig() {
        Config config = new Config();
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName("knappsack-integration-test-cache");
        groupConfig.setPassword("password1234");
        config.setGroupConfig(groupConfig);
        config.setInstanceName("integration-test-instance");

        return config;
    }


    @Override
    public void start(Settings settings, Properties properties) throws CacheException {
        hazelcastCacheRegionFactory.start(settings, properties);
    }

    @Override
    public void stop() {
        hazelcastCacheRegionFactory.stop();
    }

    @Override
    public boolean isMinimalPutsEnabledByDefault() {
        return hazelcastCacheRegionFactory.isMinimalPutsEnabledByDefault();
    }

    @Override
    public AccessType getDefaultAccessType() {
        return hazelcastCacheRegionFactory.getDefaultAccessType();
    }

    @Override
    public long nextTimestamp() {
        return hazelcastCacheRegionFactory.nextTimestamp();
    }

    @Override
    public EntityRegion buildEntityRegion(String s, Properties properties, CacheDataDescription cacheDataDescription) throws CacheException {
        return hazelcastCacheRegionFactory.buildEntityRegion(s, properties, cacheDataDescription);
    }

    @Override
    public NaturalIdRegion buildNaturalIdRegion(String s, Properties properties, CacheDataDescription cacheDataDescription) throws CacheException {
        return hazelcastCacheRegionFactory.buildNaturalIdRegion(s, properties, cacheDataDescription);
    }

    @Override
    public CollectionRegion buildCollectionRegion(String s, Properties properties, CacheDataDescription cacheDataDescription) throws CacheException {
        return hazelcastCacheRegionFactory.buildCollectionRegion(s, properties, cacheDataDescription);
    }

    @Override
    public QueryResultsRegion buildQueryResultsRegion(String s, Properties properties) throws CacheException {
        return hazelcastCacheRegionFactory.buildQueryResultsRegion(s, properties);
    }

    @Override
    public TimestampsRegion buildTimestampsRegion(String s, Properties properties) throws CacheException {
        return hazelcastCacheRegionFactory.buildTimestampsRegion(s, properties);
    }
}
