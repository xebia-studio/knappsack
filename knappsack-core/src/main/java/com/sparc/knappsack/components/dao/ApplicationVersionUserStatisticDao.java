package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.ApplicationVersionUserStatistic;
import com.sparc.knappsack.components.entities.User;

import java.util.List;

public interface ApplicationVersionUserStatisticDao extends Dao<ApplicationVersionUserStatistic> {
    /**
     * @param applicationVersion ApplicationVersion - get all download statistics for this ApplicationVersion and User
     * @param user User - get all download statistics for this ApplicationVersion and User
     * @return List<ApplicationVersionUserStatistic> - all download statistics around this ApplicationVersion and User
     */
    List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion, User user);

    /**
     * @param applicationVersion ApplicationVersion - get all download statistics for this ApplicationVersion
     * @return List<ApplicationVersionUserStatistic>
     */
    List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion);

    /**
     * @param user User - get all download statistics for this User
     * @return List<ApplicationVersionUserStatistic>
     */
    List<ApplicationVersionUserStatistic> get(User user);

    /**
     * @param application Application - get all download statistis for this Application, includes all versions
     * @return List<ApplicationVersionUserStatistic>
     */
    List<ApplicationVersionUserStatistic> get(Application application);

    /**
     * @param applicationVersion ApplicationVersion - delete all download statistics for this ApplicationVersion
     * @return long - number of rows deleted
     */
    long deleteAllForApplicationVersion(ApplicationVersion applicationVersion);

}
