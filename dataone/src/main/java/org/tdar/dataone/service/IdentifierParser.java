package org.tdar.dataone.service;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.dataone.bean.EntryType;
import org.tdar.utils.PersistableUtils;

public class IdentifierParser implements DataOneConstants {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    DataOneConfiguration D1CONFIG = DataOneConfiguration.getInstance();
    private String base;
    private String doi;
    private String partIdentifier;
    private InformationResource ir;
    private Date modified;
    private EntryType type;

    public IdentifierParser(String id_, InformationResourceService informationResourceService) {
        logger.debug("looking for Id: {}", id_);
        base = StringUtils.substringBefore(id_, D1_SEP);
        // switching back DOIs to have / in them.
        doi = base.replace(D1CONFIG.getDoiPrefix() + ":", D1CONFIG.getDoiPrefix() + "/");
        partIdentifier = StringUtils.substringAfter(id_, D1_SEP);
        ir = informationResourceService.findByDoi(doi);
        if (PersistableUtils.isNullOrTransient(ir)) {
            logger.debug("resource not foound: {}", doi);
            return;
        }
        logger.debug("{} --> {} (id: {} )", doi, ir.getId(), partIdentifier);
        if (partIdentifier.startsWith(D1_FORMAT)) {
            type = EntryType.D1;
            parseDate(D1_FORMAT);
        } else if (partIdentifier.startsWith(META)) {
            type = EntryType.TDAR;
            modified = null;
            if (partIdentifier.contains(D1_VERS_SEP)) {
                parseDate(D1_VERS_SEP);
            }
        } else if (partIdentifier.contains(D1_VERS_SEP)) {
            type = EntryType.FILE;
        } else {
            logger.warn("bad format for: {}", id_);
        }
    }

    private void parseDate(String part) {
        String date = StringUtils.substringAfter(partIdentifier, part);
        try {
            modified = new Date(Long.parseLong(date));
        } catch (NumberFormatException pe) {
            logger.debug("parse exception: {} {}", date, pe, pe);
        }
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getPartIdentifier() {
        return partIdentifier;
    }

    public void setPartIdentifier(String partIdentifier) {
        this.partIdentifier = partIdentifier;
    }

    public InformationResource getIr() {
        return ir;
    }

    public void setIr(InformationResource ir) {
        this.ir = ir;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

}
