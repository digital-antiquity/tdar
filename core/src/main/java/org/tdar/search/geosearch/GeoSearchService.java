/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.geosearch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.utils.PersistableUtils;

/**
 * @author Adam Brin
 * 
 */
@Service
public class GeoSearchService {

    private static final String GEO_JSON_FOLDER = "geoJson";

    @Autowired
    GeoSearchDao geoSearchDao;

    @Autowired
    DatasetDao datasetDao;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    // continent
    private static final String COL_CONTINENT_NAME = "continent";
    // country
    private static final String COL_COUNTRY_LONG_NAME = "long_name";

    // us_counties (county)
    private static final String COL_COUNTY_NAME = "cnty_name";

    private static final String COL_CODE = "tdar_code";

    private static final String COL_ADMIN_NAME = "admin_name";

    /*
     * extracts all geographic keywords from a lat-long-box, if however, the area of the box is deemed too large, it will not add county information. This is
     * beacuse you don't want to show county info if you're provided with an entire country
     */
    public Set<GeographicKeyword> extractAllGeographicInfo(LatitudeLongitudeBox latLong) {
        Set<GeographicKeyword> geoSet = new HashSet<GeographicKeyword>();
        if (!geoSearchDao.isEnabled()) {
            logger.debug("postgis not enabled");
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
        List<Map<String, Object>> countryResults = geoSearchDao.findAllCountriesMatching(latLong);
        Set<GeographicKeyword> geoSet = extractCols(countryResults, COL_COUNTRY_LONG_NAME, Level.COUNTRY);
        logger.trace(geoSet.size() + " geographic terms being returned from country");
        return geoSet;
    }

    private Set<GeographicKeyword> extractCols(List<Map<String, Object>> countryResults, String nameField, Level level) {
        Set<GeographicKeyword> geoSet = new HashSet<>();
        for (Map<String, Object> result : countryResults) {
            if (result.containsKey(nameField)) {
                GeographicKeyword entityToFind = createGeoKeyword((String) result.get(nameField), level, (String)result.get(COL_CODE));
                if (entityToFind != null) {
                    geoSet.add(entityToFind);
                }
            }
        }
        return geoSet;
    }

    /*
     * Finds and/or creates a geographic keyword from the level and label info
     */
    public GeographicKeyword createGeoKeyword(String label, Level level, String code) {
        if (StringUtils.isEmpty(label) || (level == null)) {
            return null;
        }
        GeographicKeyword entityToFind = new GeographicKeyword();
        entityToFind.setLabel(GeographicKeyword.getFormattedLabel(label, level));
        entityToFind.setLevel(level);
        entityToFind.setCode(code);
        return entityToFind;
    }

    /*
     * get Admin/State keywords
     */
    public Set<GeographicKeyword> extractAdminInfo(LatitudeLongitudeBox latLong) {
        List<Map<String, Object>> findAllAdminMatching = geoSearchDao.findAllAdminMatching(latLong);
        return extractCols(findAllAdminMatching, COL_ADMIN_NAME, Level.STATE);
    }

    /*
     * get country keywords
     */
    public Set<GeographicKeyword> extractCountyInfo(LatitudeLongitudeBox latLong) {
        List<Map<String, Object>> findAllCountiesMatching = geoSearchDao.findAllCountiesMatching(latLong);
        return extractCols(findAllCountiesMatching, COL_COUNTY_NAME, Level.COUNTY);
    }

    /*
     * get continent name
     */
    public Set<GeographicKeyword> extractContientInfo(LatitudeLongitudeBox latLong) {
        List<Map<String, Object>> findAllContinentsMatching = geoSearchDao.findAllContinentsMatching(latLong);
        return extractCols(findAllContinentsMatching, COL_CONTINENT_NAME, Level.CONTINENT);
    }


    public boolean isEnabled() {
        return geoSearchDao.isEnabled();
    }

    @Transactional(readOnly = false)
    public void processManagedGeographicKeywords(Resource resource, Collection<LatitudeLongitudeBox> allLatLongBoxes) {
        // needed in cases like the APIController where the collection is not properly initialized
        if (resource.getManagedGeographicKeywords() == null) {
            resource.setManagedGeographicKeywords(new LinkedHashSet<GeographicKeyword>());
        }

        Set<GeographicKeyword> kwds = new HashSet<GeographicKeyword>();
        for (LatitudeLongitudeBox latLong : allLatLongBoxes) {
            Set<GeographicKeyword> managedKeywords = extractAllGeographicInfo(latLong);
            logger.debug(resource.getId() + " :  " + managedKeywords + " " + managedKeywords.size());
            kwds.addAll(
                    datasetDao.findByExamples(GeographicKeyword.class, managedKeywords, Arrays.asList(Keyword.IGNORE_PROPERTIES_FOR_UNIQUENESS),
                            FindOptions.FIND_FIRST_OR_CREATE));
        }
        PersistableUtils.reconcileSet(resource.getManagedGeographicKeywords(), kwds);

    }

    @Transactional(readOnly = true)
    public String toGeoJson(GeographicKeyword kwd) {
        if (StringUtils.isBlank(kwd.getCode())) {
            return null;
        }

        if (kwd.getLevel() == null) {
            return null;
        }

        File path = new File(TdarConfiguration.getInstance().getTempDirectory(), GEO_JSON_FOLDER);
        if (!path.exists()) {
            path.mkdir();
        }
        File jsonFile = new File(path, String.format("%s.%s", kwd.getCode(), "json"));
        try {
            if (jsonFile.exists()) {
                return FileUtils.readFileToString(jsonFile);
            }
        } catch (IOException e) {
            logger.error("error reading jsonFile", e);
        }
        String json = geoSearchDao.toGeoJson(kwd);
        try {
            FileUtils.writeStringToFile(jsonFile, json);
        } catch (IOException e) {
            logger.error("error writing jsonFile", e);
        }
        return json;
    }

    public LatitudeLongitudeBox extractEnvelopeForCountries(List<String> countries) {
        return geoSearchDao.extractEnvelopeForCountry(countries);
    }

}
