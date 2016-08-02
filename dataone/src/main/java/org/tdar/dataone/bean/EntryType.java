package org.tdar.dataone.bean;

import org.apache.commons.lang3.StringUtils;
import org.tdar.dataone.service.DataOneService;

/**
 * The Type of Entry Response.  Types include:
 * DataOne - a ResourceMap file
 * TDAR - our local metadata
 * FILE - an actual file
 * UNKNOWN - an unknown format type
 * @author abrin
 *
 */
public enum EntryType {
    D1, TDAR, FILE, UNKOWN;
    
    public static EntryType getTypeFromFormatId(String format) {
        if (StringUtils.equalsIgnoreCase(format, DataOneService.D1_RESOURCE_MAP_FORMAT)) {
            return D1;
        }
        if (StringUtils.equalsIgnoreCase(format, DataOneService.D1_DC_FORMAT)) {
            return EntryType.TDAR;
        }
        return EntryType.UNKOWN;
    }
}
