package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.User;

import java.util.List;

public interface UserDetailsDao extends Dao<User> {

    /**
     * @return List of all User entities
     */
    List<User> getAll();

    List<User> get(List<Long> ids);

    /**
     * @param openIdIdentifier String
     * @return User with the given openID identifier
     */
    User findByOpenIdIdentifier(String openIdIdentifier);

    /**
     * @param email String - email address for the User
     * @return User with the given email address
     */
    User findByEmail(String email);

    /**
     * @param userName String
     * @return User with the given user name
     */
    User findByUserName(String userName);

    /**
     * @param ids a List of primary keys for which we want to retrieve all Users
     * @return List of all User entities matching the set of IDs
     */
    List<User> getBatch(List<Long> ids);

    /**
     * @return long - a count of all the users in the system
     */
    long countAll();
}
