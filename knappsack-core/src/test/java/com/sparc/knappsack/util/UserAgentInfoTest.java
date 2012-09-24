package com.sparc.knappsack.util;

import com.sparc.knappsack.enums.ApplicationType;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserAgentInfoTest {
    private static final String IOS_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";
    private static final String HTTP_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    private UserAgentInfo userAgentInfo;

    @Before
    public void before() throws Exception {
        userAgentInfo = new UserAgentInfo(IOS_USER_AGENT, HTTP_HEADER);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: isChrome()
     */
    @Test
    public void testIsChrome() throws Exception {
        assertFalse(userAgentInfo.isChrome());
    }

    /**
     * Method: isFirefox()
     */
    @Test
    public void testIsFirefox() throws Exception {
        assertFalse(userAgentInfo.isFirefox());
    }

    /**
     * Method: getApplicationType()
     */
    @Test
    public void testGetApplicationType() throws Exception {
        ApplicationType applicationType = userAgentInfo.getApplicationType();
        assertTrue(ApplicationType.IPHONE.equals(applicationType));
    }


} 
