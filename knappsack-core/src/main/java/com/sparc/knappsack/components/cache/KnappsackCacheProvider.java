//package com.sparc.knappsack.components.cache;
//
//import com.hazelcast.config.Config;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.hibernate.provider.HazelcastCacheProvider;
//import org.hibernate.cache.Cache;
//import org.hibernate.cache.CacheException;
//import org.hibernate.cache.CacheProvider;
//import org.springframework.stereotype.Component;
//
//import java.util.Properties;
//
//@Component
//public class KnappsackCacheProvider extends KnappsackAbstractCache implements CacheProvider {
//
//    private HazelcastCacheProvider hibernateCacheProvider;
//
//    public KnappsackCacheProvider() {
//        Config config = getConfig();
//
//        hibernateCacheProvider = new HazelcastCacheProvider(Hazelcast.newHazelcastInstance(config));
//    }
//
//    @Override
//    public Cache buildCache(String regionName, Properties properties) throws CacheException {
//        return hibernateCacheProvider.buildCache(regionName, properties);
//    }
//
//    @Override
//    public long nextTimestamp() {
//        return hibernateCacheProvider.nextTimestamp();
//    }
//
//    @Override
//    public void start(Properties properties) throws CacheException {
//        hibernateCacheProvider.start(properties);
//    }
//
//    @Override
//    public void stop() {
//        hibernateCacheProvider.stop();
//    }
//
//    @Override
//    public boolean isMinimalPutsEnabledByDefault() {
//        return hibernateCacheProvider.isMinimalPutsEnabledByDefault();
//    }
//
//
//}
