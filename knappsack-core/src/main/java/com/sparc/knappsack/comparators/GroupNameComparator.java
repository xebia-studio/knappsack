package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Group;

import java.io.Serializable;
import java.util.Comparator;

public class GroupNameComparator implements Comparator<Group>, Serializable {
    private static final long serialVersionUID = 2900785266146076143L;

    @Override
    public int compare(Group group, Group group2) {
        String name1 = group.getName();
        String name2 = group2.getName();

        return name1.compareToIgnoreCase(name2);
    }
}
