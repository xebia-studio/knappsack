package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Application;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class ApplicationNameComparatorTest {

    private ApplicationNameComparator applicationNameComparator = new ApplicationNameComparator();

    private Application application1;
    private Application application2;

    @Before
    public void before() throws Exception {
        application1 = new Application();
        application2 = new Application();

        application1.setName("ABC");
        application2.setName("ZYX");
    }

    /**
     * Method: compare(Object app1, Object app2)
     */
    @Test
    public void testCompare() throws Exception {
        int result = applicationNameComparator.compare(application1, application2);
        assertTrue(result < 0);

        result = applicationNameComparator.compare(application2, application1);
        assertTrue(result > 0);
    }


} 
