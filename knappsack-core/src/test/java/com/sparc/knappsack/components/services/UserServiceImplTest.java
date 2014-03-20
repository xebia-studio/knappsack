package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.enums.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserServiceImplTest {

    @Test
    public void shouldFilterPublishedVersions() {
        UserServiceImpl userService = new UserServiceImpl();

        List<ApplicationVersion> versions = new ArrayList<ApplicationVersion>();
        versions.add(createMock(2L, "1.0.0"));
        versions.add(createMock(1L, "1.0.0"));
        versions.add(createMock(6L, "1.0.0"));
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
        versions.add(createRandomMock());
//        Collections.shuffle(versions);

        int expectedCount = 10;
        String expectedFirst = "1.0.0";

        // When
        List<ApplicationVersion> result = userService.filterAndSortApplicationVersions(SortOrder.ASCENDING, 10, versions);
        for (ApplicationVersion v : result) {
            System.out.println(v.getId() + " "+  v.getVersionName());
        }
        Assert.assertEquals(expectedCount, result.size());
        Assert.assertEquals(expectedFirst, result.get(0).getVersionName());


    }

    private ApplicationVersion createMock(Long id, String versionName) {
        ApplicationVersion version = new ApplicationVersion();
        version.setId(id);
        version.setVersionName(versionName);
        return version;
    }

    private ApplicationVersion createRandomMock() {
        ApplicationVersion version = new ApplicationVersion();
        version.setId((long) (Math.random() * 1000000));
        version.setVersionName(UUID.randomUUID().toString());
        return version;
    }

}
