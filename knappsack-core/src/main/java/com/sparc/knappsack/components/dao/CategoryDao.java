package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.ApplicationType;

import java.util.List;

public interface CategoryDao extends Dao<Category>{

    /**
     * @return List of all Category entities
     */
    List<Category> getAll();

    /**
     * @param user
     * @return List of all Category entities which are available for the specified user.  Only categories which have an application will be returned.
     */
    List<Category> getAllForUser(User user, ApplicationType deviceType);
}
