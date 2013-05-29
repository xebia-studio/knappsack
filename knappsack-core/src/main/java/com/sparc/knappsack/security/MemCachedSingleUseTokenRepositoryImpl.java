package com.sparc.knappsack.security;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;

public class MemCachedSingleUseTokenRepositoryImpl extends AbstractSingleUseTokenRepository implements SingleUseTokenRepository {
    private static final Logger log = LoggerFactory.getLogger(MemCachedSessionRegistryImpl.class);

    @Qualifier("memcachedClient")
    @Autowired(required = true)
    private MemcachedClient client;

    @Value("${singleuse.token.validity.seconds}")
    private int tokenValiditySecond;

    @Override
    public void putToken(SingleUseToken token) {
        if (token == null) {
            log.error("Attempted to put null token.");
            return;
        } else if (!StringUtils.hasText(token.getSessionIdHash())) {
            log.error("Attempted to put token with empty key.");
        }

        client.set(token.getSessionIdHash(), tokenValiditySecond, token);
    }

    @Override
    public void updateToken(SingleUseToken token) {
        if (token == null) {
            log.error("Attempted to update with null token.");
            return;
        }
        client.replace(token.getSessionIdHash(), tokenValiditySecond, token);
    }

    @Override
    public void removeToken(String key) {
        if (!StringUtils.hasText(key)) {
            log.error("Attempted to remove a token with an empty key.");
            return;
        }
        client.delete(key);
    }

    @Override
    public SingleUseToken getToken(String key) {
        if (!StringUtils.hasText(key)) {
            log.error("Attempted to get a token for an empty key.");
            return null;
        }
        SingleUseToken token = (SingleUseToken) client.get(key);
        removeToken(key);
        return token;
    }

    @Override
    public boolean validateAndExpireTokenForKey(String key, boolean shouldExpireToken) {
        if (!StringUtils.hasText(key)) {
            log.error("Attempted to validate and expire a token for an empty key.");
            return false;
        }

        if (shouldExpireToken) {
            return isValidToken(getToken(key), tokenValiditySecond);
        } else {
            return isValidToken((SingleUseToken) client.get(key), tokenValiditySecond);
        }
    }

    @PreDestroy
    private void preDestroy() {
        log.info("Shutting down Memcached");
        client.shutdown();
    }
}
