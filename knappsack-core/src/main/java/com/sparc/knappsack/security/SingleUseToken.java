package com.sparc.knappsack.security;

import com.sparc.knappsack.exceptions.TokenException;
import org.hsqldb.lib.MD5;

import java.util.Date;

public class SingleUseToken {

    private Date date;
    private String sessionIdHash;

    public SingleUseToken(String sessionId) throws TokenException {
        try {
            MD5 md5 = new MD5();
            this.sessionIdHash = md5.digest(sessionId);
        } catch (Exception e) {
            throw new TokenException("Error creating SingleUseToken.", e);
        }

        date = new Date();
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public String getSessionIdHash() {
        return sessionIdHash;
    }

}
