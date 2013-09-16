package org.tdar.core.service;

public interface ConfigurableService<S extends Configurable> {

    public S getProvider();

    public boolean isServiceRequired();

}
