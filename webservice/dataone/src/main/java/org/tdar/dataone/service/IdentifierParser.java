package org.tdar.dataone.service;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.base.DoiDao;
import org.tdar.core.dao.resource.InformationResourceDao;
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
    private boolean seriesIdentifier = false;

    public IdentifierParser(String id_, DoiDao doiDao) {
        logger.debug("looking for Id: {}", id_);
        base = StringUtils.substringBefore(id_, D1_SEP);
        // switching back DOIs to have / in them.
        doi = base.replace(D1CONFIG.getDoiPrefix() + ":", D1CONFIG.getDoiPrefix() + "/");
        partIdentifier = StringUtils.substringAfter(id_, D1_SEP);
        if (NumberUtils.isDigits(doi)) {
            ir = doiDao.find(InformationResource.class,Long.parseLong(doi));
            setSeriesIdentifier(true);
        } else {
            ir = doiDao.findByDoi(doi);
        }
        if (PersistableUtils.isNullOrTransient(ir)) {
            logger.debug("resource not foound: {}", doi);
            return;
        }
        logger.trace("{} --> {} (id: {} )", doi, ir.getId(), partIdentifier);
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
        if (StringUtils.isBlank(date)) {
            return;
        }
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

    public boolean isSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(boolean seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    /**
     * The Identifier is constructed by combining the type with additional information. As we do not track every version of a
     * record in tDAR, we use the "date" in the identifier name to differentiate within D1.
     * 
     * @param identifier
     * @param dateUpdated
     * @param type
     * @param irfId
     * @param version
     * @return
     */
    public static String formatIdentifier(String identifier, Date dateUpdated, EntryType type, Long irfId, Integer version) {
        StringBuilder sb = new StringBuilder();
        sb.append(webSafeDoi(identifier)).append(DataOneService.D1_SEP);
        switch (type) {
            case D1:
                sb.append(DataOneService.D1_FORMAT);
                sb.append(dateUpdated.getTime());
                break;
            case FILE:
                sb.append(irfId);
                sb.append(DataOneService.D1_VERS_SEP);
                sb.append(version);
                break;
            case TDAR:
                sb.append(DataOneService.META);
                sb.append(DataOneService.D1_VERS_SEP);
                sb.append(dateUpdated.getTime());
                break;
            default:
                break;
        }
        return sb.toString().replace(" ", "%20");

    }

    public static String formatIdentifier(String identifier, Date dateUpdated, EntryType type, InformationResourceFile irf) {
        if (irf == null) {
            return IdentifierParser.formatIdentifier(identifier, dateUpdated, type, null, null);
        }
        return IdentifierParser.formatIdentifier(identifier, dateUpdated, type, irf.getId(), irf.getLatestVersion());
    }

    public static String webSafeDoi(String identfier) {
        // switching back DOIs to have : in them instead of /.
        return identfier.replace("/", ":");
    }

}
