package com.sparc.knappsack.security;

public abstract class AbstractSingleUseTokenRepository {

    protected boolean isValidToken(SingleUseToken singleUseToken, int seconds) {
        if (singleUseToken != null && (singleUseToken.getDate().getTime() + seconds*1000 >= System.currentTimeMillis())) {
            return true;
        }

        return false;
    }

}
