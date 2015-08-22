package org.tdar.core.service;

/**
 * Interface to support @link AbstractConfigurableService and services like the DOIService which may have different DAO backings
 * 
 * @author abrin
 * 
 * @param <S>
 */
public interface ConfigurableService<S extends Configurable> {

    S getProvider();

    boolean isServiceRequired();

}
