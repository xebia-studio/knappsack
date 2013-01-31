package com.sparc.knappsack.security;

import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;

/**
 * Override JdbcTokenRepositoryImpl in order to add an 'exists' check on table creation for development environments.
 */
public class CustomJdbcTokenRepositoryImpl extends JdbcTokenRepositoryImpl {

    public static final String CREATE_TABLE_SQL =
            "create table IF NOT EXISTS persistent_logins (username varchar(255) not null, series varchar(64) primary key, " +
                    "token varchar(64) not null, last_used timestamp not null)";

    private boolean createTableOnStartup;

    protected void initDao() {
        if (createTableOnStartup) {
            getJdbcTemplate().execute(CREATE_TABLE_SQL);
        }
    }

    public void setCreateTableOnStartup(boolean createTableOnStartup) {
        this.createTableOnStartup = createTableOnStartup;
    }
}
