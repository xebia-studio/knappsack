package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.CustomBranding;

public interface CustomBrandingDao extends Dao<CustomBranding> {

    CustomBranding getBySubdomain(String subdomain);

}
