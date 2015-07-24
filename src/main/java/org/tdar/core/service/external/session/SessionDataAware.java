package org.tdar.core.service.external.session;

/**
 * $Id$
 * 
 * Marker interface for classes that need access to session data. Currently using Spring to manage
 * the SessionData object as a session-scoped bean that gets injected into anything that is SessionDataAware.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface SessionDataAware {
    SessionData getSessionData();

    void setSessionData(SessionData sessionData);
}
