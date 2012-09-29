/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.geosearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;

/**
 * @author Adam Brin
 * 
 */
public class GeoSearchService {

    private SimpleJdbcTemplate jdbcTemplate;

    private static boolean databaseEnabled = true;
    private final Logger logger = Logger.getLogger(getClass());
    private static final Integer DEFAULT_PROJECTION = 4326;

    // table definitions
    private static final String TABLE_COUNTRY = "country_wgs84";
    private static final String TABLE_ADMIN = "admin1_wgs84";
    private static final String TABLE_CONTINENTS = "continents_wgs84";
    private static final String TABLE_COUNTIES = "us_counties_wgs84";
    // private final String TOWNSHIP_TABLE = "TownRange2_WGS84";

    // continent
    private static final String COL_CONTINENT_NAME = "continent";

    // country
    private static final String COL_COUNTRY_LONG_NAME = "long_name";
    private static final String COL_ISO_2DIGITS = "iso_2digit";
    private static final String COLUMNS_COUNTRY = COL_ISO_2DIGITS + ", " + COL_COUNTRY_LONG_NAME;

    // admin (state)
    private static final String COL_ADMIN_NAME = "admin_name";
    private static final String COL_ADMIN_TYPE = "type_eng";
    @SuppressWarnings("unused")
    private static final String COLUMNS_ADMIN = COL_ADMIN_NAME + ", " + COL_ADMIN_TYPE;

    // us_counties (county)
    private static final String COL_COUNTY_NAME = "cnty_name";
    private static final String COL_STATE_NAME = "state_name";
    @SuppressWarnings("unused")
    private static final String COLUMNS_COUNTIES = COL_COUNTY_NAME + ", " + COL_STATE_NAME;
    private static final String COL_FIPS = "fips";

    private static final int FIPS_ALL_COUNTIES_SUFFIX = 999;
    private static final int FIPS_UNKNOWN_COUNTIES_SUFFIX = 998;

    // doc on query type: http://www.postgis.org/docs/ST_Intersects.html
    private final static String QUERY_COVERAGE = "select %1$s from \"%2$s\" where ST_Intersects(the_geom,ST_GeomFromText('%3$s',%4$s))";

    /*
     * Sample RAW Query for reference select * from "Country_WGS84" where
     * st_covers(the_geom, ST_GeomFromText('POLYGON(( -70.1053047180176
     * 42.0591071009751,-70.1164627075195 42.0591071009751, -70.1164627075195
     * 42.0519694606272,-70.1053047180176 42.0519694606272, -70.1053047180176
     * 42.0591071009751))',4326));
     */

    private final static String QUERY_ENVELOPE = "SELECT ST_Envelope(ST_Collect(the_geom)) as %2$s FROM \"%1$s\" where %3$s";
    private final static String QUERY_ENVELOPE_2 = "(%1$s='%2$s') ";
    private final static String POLYGON = "polygon";

    private String constructGeomQuery(String columns, String tableName, String polygon) {
        return String.format(QUERY_COVERAGE, columns, tableName, polygon, DEFAULT_PROJECTION);
    }

    /*
     * extracts all geographic keywords from a lat-long-box, if however, the area of the box is deemed too large, it will not add county information. This is
     * beacuse you don't want to show county info if you're provided with an entire country
     */
    public Set<GeographicKeyword> extractAllGeographicInfo(LatitudeLongitudeBox latLong) {
        Set<GeographicKeyword> geoSet = new HashSet<GeographicKeyword>();
        if (!isEnabled()) {
            return geoSet;
        }
        geoSet.addAll(extractContientInfo(latLong));
        Set<GeographicKeyword> countries = extractCountryInfo(latLong);
        geoSet.addAll(countries);
        if (countries.size() < 3) {
            Set<GeographicKeyword> admin = extractAdminInfo(latLong);
            geoSet.addAll(admin);
            // if we're larger than this, then don't show county info
            if (latLong.getArea() < 2 && admin.size() < 3) {
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
        String sql = constructGeomQuery(COLUMNS_COUNTRY, TABLE_COUNTRY, latLong.convertToPolygonBox());
        logger.trace(sql);
        List<Map<String, Object>> countryResults = findAll(sql);
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
        if (StringUtils.isEmpty(label) || level == null)
            return null;
        GeographicKeyword entityToFind = new GeographicKeyword();
        entityToFind.setLabel(GeographicKeyword.getFormattedLabel(label, level));
        entityToFind.setLevel(level);
        return entityToFind;
    }

    /*
     * get Admin/State keywords
     */
    public Set<GeographicKeyword> extractAdminInfo(LatitudeLongitudeBox latLong) {
        return extractSingleColumnInfo(latLong, COL_ADMIN_NAME, TABLE_ADMIN, "State", Level.STATE);
    }

    /*
     * get country keywords
     */
    public Set<GeographicKeyword> extractCountyInfo(LatitudeLongitudeBox latLong) {
        return extractSingleColumnInfo(latLong, COL_COUNTY_NAME, TABLE_COUNTIES, "County", Level.COUNTY);
    }

    /*
     * get continent name
     */
    public Set<GeographicKeyword> extractContientInfo(LatitudeLongitudeBox latLong) {
        return extractSingleColumnInfo(latLong, COL_CONTINENT_NAME, TABLE_CONTINENTS, "", Level.CONTINENT);
    }

    /*
     * generic method for extracting keywords from a lat-long box. The query is generated by specifying a column, table, label and level
     */
    private Set<GeographicKeyword> extractSingleColumnInfo(LatitudeLongitudeBox latLong, String column, String table, String label, Level level) {
        Set<GeographicKeyword> geoSet = new HashSet<GeographicKeyword>();
        String sql = constructGeomQuery(column, table, latLong.convertToPolygonBox());
        logger.trace(sql);
        List<Map<String, Object>> countryResults = findAll(sql);
        for (Map<String, Object> result : countryResults) {
            if (result.containsKey(column)) {
                if (!StringUtils.isEmpty(label)) {
                    GeographicKeyword entityToFind = createGeoKeyword(result.get(column).toString(), level);
                    if (entityToFind != null) {
                        geoSet.add(entityToFind);
                    }
                }
            }
        }
        logger.trace(geoSet.size() + " geographic terms being returned from " + label);
        return geoSet;
    }

    /*
     * generic method for performing query
     */
    private List<Map<String, Object>> findAll(String sql) {
        logger.debug(sql);
        List<Map<String, Object>> queryForList = new ArrayList<Map<String, Object>>();
        try {
            queryForList = jdbcTemplate.queryForList(sql, new HashMap<String, String>());
        } catch (EmptyResultDataAccessException e) {
            logger.trace("no results found for query");
        } catch (CannotGetJdbcConnectionException e) {
            logger.error("PostGIS connection is not configured");
            setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
        }
        return queryForList;
    }

    /*
     * generic method for performing query
     */
    private Map<String, Object> findFirst(String sql) {
        Map<String, Object> queryForList = new HashMap<String, Object>();
        try {
            queryForList = jdbcTemplate.queryForMap(sql, new HashMap<String, String>());
        } catch (EmptyResultDataAccessException e) {
            logger.trace("no results found for query");
        } catch (CannotGetJdbcConnectionException e) {
            logger.error("gis database connection not enabled");
            databaseEnabled = false;
        } catch (Exception e) {
            e.printStackTrace();
            databaseEnabled = false;
        }
        return queryForList;
    }

    /*
     * reverse lookup to get a bounding box for a FIPS code or set of fips codes.
     */
    public LatitudeLongitudeBox extractLatLongFromFipsCode(String... fipsCodes) {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        StringBuffer suffix = new StringBuffer();
        if (ArrayUtils.isEmpty(fipsCodes))
            return null;

        for (int i = 0; i < fipsCodes.length; i++) {
            try {
                Integer.valueOf(fipsCodes[i]);
            } catch (Exception e) {
                logger.debug("could not parse fips code: " + fipsCodes[i], e);
                return null;
            }

            String col = COL_FIPS;
            if (fipsCodes[i].endsWith(Integer.toString(FIPS_ALL_COUNTIES_SUFFIX)) || fipsCodes[i].endsWith(Integer.toString(FIPS_UNKNOWN_COUNTIES_SUFFIX))) {
                fipsCodes[i] = fipsCodes[i].substring(0, 2);
                col = " substring(" + COL_FIPS + " from 1 for 2) ";
                logger.trace("using broader matching for FIPS codes:" + fipsCodes[i]);
                // this is a small issue because we cross the dateline. If we don't do this, we end up with an envelope that includes
                // mongolia, china, etc.
                if (fipsCodes[i].equals("02")) {
                    suffix.append(" cnty_name not like '%Aleutian%' and ");
                }
            }
            suffix.append(String.format(QUERY_ENVELOPE_2, col, fipsCodes[i]));
            if (i + 1 < fipsCodes.length)
                suffix.append(" OR ");
        }

        String sql = String.format(QUERY_ENVELOPE, TABLE_COUNTIES, POLYGON, suffix.toString());
        logger.debug(sql);
        Map<String, Object> fipsResults = findFirst(sql);
        if (fipsResults == null || fipsResults.get(POLYGON) == null)
            return null;
        PGgeometry poly = (PGgeometry) fipsResults.get(POLYGON);
        logger.info(poly.getGeometry().toString());
        Point firstPoint = poly.getGeometry().getPoint(0);
        Point thirdPoint = poly.getGeometry().getPoint(2);
        logger.trace(firstPoint + " " + firstPoint.getX());
        // NOTE: ASSUMES THAT BELOW IS result of an envelope
        latLong.setMinimumLatitude(firstPoint.getY());
        latLong.setMinimumLongitude(firstPoint.getX());
        latLong.setMaximumLatitude(thirdPoint.getY());
        latLong.setMaximumLongitude(thirdPoint.getX());
        logger.trace(latLong.toString());

        return latLong;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings
    @Qualifier("tdarGeoDataSource")
    public void setDataSource(DataSource dataSource) {
        try {
            setEnabled(true);
            this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
        }
    }

    private static void setEnabled(boolean enabled) {
        GeoSearchService.databaseEnabled = enabled;
    }

    public boolean isEnabled() {
        return databaseEnabled;
    }
}
