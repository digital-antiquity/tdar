package org.tdar.transform;

/*
 * Interface for JAXB Conversion to DC, MODS, or other types
 */
public interface Transformer<S, R> {

    R transform(S source);

}
