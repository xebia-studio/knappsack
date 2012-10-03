package com.sparc.knappsack.security;

import java.util.HashMap;
import java.util.Map;

public class SingleUseTokenRepositoryImpl implements SingleUseTokenRepository {
    private final Map<String, SingleUseToken> tokens = new HashMap<String, SingleUseToken>();

    @Override
    public void putToken(SingleUseToken token) {
        tokens.put(token.getSessionIdHash(), token);
    }

    @Override
    public void updateToken(SingleUseToken token) {
        tokens.remove(token.getSessionIdHash());
        tokens.put(token.getSessionIdHash(), token);
    }

    @Override
    public void removeToken(String key) {
        tokens.remove(key);
    }

    @Override
    public SingleUseToken getToken(String key) {
        SingleUseToken token = tokens.get(key);
        removeToken(key);
        return token;
    }
}
