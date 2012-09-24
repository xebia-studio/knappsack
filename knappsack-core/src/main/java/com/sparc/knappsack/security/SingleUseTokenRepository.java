package com.sparc.knappsack.security;

public interface SingleUseTokenRepository {

    void putToken(SingleUseToken token);

    void updateToken(SingleUseToken token);

    void removeToken(String key);

    SingleUseToken getToken(String key);

}
