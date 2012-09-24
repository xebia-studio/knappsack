package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Application;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ApplicationDescriptionComparatorTest {
    private ApplicationDescriptionComparator applicationDescriptionComparator = new ApplicationDescriptionComparator();

    private Application application1;
    private Application application2;

    @Before
    public void before() throws Exception {
        application1 = new Application();
        application2 = new Application();

        application1.setDescription("ABC");
        application2.setDescription("ZYX");
    }

    /**
     * Method: compare(Object app1, Object app2)
     */
    @Test
    public void testCompare() throws Exception {
        int result = applicationDescriptionComparator.compare(application1, application2);
        assertTrue(result < 0);

        result = applicationDescriptionComparator.compare(application2, application1);
        assertTrue(result > 0);
    }


} 
