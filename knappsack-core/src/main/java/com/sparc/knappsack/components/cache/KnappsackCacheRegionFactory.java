//package com.sparc.knappsack.components.cache;
//
//import com.hazelcast.config.Config;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.hibernate.HazelcastCacheRegionFactory;
//import org.hibernate.cache.*;
//import org.hibernate.cache.access.AccessType;
//import org.hibernate.cfg.Settings;
//
//import java.util.Properties;
//
//public class KnappsackCacheRegionFactory extends KnappsackAbstractCache implements RegionFactory {
//
//    private HazelcastCacheRegionFactory hazelcastCacheRegionFactory;
//
//    public KnappsackCacheRegionFactory() {
//        Config config = getConfig();
//        this.hazelcastCacheRegionFactory = new HazelcastCacheRegionFactory(Hazelcast.newHazelcastInstance(config));
//    }
//
//    @Override
//    public void start(Settings settings, Properties properties) throws CacheException {
//        hazelcastCacheRegionFactory.start(settings, properties);
//    }
//
//    @Override
//    public void stop() {
//        hazelcastCacheRegionFactory.stop();
//    }
//
//    @Override
//    public boolean isMinimalPutsEnabledByDefault() {
//        return hazelcastCacheRegionFactory.isMinimalPutsEnabledByDefault();
//    }
//
//    @Override
//    public AccessType getDefaultAccessType() {
//        return hazelcastCacheRegionFactory.getDefaultAccessType();
//    }
//
//    @Override
//    public long nextTimestamp() {
//        return hazelcastCacheRegionFactory.nextTimestamp();
//    }
//
//    @Override
//    public EntityRegion buildEntityRegion(String s, Properties properties, CacheDataDescription cacheDataDescription) throws CacheException {
//        return hazelcastCacheRegionFactory.buildEntityRegion(s, properties, cacheDataDescription);
//    }
//
//    @Override
//    public CollectionRegion buildCollectionRegion(String s, Properties properties, CacheDataDescription cacheDataDescription) throws CacheException {
//        return hazelcastCacheRegionFactory.buildCollectionRegion(s, properties, cacheDataDescription);
//    }
//
//    @Override
//    public QueryResultsRegion buildQueryResultsRegion(String s, Properties properties) throws CacheException {
//        return hazelcastCacheRegionFactory.buildQueryResultsRegion(s, properties);
//    }
//
//    @Override
//    public TimestampsRegion buildTimestampsRegion(String s, Properties properties) throws CacheException {
//        return hazelcastCacheRegionFactory.buildTimestampsRegion(s, properties);
//    }
//}
