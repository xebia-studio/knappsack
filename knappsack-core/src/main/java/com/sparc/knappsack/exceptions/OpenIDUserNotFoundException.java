package com.sparc.knappsack.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAuthenticationToken;

public class OpenIDUserNotFoundException extends UsernameNotFoundException {

    private OpenIDAuthenticationToken token;

    public OpenIDUserNotFoundException(String msg) {
        super(msg);
    }

    public OpenIDUserNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    public OpenIDUserNotFoundException(String msg, OpenIDAuthenticationToken token) {
        super(msg);

        this.token = token;
    }

    public OpenIDAuthenticationToken getOpenIDAuthenticationToken() {
        return this.token;
    }
}
