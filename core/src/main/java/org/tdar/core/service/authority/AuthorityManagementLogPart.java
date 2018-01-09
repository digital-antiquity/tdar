package org.tdar.core.service.authority;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.utils.jaxb.IdList;
import org.tdar.utils.jaxb.converters.JaxbMapConverter;

/**
 * Static class for the Log Entry Part used to log to XML via JAXB
 * 
 * @author abrin
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "logPart")
public class AuthorityManagementLogPart {

    private HashMap<String, IdList> fieldToDupeIds = new HashMap<String, IdList>();

    public void add(String fieldName, Long dupeId) {
        IdList dupeIds = fieldToDupeIds.get(fieldName);
        if (dupeIds == null) {
            dupeIds = new IdList();
            fieldToDupeIds.put(fieldName, dupeIds);
        }
        dupeIds.add(dupeId);
    }

    /**
     * @return the fieldToDupeIds
     */
    @XmlElement
    // @XmlAnyElement(lax=true)
    @XmlJavaTypeAdapter(JaxbMapConverter.class)
    public HashMap<String, IdList> getFieldToDupeIds() {
        return fieldToDupeIds;
    }

    @Override
    public String toString() {
        return fieldToDupeIds.toString();
    }

}