package com.sparc.knappsack.security;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Tests the SingleUseToken Class.
 */

public class SingleUseTokenTest {

    private SingleUseToken singleUseToken;

    @Before
    public void before() throws Exception {
        singleUseToken = new SingleUseToken("1");
    }

    @Test
    public void testGetSessionIdHash() {
        //SessionId = "1" should always return the hash shown below.
        assertEquals("c4ca4238a0b923820dcc509a6f75849b", singleUseToken.getSessionIdHash());
    }

}
