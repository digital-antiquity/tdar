package org.tdar.core.bean;

import java.io.Serializable;

/**
 * Base Class for things that can be emitted to JSON. Ultimately this should be removed and replaced with the
 * JACKSON JSON Conversion through JAXB
 */
public interface JsonModel extends Serializable {

}
