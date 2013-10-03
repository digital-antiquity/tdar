package org.tdar.core.dao.external.auth;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public abstract class BaseAuthenticationProvider implements AuthenticationProvider {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    
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
