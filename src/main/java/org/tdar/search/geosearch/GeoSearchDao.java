/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.geosearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

/**
 * This is a DAO to manage access to postGIS. It's attempted to manage this by hiding all of the PostGIS info at this layer
 * as opposed to the previous model that used a single combined, Dao and Service model
 * 
 * @author Adam Brin
 * 
 */
public class GeoSearchDao {

    private static boolean databaseEnabled;
    private SimpleJdbcTemplate jdbcTemplate;
    private final Logger logger = Logger.getLogger(getClass());
    public static final Integer DEFAULT_PROJECTION = 4326;

    /*
     * useful for visualizing WKTs
     * http://dev.openlayers.org/releases/OpenLayers-2.4/examples/wkt.html
     */

    /*
     * This query says, find all objects where the (area of the intersection of BoundingBox&Shape > 0) &&
     * (((area of the intersection of BoundingBox&Shape) / (area of shape)) > x percent or
     * ((area of the intersection of BoundingBox&Shape) / (area of bounding box) > y percent))
     * 
     * So, there needs to be some overlap between the two, and it needs to be a reasonable amount. Suggested parameters are
     * 30% for x and 80% for y. But, this needs more testing.
     */
    private final static String QUERY_COVERAGE_NEW = "SELECT %1$s, ST_Area(the_geom) as \"geom_area\", ST_Area(%3$s) as \"sect_area\", " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / ST_Area(%3$s) > %4$s as \"overlap\", " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / ST_area(the_geom) > %4$s as \"overlap_i\"  " +
            "FROM %2$s where (ST_Disjoint(the_geom,%3$s) is false) AND ST_Area(ST_Intersection(%3$s, the_geom)) > 0 AND ( " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / ST_Area(%3$s) > %4$s OR " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / ST_area(the_geom) > %5$s )";

    private final static String QUERY_GEOM_PART = "ST_GeomFromText('%1$s',%2$s)";

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

    /*
     * generic method for performing query
     */
    public List<Map<String, Object>> findAll(String sql) {
        logger.trace(sql);
        List<Map<String, Object>> queryForList = new ArrayList<Map<String, Object>>();
        if (isEnabled()) {
            try {
                queryForList = getJdbcTemplate().queryForList(sql, new HashMap<String, String>());
            } catch (EmptyResultDataAccessException e) {
                logger.trace("no results found for query");
            } catch (CannotGetJdbcConnectionException e) {
                logger.error("PostGIS connection is not configured");
                setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
                setEnabled(false);
            }
        }
        return queryForList;
    }

    /*
     * generic method for performing query
     */
    public Map<String, Object> findFirst(String sql) {
        Map<String, Object> queryForList = new HashMap<String, Object>();
        try {
            queryForList = getJdbcTemplate().queryForMap(sql, new HashMap<String, String>());
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings
    @Qualifier("tdarGeoDataSource")
    public void setDataSource(DataSource dataSource) {
        try {
            setEnabled(true);
            setJdbcTemplate(new SimpleJdbcTemplate(dataSource));
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
        }
    }

    private static void setEnabled(boolean enabled) {
        databaseEnabled = enabled;
    }

    public boolean isEnabled() {
        return databaseEnabled;
    }

    private String constructGeomQuery(String columns, String tableName, String polygon) {
        String coverage = String.format(QUERY_GEOM_PART, polygon, DEFAULT_PROJECTION);
        String toReturn = String.format(QUERY_COVERAGE_NEW, columns, tableName, coverage, ".3", ".8");
        logger.trace(toReturn);
        return toReturn;
    }

    public List<Map<String, Object>> findAllAdminMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(COL_ADMIN_NAME, TABLE_ADMIN, latLong.convertToPolygonBox());
        logger.trace(sql);
        return findAll(sql);
    }

    public List<Map<String, Object>> findAllContinentsMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(COL_CONTINENT_NAME, TABLE_CONTINENTS, latLong.convertToPolygonBox());
        logger.trace(sql);
        return findAll(sql);
    }

    public List<Map<String, Object>> findAllCountiesMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(COL_COUNTY_NAME, TABLE_COUNTIES, latLong.convertToPolygonBox());
        logger.trace(sql);
        return findAll(sql);
    }

    /**
     * @param convertToPolygonBox
     * @return
     */
    public List<Map<String, Object>> findAllCountriesMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(COLUMNS_COUNTRY, TABLE_COUNTRY, latLong.convertToPolygonBox());
        logger.trace(sql);
        return findAll(sql);
    }

    /*
     * reverse lookup to get a bounding box for a FIPS code or set of fips codes.
     */
    @Deprecated
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
        logger.trace(sql);
        Map<String, Object> fipsResults = findFirst(sql);
        if (fipsResults == null || fipsResults.get(POLYGON) == null)
            return null;
        PGgeometry poly = (PGgeometry) fipsResults.get(POLYGON);
        logger.trace(poly.getGeometry().toString());
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

    public void setJdbcTemplate(SimpleJdbcTemplate template) {
        this.jdbcTemplate = template;
    }

    public SimpleJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
