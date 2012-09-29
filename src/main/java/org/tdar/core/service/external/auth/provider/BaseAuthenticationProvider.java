package org.tdar.core.service.external.auth.provider;

import org.apache.log4j.Logger;
import org.tdar.core.service.external.auth.AuthenticationProvider;

public abstract class BaseAuthenticationProvider implements AuthenticationProvider {

    protected final Logger logger = Logger.getLogger(getClass());
}
