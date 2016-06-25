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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.utils.MessageHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This is a DAO to manage access to postGIS. It's attempted to manage this by hiding all of the PostGIS info at this layer
 * as opposed to the previous model that used a single combined, Dao and Service model
 * 
 * @author Adam Brin
 * 
 */
@Component
public class GeoSearchDao {

    private static boolean databaseEnabled;
    private JdbcTemplate jdbcTemplate;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    public static final Integer DEFAULT_PROJECTION = 4326;

    private static final String PSQL_POLYGON = "POLYGON((%1$s %2$s,%3$s %2$s,%3$s %4$s,%1$s %4$s,%1$s %2$s))";

    private static final String PSQL_MULTIPOLYGON_DATELINE = "MULTIPOLYGON(((%1$s %2$s,%1$s %3$s,  180 %3$s,  180 %2$s,%1$s %2$s)), ((-180 %3$s, %4$s %3$s,%4$s %2$s,-180 %2$s,-180 %3$s)))";

    // private static final String PSQL_MULTIPOLYGON_DATELINE =
    // "MULTIPOLYGON(((%1$s %2$s,%1$s %3$s, -180 %3$s, -180 %2$s,%1$s %2$s)), (( 180 %3$s, %4$s %3$s,%4$s %2$s, 180 %2$s, 180 %3$s)))";
    public String convertToPolygonBox(LatitudeLongitudeBox latLong) {
        // if we've got something that goes over the dateline, then we need to split
        // into a multipolygon instead of a standard one. The multipolygon is two polygons
        // each one being on either side of the dateline
        if (!latLong.isValid()) {
            throw new TdarRuntimeException(MessageHelper.getMessage("geoSearchService.lat_long_Not_valid"));
        }
        if (latLong.crossesDateline()) {
            return String.format(PSQL_MULTIPOLYGON_DATELINE, latLong.getObfuscatedWest(), latLong.getObfuscatedSouth(),
                    latLong.getObfuscatedNorth(), latLong.getObfuscatedEast()).toString();
        }
        return String.format(PSQL_POLYGON, latLong.getObfuscatedEast(), latLong.getObfuscatedNorth(),
                latLong.getObfuscatedWest(), latLong.getObfuscatedSouth()).toString();
    }

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
    private final static String QUERY_COVERAGE_NEW = "SELECT %1$s, area as \"geom_area\", ST_Area(%3$s) as \"sect_area\", " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / ST_Area(%3$s) > %4$s as \"overlap\", " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / area > %4$s as \"overlap_i\"  " +
            "FROM %2$s where (ST_Disjoint(the_geom,%3$s) is false) AND ST_Area(ST_Intersection(%3$s, the_geom)) > 0 AND ( " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / ST_Area(%3$s) > %4$s OR " +
            "ST_Area(ST_Intersection(%3$s,the_geom)) / area > %5$s )";

    private final static String QUERY_GEOM_PART = "ST_GeomFromText('%1$s',%2$s)";

    /*
     * Sample RAW Query for reference select * from "Country_WGS84" where
     * st_covers(the_geom, ST_GeomFromText('POLYGON(( -70.1053047180176
     * 42.0591071009751,-70.1164627075195 42.0591071009751, -70.1164627075195
     * 42.0519694606272,-70.1053047180176 42.0519694606272, -70.1053047180176
     * 42.0591071009751))',4326));
     */

    private final static String QUERY_ENVELOPE = "SELECT ST_Envelope(ST_Collect(the_geom)) as %2$s FROM %1$s where %3$s";

    private final static String QUERY_ENVELOPE_2 = "(%1$s='%2$s') ";
    private final static String POLYGON = "polygon";
    // , concat('${',%1$s,'-style?default('''')}') as style

    private static final String COL_FIPS = "fips";

    private static final int FIPS_ALL_COUNTIES_SUFFIX = 999;
    private static final int FIPS_UNKNOWN_COUNTIES_SUFFIX = 998;

    /*
     * generic method for performing query
     */
    public List<Map<String, Object>> findAll(String sql, Object... params) {
        logger.trace(sql);
        List<Map<String, Object>> queryForList = new ArrayList<Map<String, Object>>();
        if (isEnabled()) {
            try {
                queryForList = getJdbcTemplate().queryForList(sql, params);
            } catch (EmptyResultDataAccessException e) {
                logger.trace("no results found for query");
            } catch (CannotGetJdbcConnectionException e) {
                logger.error("PostGIS connection is not configured");
                setEnabled(false);
            } catch (Exception e) {
                logger.debug("exception in geosearch:", e);
                setEnabled(false);
            }
        }
        return queryForList;
    }

    /*
     * generic method for performing query
     */
    public Map<String, Object> findFirst(String sql, Object... params) {
        Map<String, Object> queryForList = new HashMap<String, Object>();
        try {
            queryForList = getJdbcTemplate().queryForMap(sql, params);
        } catch (EmptyResultDataAccessException e) {
            logger.trace("no results found for query");
        } catch (CannotGetJdbcConnectionException e) {
            logger.error("gis database connection not enabled");
            GeoSearchDao.setEnabled(false);
        } catch (Exception e) {
            logger.debug("exception in geosearch:", e);
            GeoSearchDao.setEnabled(false);
        }
        return queryForList;
    }

    @SuppressFBWarnings
    @Autowired(required = false)
    @Lazy(true)
    public void setDataSource(@Qualifier("tdarGeoDataSource") DataSource dataSource) {
        try {
            setEnabled(true);
            setJdbcTemplate(new JdbcTemplate(dataSource));
        } catch (Exception e) {
            logger.debug("exception in geosearch:", e);
            setEnabled(false);
        }
    }

    private static void setEnabled(boolean enabled) {
        databaseEnabled = enabled;
    }

    public boolean isEnabled() {
        return databaseEnabled;
    }

    private String constructGeomQuery(SpatialTables table, String polygon) {
        String coverage = String.format(QUERY_GEOM_PART, polygon, DEFAULT_PROJECTION);
        String toReturn = String.format(QUERY_COVERAGE_NEW, table.getPrimaryColumn(), table.getTableName(), coverage, ".3", ".8");
        logger.trace(toReturn);
        return toReturn;
    }

    public List<Map<String, Object>> findAllAdminMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(SpatialTables.ADMIN, convertToPolygonBox(latLong));
        logger.trace(sql);
        return findAll(sql);
    }

    public List<Map<String, Object>> findAllContinentsMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(SpatialTables.CONTINENT, convertToPolygonBox(latLong));
        logger.trace(sql);
        return findAll(sql);
    }

    public List<Map<String, Object>> findAllCountiesMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(SpatialTables.COUNTY, convertToPolygonBox(latLong));
        logger.trace(sql);
        return findAll(sql);
    }

    /**
     * @param convertToPolygonBox
     * @return
     */
    public List<Map<String, Object>> findAllCountriesMatching(LatitudeLongitudeBox latLong) {
        String sql = constructGeomQuery(SpatialTables.COUNTRY, convertToPolygonBox(latLong));
        logger.trace(sql);
        return findAll(sql);
    }

    /*
     * reverse lookup to get a bounding box for a FIPS code or set of fips codes.
     */
    public LatitudeLongitudeBox extractLatLongFromFipsCode(String... fipsCodes) {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        StringBuffer suffix = new StringBuffer();
        if (ArrayUtils.isEmpty(fipsCodes)) {
            return null;
        }

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
            if ((i + 1) < fipsCodes.length) {
                suffix.append(" OR ");
            }
        }

        String sql = String.format(QUERY_ENVELOPE, SpatialTables.COUNTY.getTableName(), POLYGON, suffix.toString());
        logger.trace(sql);
        Map<String, Object> fipsResults = findFirst(sql);
        if ((fipsResults == null) || (fipsResults.get(POLYGON) == null)) {
            return null;
        }
        PGgeometry poly = (PGgeometry) fipsResults.get(POLYGON);
        logger.trace(poly.getGeometry().toString());
        Point firstPoint = poly.getGeometry().getPoint(0);
        Point thirdPoint = poly.getGeometry().getPoint(2);
        logger.trace(firstPoint + " " + firstPoint.getX());
        // NOTE: ASSUMES THAT BELOW IS result of an envelope
        latLong.setSouth(firstPoint.getY());
        latLong.setWest(firstPoint.getX());
        latLong.setNorth(thirdPoint.getY());
        latLong.setEast(thirdPoint.getX());
        logger.trace(latLong.toString());

        return latLong;
    }

    public void setJdbcTemplate(JdbcTemplate template) {
        this.jdbcTemplate = template;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public SpatialTables getTableFromLevel(Level level) {
        switch (level) {
            case CONTINENT:
                return SpatialTables.CONTINENT;
            case COUNTRY:
                return SpatialTables.COUNTRY;
            case COUNTY:
                return SpatialTables.COUNTY;
            case STATE:
                return SpatialTables.ADMIN;
            default:
                return null;
        }
    }

    public String toGeoJson(GeographicKeyword kwd) {
        String sql = null;
        try {
            SpatialTables table = getTableFromLevel(kwd.getLevel());
            sql = String.format("select ST_asGeoJson(the_geom) from %s where %s='%s'", table.getTableName(), table.getElementName(),
                    StringUtils.substringBeforeLast(kwd.getLabel(), "("));
            return jdbcTemplate.queryForObject(sql, String.class);
        } catch (Exception e) {
            logger.warn(sql);
            logger.warn("exception in getting json", e);
        }
        return null;
    }

}
