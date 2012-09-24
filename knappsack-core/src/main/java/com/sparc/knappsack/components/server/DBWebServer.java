package com.sparc.knappsack.components.server;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.sql.SQLException;

/**
 * This class starts up the H2 web server in order to conveniently view the database.
 * This should only be used during development.
 */
public final class DBWebServer {
    private static final Logger LOG = LoggerFactory.getLogger(DBWebServer.class);

    private static Server webServer;

    private DBWebServer() {
    }

    public static Server createWebServer(String... args) throws SQLException {

        if (webServer == null) {
            try {
                synchronized (DBWebServer.class) {
                    if(webServer == null) {
                        webServer = Server.createWebServer(args);
                    }
                }
            } catch (SQLException e) {
                LOG.error("SQLException caught creating the H2 web server console", e);
                throw new SQLException(e);
            }
        }
        return webServer;
    }

    @PreDestroy
    public static void stopServer() {
        if (webServer != null) {
            webServer.getService().stop();
            webServer.stop();
        }
    }

    @SuppressWarnings("unused")
    public static Server getWebServer() {
        return webServer;
    }

}
