package com.sparc.knappsack.security;

import com.sparc.knappsack.components.dao.UserDetailsDao;
import com.sparc.knappsack.components.entities.User;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class MemCachedSessionRegistryImpl implements SessionRegistry, ApplicationListener<SessionDestroyedEvent> {

    private static final Logger log = LoggerFactory.getLogger(MemCachedSessionRegistryImpl.class);

    @Qualifier("memcachedClient")
    @Autowired(required = true)
    private MemcachedClient client;

    @Qualifier("userDetailsDao")
    @Autowired(required = true)
    private UserDetailsDao userDetailsDao;

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        String sessionId = event.getId();
        removeSessionInformation(sessionId);
    }

    @Override
    public List<Object> getAllPrincipals() {
        Set<String> uuids = new HashSet<String>();
        for (User user : userDetailsDao.getAll()) {
            uuids.add(user.getUuid());
        }

        List<Object> principals = new ArrayList<Object>();
        if (uuids.size() > 0) {
            Map<String, Object> storedPrincipals = client.getBulk(uuids);
            principals.addAll(storedPrincipals.values());
        }

        return principals;
    }

    @Override
    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions) {
        List<SessionInformation> sessions = new ArrayList<SessionInformation>();
        if (principal instanceof User) {
            Set<String> sessionIds = (Set<String>) client.get(((User) principal).getUuid());
            if (sessionIds != null) {
                for (String id : sessionIds) {
                    SessionInformation info = (SessionInformation) client.get(id);
                    if (info != null) {
                        sessions.add(info);
                    } else {
                        removeSessionInformation(id);
                    }
                }
            }
        }
        return sessions;
    }

    @Override
    public SessionInformation getSessionInformation(String sessionId) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");

        return (SessionInformation) client.get(sessionId);
    }

    @Override
    public void refreshLastRequest(String sessionId) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");

        SessionInformation info = getSessionInformation(sessionId);

        if (info != null) {
            info.refreshLastRequest();
            client.replace(sessionId, 86400, info);
        }
    }

    @Override
    public void registerNewSession(String sessionId, Object principal) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");
        Assert.notNull(principal, "Principal required as per interface contract");
        Assert.isInstanceOf(User.class, principal, "Principal not instance of User");

        if (log.isDebugEnabled()) {
            log.debug("Registering session " + sessionId +", for principal " + principal);
        }

        if (getSessionInformation(sessionId) != null) {
            removeSessionInformation(sessionId);
        }

        client.set(sessionId, 86400, new SessionInformation(principal, sessionId, new Date()));

        Set<String> sessionsUsedByPrincipal = (Set<String>) client.get(((User) principal).getUuid());

        if (sessionsUsedByPrincipal == null) {
            sessionsUsedByPrincipal = new CopyOnWriteArraySet<String>();
            if (!isSuccess(client.add(((User) principal).getUuid(), 86400, sessionsUsedByPrincipal))) {
                Set<String> prevSessionsUsedByPrincipal = (Set<String>) client.get(((User) principal).getUuid());

                if (prevSessionsUsedByPrincipal != null) {
                    sessionsUsedByPrincipal = prevSessionsUsedByPrincipal;
                }
            }
        }

        sessionsUsedByPrincipal.add(sessionId);
        client.set(((User) principal).getUuid(), 86400, sessionsUsedByPrincipal);

        if (log.isTraceEnabled()) {
            log.trace("Sessions used by '" + principal + "' : " + sessionsUsedByPrincipal);
        }
    }

    @Override
    public void removeSessionInformation(String sessionId) {
        Assert.hasText(sessionId, "SessionId required as per interface contract");

        SessionInformation info = getSessionInformation(sessionId);

        if (info == null) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.debug("Removing session " + sessionId + " from set of registered sessions");
        }

        if (!isSuccess(client.delete(sessionId))) {
            log.error(String.format("Error removing session from cache: %s", sessionId));
        }

        Set<String> sessionsUsedByPrincipal = (Set<String>) client.get(((User) info.getPrincipal()).getUuid());

        if (sessionsUsedByPrincipal == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Removing session " + sessionId + " from principal's set of registered sessions");
        }

        sessionsUsedByPrincipal.remove(sessionId);

        if (sessionsUsedByPrincipal.isEmpty()) {
            // No need to keep object in principals Map anymore
            if (log.isDebugEnabled()) {
                log.debug("Removing principal " + info.getPrincipal() + " from registry");
            }
            client.delete(((User) info.getPrincipal()).getUuid());
        } else {
            client.replace(((User) info.getPrincipal()).getUuid(), 86400, sessionsUsedByPrincipal);
        }

        if (log.isTraceEnabled()) {
            log.trace("Sessions used by '" + info.getPrincipal() + "' : " + sessionsUsedByPrincipal);
        }
    }

    private boolean isSuccess(OperationFuture<Boolean> obj) {
        boolean success = false;
        if (obj != null) {
            success = obj.getStatus().isSuccess();
        }
        return success;
    }

    @PostConstruct
    private void postConstruct() {

    }

    @PreDestroy
    private void preDestroy() {
        log.info("Shutting down Memcached");
        client.shutdown();
    }
}
