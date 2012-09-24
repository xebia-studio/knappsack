package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class ApplicationVersionComparatorTest {
    private ApplicationVersionComparator applicationVersionComparator = new ApplicationVersionComparator();
    private ApplicationVersion appVersion1;
    private ApplicationVersion appVersion2;

    @Before
    public void before() throws Exception {
        appVersion1 = new ApplicationVersion();
        appVersion2 = new ApplicationVersion();

        appVersion1.setVersionName("1.0.0");
        appVersion2.setVersionName("2.0.0");
    }

    /**
     * Method: compare(Object obj1, Object obj2)
     */
    @Test
    public void testCompare() throws Exception {
        int result = applicationVersionComparator.compare(appVersion1, appVersion2);
        assertTrue(result < 0);

        result = applicationVersionComparator.compare(appVersion2, appVersion1);
        assertTrue(result > 0);
    }

} 
