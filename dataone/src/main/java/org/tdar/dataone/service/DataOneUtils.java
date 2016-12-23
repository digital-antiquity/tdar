package org.tdar.dataone.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Subject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.dataone.bean.EntryType;

/**
 * helper class
 * 
 * @author abrin
 *
 */
public class DataOneUtils {

    private static final transient Logger logger = LoggerFactory.getLogger(DataOneUtils.class);

    public static Subject createSubject(String name) {
        Subject subject = new Subject();
        subject.setValue(name);
        return subject;
    }

    public static Identifier createIdentifier(String formattedIdentifier) {
        Identifier id = new Identifier();
        id.setValue(formattedIdentifier);
        logger.debug("setting identifier:{}", formattedIdentifier);
        return id;
    }

    public static Checksum createChecksum(String checksum) {
        Checksum cs = new Checksum();
        cs.setAlgorithm("MD5");
        cs.setValue(checksum);
        return cs;
    }

    public static AccessRule createAccessRule(Permission permission, String name) {
        AccessRule rule = new AccessRule();
        rule.getPermissionList().add(permission);
        if (StringUtils.isNotBlank(name)) {
            rule.getSubjectList().add(DataOneUtils.createSubject(name));
        }
        return rule;
    }

    public static ObjectFormatIdentifier contentTypeToD1Format(EntryType type, String contentType) {
        ObjectFormatIdentifier identifier = new ObjectFormatIdentifier();
        switch (type) {
            case D1:
                identifier.setValue(DataOneConstants.D1_RESOURCE_MAP_FORMAT);
                break;
            case FILE:
                break;
            case TDAR:
                identifier.setValue(DataOneConstants.D1_DC_FORMAT);
                break;
            default:
                identifier.setValue("BAD-FORMAT");
                break;
        }
        return identifier;
    }

    public static String checksumString(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String result = DigestUtils.md5Hex(string);
        return result;
    }

    public static DateTime parseAndConvertToLocalTime(final String string) {
        String local = StringUtils.replace(string, " ", "+"); // fix 2016-12-21T18:10:37.445 00:00 from DataOne to 2016-12-21T18:10:37.445+00:00 
        DateTime dt = new DateTime(local,DateTimeZone.UTC);
        return dt.toDateTime(DateTimeZone.getDefault());
    }

    public static DateTime toUtc(Date date) {
        return new DateTime(date, DateTimeZone.getDefault()).toDateTime(DateTimeZone.UTC);
    }

    public static String createSeriesId(Long id, EntryType type) {
        return id.toString() + DataOneConstants.D1_SEP + type.getUniquePart();
    }
}
