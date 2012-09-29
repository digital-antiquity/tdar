package org.tdar.core.service;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.exception.TdarRuntimeException;

/**
 * $Id$
 * 
 * Base scaffolding for services that may select from a set of Configurable providers.  
 * Current examples include DOI (EZID, anspid) and Auth (Crowd, LDAP) services.
 * 
 * @author Adam Brin
 * @version $Rev$
 * @param <S>
 */
@Service
public abstract class AbstractConfigurableService<S extends Configurable> {

    private final Logger logger = Logger.getLogger(getClass());
    private List<S> allServices;

    public List<S> getAllServices() {
        return allServices;
    }
    
    @Autowired
    public void setAllServices(List<S> providers) {
        allServices = providers;
        if (allServices != null) {
            Iterator<S> iterator = allServices.iterator();
            while (iterator.hasNext()) {
                S provider = iterator.next();
                if (provider.isConfigured()) {
                    logger.debug(String.format("enabling %s provider: %s will use first", getClass().getSimpleName(), provider.getClass().getSimpleName()));
                } else {
                    logger.debug(String.format("disabling unconfigured %s provider: %s", getClass().getSimpleName(), provider.getClass().getSimpleName()));
                    iterator.remove();
                }
            }
        }
    }

    public S getProvider() {
        if (allServices.isEmpty()) {
            throw new TdarRuntimeException(String.format(
                    "No configured %s found. An AuthenticationProvider must be specified in spring-local-settings.xml and have a valid configuration.",
                    getClass().getSimpleName()));
        }
        return allServices.get(0);
    }

}
