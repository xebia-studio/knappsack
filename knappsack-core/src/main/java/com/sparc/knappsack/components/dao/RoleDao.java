package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Role;

import java.util.List;


public interface RoleDao extends Dao<Role> {

    /**
     * @param authority String - This String will be the same value as the name of a UserRole enum
     * @return Role
     * @see com.sparc.knappsack.enums.UserRole
     */
    Role getByAuthority(String authority);

    /**
     * @return List of all Role entities
     */
    List<Role> getAll();
}
