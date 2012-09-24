package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.enums.ApplicationType;

import java.util.List;

public interface ApplicationDao extends Dao<Application> {
    /**
     * @return All Application entities
     */
    List<Application> getAll();

    /**
     * @param applicationType ApplicationType
     * @return all Application entities of the given ApplicationType
     */
    List<Application> getAll(ApplicationType applicationType);

    /**
     * @param searchCriteria String - Used to get a list of all applications case-insensitive, wildcard matching a specified criteria.
     *  Searches on either application Name or Application Description.
     *  The below translates to the following traditional sql statement.
     *  Select * from ApplicationsObject where (NAME like '%Criteria%') OR (DESCRIPTION like '%Criteria%');
     * @return List of Application entities matching the search criteria
     */
    List<Application> getByNameAndDescription(String searchCriteria);

    /**
     * @param category Category
     * @return List of Application entities associated with the given Category
     */
    List<Application> getByCategory(Category category);

    /**
     * @param category Category
     * @param applicationType ApplicationType
     * @return List of Application entities for a given Category and ApplicationType
     */
    List<Application> getByCategoryAndApplicationType(Category category, ApplicationType applicationType);
}
