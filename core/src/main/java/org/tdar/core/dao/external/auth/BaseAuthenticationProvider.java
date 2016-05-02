package org.tdar.core.dao.external.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAuthenticationProvider implements AuthenticationProvider {

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean enabled;

    /**
     * @return the enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
