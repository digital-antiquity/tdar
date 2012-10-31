package org.tdar.core.dao.external.auth;

import org.apache.log4j.Logger;

public abstract class BaseAuthenticationProvider implements AuthenticationProvider {

    protected final Logger logger = Logger.getLogger(getClass());
}
