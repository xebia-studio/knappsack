package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Invitation;

import java.io.Serializable;
import java.util.Comparator;

public class InvitationDomainNameComparator implements Comparator<Invitation>, Serializable {
    private static final long serialVersionUID = 990737072542213520L;

    @Override
    public int compare(Invitation invitation, Invitation invitation2) {
        String name1 = invitation.getDomain().getName();
        String name2 = invitation2.getDomain().getName();

        return name1.compareToIgnoreCase(name2);
    }
}
