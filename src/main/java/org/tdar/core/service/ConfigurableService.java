package org.tdar.core.service;

public interface ConfigurableService<S extends Configurable> {

    S getProvider();

    boolean isServiceRequired();

}
