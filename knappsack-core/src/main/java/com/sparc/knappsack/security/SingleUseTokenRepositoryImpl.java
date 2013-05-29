package com.sparc.knappsack.security;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingleUseTokenRepositoryImpl extends AbstractSingleUseTokenRepository implements SingleUseTokenRepository {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SingleUseTokenRepositoryImpl.class);

    @Value("${singleuse.token.validity.seconds}")
    private int tokenValiditySecond;

    private static final Map<String, SingleUseToken> tokens = new ConcurrentHashMap<String, SingleUseToken>();

    @Override
    public void putToken(SingleUseToken token) {
        if (token == null) {
            log.error("Attempted to put null token.");
            return;
        } else if (!StringUtils.hasText(token.getSessionIdHash())) {
            log.error("Attempted to put token with empty key.");
        }
        tokens.put(token.getSessionIdHash(), token);
    }

    @Override
    public void updateToken(SingleUseToken token) {
        if (token == null) {
            log.error("Attempted to update with null token.");
            return;
        }
        tokens.remove(token.getSessionIdHash());
        tokens.put(token.getSessionIdHash(), token);
    }

    @Override
    public void removeToken(String key) {
        if (!StringUtils.hasText(key)) {
            log.error("Attempted to remove a token with an empty key.");
            return;
        }
        tokens.remove(key);
    }

    @Override
    public SingleUseToken getToken(String key) {
        if (!StringUtils.hasText(key)) {
            log.error("Attempted to get a token for an empty key.");
            return null;
        }

        SingleUseToken token = tokens.get(key);
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
            return isValidToken(tokens.get(key), tokenValiditySecond);
        }
    }
}
