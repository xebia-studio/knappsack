package com.sparc.knappsack.security;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PreDestroy;

public class MemCachedSingleUseTokenRepositoryImpl implements SingleUseTokenRepository {
    private static final Logger log = LoggerFactory.getLogger(MemCachedSessionRegistryImpl.class);

    @Qualifier("memcachedClient")
    @Autowired(required = true)
    private MemcachedClient client;

    @Value("${singleuse.token.validity.seconds}")
    private int tokenValiditySecond;

    @Override
    public void putToken(SingleUseToken token) {
        client.set(token.getSessionIdHash(), tokenValiditySecond, token);
    }

    @Override
    public void updateToken(SingleUseToken token) {
        client.replace(token.getSessionIdHash(), tokenValiditySecond, token);
    }

    @Override
    public void removeToken(String key) {
        client.delete(key);
    }

    @Override
    public SingleUseToken getToken(String key) {
        SingleUseToken token = (SingleUseToken) client.get(key);
        removeToken(key);
        return token;
    }

    @PreDestroy
    private void preDestroy() {
        log.info("Shutting down Memcached");
        client.shutdown();
    }
}
