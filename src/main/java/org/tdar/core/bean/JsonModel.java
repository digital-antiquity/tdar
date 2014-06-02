package org.tdar.core.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.tdar.core.configuration.JSONTransient;
import org.tdar.utils.json.WhitelistFilter;

/**
 * Base Class for things that can be emitted to JSON. Ultimately this should be removed and replaced with the
 * JACKSON JSON Conversion through JAXB
 */
public interface JsonModel extends Serializable {

}
