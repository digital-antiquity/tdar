package org.tdar.tag;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for subjectType.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="subjectType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AGRICULTURE_AND_SUBSISTENCE"/>
 *     &lt;enumeration value="CIVIL"/>
 *     &lt;enumeration value="COMMEMORATIVE"/>
 *     &lt;enumeration value="COMMERCIAL"/>
 *     &lt;enumeration value="COMMUNICATIONS"/>
 *     &lt;enumeration value="DEFENCE"/>
 *     &lt;enumeration value="DOMESTIC"/>
 *     &lt;enumeration value="EDUCATION"/>
 *     &lt;enumeration value="GARDENS_PARKS_AND_URBAN_SPACES"/>
 *     &lt;enumeration value="HEALTH_AND_WELFARE"/>
 *     &lt;enumeration value="INDUSTRIAL"/>
 *     &lt;enumeration value="MARITIME"/>
 *     &lt;enumeration value="MONUMENT_BY_FORM"/>
 *     &lt;enumeration value="RECREATIONAL"/>
 *     &lt;enumeration value="RELIGIOUS_RITUAL_AND_FUNERARY"/>
 *     &lt;enumeration value="TRANSPORT"/>
 *     &lt;enumeration value="UNASSIGNED"/>
 *     &lt;enumeration value="WATER_SUPPLY_AND_DRAINAGE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "subjectType")
@XmlEnum
public enum SubjectType {

    AGRICULTURE_AND_SUBSISTENCE,
    CIVIL,
    COMMEMORATIVE,
    COMMERCIAL,
    COMMUNICATIONS,
    DEFENCE,
    DOMESTIC,
    EDUCATION,
    GARDENS_PARKS_AND_URBAN_SPACES,
    HEALTH_AND_WELFARE,
    INDUSTRIAL,
    MARITIME,
    MONUMENT_BY_FORM,
    RECREATIONAL,
    RELIGIOUS_RITUAL_AND_FUNERARY,
    TRANSPORT,
    UNASSIGNED,
    WATER_SUPPLY_AND_DRAINAGE;

    public String value() {
        return name();
    }

    public static SubjectType fromValue(String v) {
        return valueOf(v);
    }

}
