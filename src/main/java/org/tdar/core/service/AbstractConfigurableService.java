package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public abstract class AbstractConfigurableService<S extends Configurable> implements ConfigurableService<S> {

    private final Logger logger = Logger.getLogger(getClass());
    private List<S> allServices = new ArrayList<>();

    /**
     * returns all services that the system knows about regardless of whether they're enabled or not.
     * 
     * @return
     */
    public List<S> getAllServices() {
        return allServices;
    }

    /**
     * Chooses the providers of the service. The first configured and enabled one will become the one that is used.
     * 
     * @param providers
     *            The list of providers that will be examined to see if they are suitable. Ordinarily provided by Spring.
     * @throws IllegalStateException
     *             if the service is required, but a correctly configured and enabled provider is not found.
     */
    @Autowired
    public void setAllServices(List<S> providers) {
        allServices.clear();
        if (providers != null) {
            Iterator<S> iterator = providers.iterator();
            while (iterator.hasNext()) {
                S provider = iterator.next();
                if (provider.isConfigured()) {
                    if (provider.isEnabled()) {
                        allServices.add(provider);
                        logger.debug(String.format("Provider: %s is enabled & configured.", provider.getClass().getSimpleName()));
                    } else {
                        logger.debug(String.format("not enabled, so disabling provider: %s", provider.getClass().getSimpleName()));
                    }
                } else {
                    logger.debug(String.format("not configured, so disabling provider: %s", provider.getClass().getSimpleName()));
                }
            }
        }
        if (allServices.isEmpty() && isServiceRequired()) {
            String message = String.format(
                    "No configured %s found. A Provider must be specified in spring-local-settings.xml and have a valid configuration.",
                    getClass().getSimpleName());
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (!allServices.isEmpty()) {
            logger.debug(String.format("Provider used will be: %s", getProvider().getClass().getSimpleName()));
        }
    }

    /**
     * @return the first provider in the list of providers. NB: will return <b>null</b> if service is not required and no services were configured.
     * @see org.tdar.core.service.ConfigurableService#getProvider()
     */
    @Override
    public S getProvider() {
        if (allServices.isEmpty()) {
            logger.warn(String.format("no available provider found by service  %s", getClass()));
            return null;
        }
        return allServices.get(0);
    }

    /**
     * If not required, the getProvider method can return <b>null</b> if no provider is found!<br />
     * Made abstract to ensure that you are aware of this point if you override this class!
     * 
     * @see org.tdar.core.service.ConfigurableService#isServiceRequired()
     */
    @Override
    abstract public boolean isServiceRequired();
}
