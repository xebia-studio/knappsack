package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Application;

import java.io.Serializable;
import java.util.Comparator;

public class ApplicationDescriptionComparator implements Comparator<Application>,Serializable {

    private static final long serialVersionUID = 3301132247462308327L;

    @Override
    public int compare(Application app1, Application app2) {

        String app1Desc = app1.getDescription();
        String app2Desc = app2.getDescription();

        return app1Desc.compareToIgnoreCase(app2Desc);
    }

}
