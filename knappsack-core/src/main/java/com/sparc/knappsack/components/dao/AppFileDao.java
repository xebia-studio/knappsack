package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.AppFile;

import java.util.List;

public interface AppFileDao extends Dao<AppFile> {
    /**
     * @return all AppFile entities
     */
    public List<AppFile> getAll();

}
