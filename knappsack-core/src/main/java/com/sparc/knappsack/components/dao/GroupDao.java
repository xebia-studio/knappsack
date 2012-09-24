package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;

import java.util.List;

public interface GroupDao extends Dao<Group> {

    /**
     * @param name String
     * @param organization Organization
     * @return Group matching the given name and Organization
     */
    Group get(String name, Organization organization);

    /**
     * @return List of all Group entities
     */
    List<Group> getAll();

    /**
     * @param applicationVersionId Id of the application version to search on
     * @return List of all Group entities that have guest permissions to the given ApplicationVersion
     */
    List<Group> getAllGuestGroups(long applicationVersionId);

    /**
     * @param application Application
     * @return Group of which the given Application is owned (edit permissions)
     */
    Group getOwnedGroup(Application application);

    /**
     * @param accessCode String - UUID of the Group
     * @return Group with the given UUID
     */
    Group getGroupByAccessCode(String accessCode);
}
