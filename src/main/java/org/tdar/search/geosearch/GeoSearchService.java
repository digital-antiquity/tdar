/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.geosearch;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.search.geosearch.GeoSearchDao.SpatialTables;
import org.tdar.struts.data.SvgMapWrapper;

/**
 * @author Adam Brin
 * 
 */
@Service
public class GeoSearchService {

    @Autowired(required=false)
    GeoSearchDao geoSearchDao;
    private final Logger logger = Logger.getLogger(getClass());

    // continent
    private static final String COL_CONTINENT_NAME = "continent";
    // country
    private static final String COL_COUNTRY_LONG_NAME = "long_name";
    // country code
    private static final String COL_ISO_2DIGITS = "iso_2digit";

    // admin (state)
    private static final String COL_ADMIN_NAME = "admin_name";

    // us_counties (county)
    private static final String COL_COUNTY_NAME = "cnty_name";

    /*
     * extracts all geographic keywords from a lat-long-box, if however, the area of the box is deemed too large, it will not add county information. This is
     * beacuse you don't want to show county info if you're provided with an entire country
     */
    public Set<GeographicKeyword> extractAllGeographicInfo(LatitudeLongitudeBox latLong) {
        Set<GeographicKeyword> geoSet = new HashSet<GeographicKeyword>();
        if (!geoSearchDao.isEnabled()) {
            return geoSet;
        }
        geoSet.addAll(extractContientInfo(latLong));
        Set<GeographicKeyword> countries = extractCountryInfo(latLong);
        geoSet.addAll(countries);
        if (countries.size() < 3) {
            Set<GeographicKeyword> admin = extractAdminInfo(latLong);
            geoSet.addAll(admin);
            // if we're larger than this, then don't show county info
            if ((latLong.getArea() < 2) && (admin.size() < 3)) {
                geoSet.addAll(extractCountyInfo(latLong));
            }
        }
        logger.trace(geoSet.size() + " geographic terms being returned");
        return geoSet;
    }

    /*
     * just get country info (ISO Country Code + Name)
     */
    public Set<GeographicKeyword> extractCountryInfo(LatitudeLongitudeBox latLong) {
        Set<GeographicKeyword> geoSet = new HashSet<GeographicKeyword>();
        List<Map<String, Object>> countryResults = geoSearchDao.findAllCountriesMatching(latLong);
        for (Map<String, Object> result : countryResults) {
            if (result.containsKey(COL_COUNTRY_LONG_NAME)) {
                GeographicKeyword entityToFind = createGeoKeyword((String) result.get(COL_COUNTRY_LONG_NAME), Level.COUNTRY);
                if (entityToFind != null) {
                    geoSet.add(entityToFind);
                }
            }
            if (result.containsKey(COL_ISO_2DIGITS)) {
                GeographicKeyword entityToFind = createGeoKeyword((String) result.get(COL_ISO_2DIGITS), Level.ISO_COUNTRY);
                if (entityToFind != null) {
                    geoSet.add(entityToFind);
                }
            }
        }
        logger.trace(geoSet.size() + " geographic terms being returned from country");
        return geoSet;
    }

    /*
     * Finds and/or creates a geographic keyword from the level and label info
     */
    public GeographicKeyword createGeoKeyword(String label, Level level) {
        if (StringUtils.isEmpty(label) || (level == null)) {
            return null;
        }
        GeographicKeyword entityToFind = new GeographicKeyword();
        entityToFind.setLabel(GeographicKeyword.getFormattedLabel(label, level));
        entityToFind.setLevel(level);
        return entityToFind;
    }

    /*
     * get Admin/State keywords
     */
    public Set<GeographicKeyword> extractAdminInfo(LatitudeLongitudeBox latLong) {
        List<Map<String, Object>> findAllAdminMatching = geoSearchDao.findAllAdminMatching(latLong);
        return extractSingleColumnInfo(findAllAdminMatching, COL_ADMIN_NAME, Level.STATE);
    }

    /*
     * get country keywords
     */
    public Set<GeographicKeyword> extractCountyInfo(LatitudeLongitudeBox latLong) {
        List<Map<String, Object>> findAllCountiesMatching = geoSearchDao.findAllCountiesMatching(latLong);
        return extractSingleColumnInfo(findAllCountiesMatching, COL_COUNTY_NAME, Level.COUNTY);
    }

    /*
     * get continent name
     */
    public Set<GeographicKeyword> extractContientInfo(LatitudeLongitudeBox latLong) {
        List<Map<String, Object>> findAllContinentsMatching = geoSearchDao.findAllContinentsMatching(latLong);
        return extractSingleColumnInfo(findAllContinentsMatching, COL_CONTINENT_NAME, Level.CONTINENT);
    }

    /*
     * generic method for extracting keywords from a lat-long box. The query is generated by specifying a column, table, label and level
     */
    private Set<GeographicKeyword> extractSingleColumnInfo(List<Map<String, Object>> countryResults, String column, Level level) {
        Set<GeographicKeyword> geoSet = new HashSet<GeographicKeyword>();
        for (Map<String, Object> result : countryResults) {
            if (result.containsKey(column)) {
                GeographicKeyword entityToFind = createGeoKeyword(result.get(column).toString(), level);
                if (entityToFind != null) {
                    geoSet.add(entityToFind);
                }
            }
        }
        logger.trace(geoSet.size() + " geographic terms being returned from " + level.name());
        return geoSet;
    }

    public boolean isEnabled() {
        return geoSearchDao.isEnabled();
    }

    public SvgMapWrapper toSvg(double strokeWidth, String searchPrefix, String searchSuffix, SpatialTables table, String limit) {
        return geoSearchDao.getMapSvg(strokeWidth, searchPrefix, searchSuffix, table, limit);
    }
}
