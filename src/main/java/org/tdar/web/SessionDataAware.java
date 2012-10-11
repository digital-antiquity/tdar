package org.tdar.web;

/**
 * $Id$
 * 
 * Marker interface for classes that need access to session data.  Currently using Spring to manage
 * the SessionData object as a session-scoped bean that gets injected into anything that is SessionDataAware.
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface SessionDataAware {
    public final static String RETURN = "return";
    public SessionData getSessionData();
    public void setSessionData(SessionData sessionData);
}
