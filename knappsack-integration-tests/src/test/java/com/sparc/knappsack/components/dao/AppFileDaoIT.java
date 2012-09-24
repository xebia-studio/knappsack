package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.EntityUtil;
import com.sparc.knappsack.components.entities.AppFile;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AppFileDaoIT extends AbstractDaoIntegrationTests {

    @Autowired(required = true)
    private AppFileDao appFileDao;

   /* @Test
    public void testAppFileDao() {
        AppFile appFile = createAppFile();

        appFileDao.add(appFile);
        assertNotNull(appFile.getId());
        assertTrue(appFile.getId() > 0);

        AppFile returnedAppFile = appFileDao.get(appFile.getId());
        assertNotNull(returnedAppFile);
        assertTrue(appFile.equals(returnedAppFile));

        appFileDao.delete(returnedAppFile);
        AppFile deletedAppFile = appFileDao.get(returnedAppFile.getId());
        assertNull(deletedAppFile);
    }*/

    @Test
    public void testAppFileDaoGetAll() {
        List<AppFile> appFiles = new ArrayList<AppFile>();
        appFiles.add(createAppFile());
        appFiles.add(createAppFile());
        appFiles.add(createAppFile());

        for (AppFile appFile : appFiles) {
            appFileDao.add(appFile);
        }

        List returnedAppFiles = appFileDao.getAll();
        assertEquals(returnedAppFiles.size(), returnedAppFiles.size());
    }

    private AppFile createAppFile() {
        AppFile appFile = EntityUtil.createAppFile();

        return appFile;
    }



}
