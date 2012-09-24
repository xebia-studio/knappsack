package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.QAppFile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("appFileDao")
public class AppFileDaoImpl extends BaseDao implements AppFileDao {
    QAppFile appFile = QAppFile.appFile;

    public void add(AppFile appFile) {
        getEntityManager().persist(appFile);
    }

    public List<AppFile> getAll() {
        return query().from(appFile).listDistinct(appFile);
    }

    public AppFile get(Long id) {
        return getEntityManager().find(AppFile.class, id);
    }

    public void delete(AppFile appFile) {
        getEntityManager().remove(appFile);
    }

    public void update(AppFile appFile) {
        getEntityManager().merge(appFile);
    }
}
