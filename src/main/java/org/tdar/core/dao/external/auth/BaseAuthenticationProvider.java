package org.tdar.core.dao.external.auth;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAuthenticationProvider implements AuthenticationProvider {

    protected org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    
    private boolean enabled;

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
