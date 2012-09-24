package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Category;

import java.util.List;

public interface CategoryDao extends Dao<Category>{

    /**
     * @return List of all Category entities
     */
    List<Category> getAll();
}
