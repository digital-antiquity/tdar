package org.tdar;

import java.util.regex.Pattern;

public interface TestConstants {
    // public static final String DEFAULT_HOST = "localhost";
    /** id for a user that has admin rights **/
    // public static Long ADMIN_USER_ID = 8093L;
    // public static Long USER_ID = 8092L;
    /** id for project that has children **/
    public static Long PARENT_PROJECT_ID = 3805L;
    public static Long TEST_INSTITUTION_ID = 12088L;
    // public final static int DEFAULT_PORT = 8180;
    // public final static int DEFAULT_SECURE_PORT = 8143;
    public final static String TEST_ROOT_DIR = "target/test-resources/";//src/test/resources/";

    public static String FILESTORE_PATH = "target/generated/filestore/";

    public static final String TEST_BULK_DIR = TEST_ROOT_DIR + "bulk/";
    public static String TEST_SENSORY_DIR = TEST_ROOT_DIR + "sensory/";
    public static final String TEST_DOCUMENT_DIR = TEST_ROOT_DIR + "documents/";
    public static final String TEST_ARCHIVE_DIR = TEST_ROOT_DIR + "archive/";
    public static final String TEST_AUDIO_DIR = TEST_ROOT_DIR + "audio/";
    public static final String TEST_GIS_DIR = "gis/";
    public static final String TEST_SHAPEFILE_DIR = TEST_ROOT_DIR + TEST_GIS_DIR + "shapefiles/tijeras/";
    public static String TEST_GEOTIFF_DIR = TEST_ROOT_DIR + TEST_GIS_DIR + "geotiff/";
    public static String TEST_GEOTIFF_TFW = TEST_GEOTIFF_DIR + "Untitled.tfw";
    public static String TEST_GEOTIFF_COMBINED = TEST_GEOTIFF_DIR + "geo-combined-untitled.tif";
    public static String TEST_GEOTIFF = TEST_ROOT_DIR + TEST_GIS_DIR + "geotiff/Untitled.tif";
    public static final String TEST_ONTOLOGY_DIR = TEST_ROOT_DIR + "ontology/";
    public static final String TEST_IMAGE_DIR = TEST_ROOT_DIR + "images/";
    public static String TEST_XML_DIR = TEST_ROOT_DIR + "xml/";
    public static String TEST_SCHEMA_DIR = TEST_ROOT_DIR + "schemaCache/";
    public static String TEST_CODING_SHEET_DIR = TEST_ROOT_DIR + "coding sheet/";
    public static final String TEST_DATA_INTEGRATION_DIR = TEST_ROOT_DIR + "data_integration_tests/";

    public static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    public static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;

    // public static String USERNAME = "test@tdar.org";
    // public static String PASSWORD = "test";
    public static Long ADMIN_PROJECT_ID = 1L;
    public static Long PROJECT_ID = 2L;
    // public static String ADMIN_USERNAME = "admin@tdar.org";
    // public static String ADMIN_PASSWORD = "admin";
    public static Long ADMIN_INDEPENDENT_PROJECT_ID = 1L;
    public static final String TEST_DOCUMENT_NAME = "pia-09-lame-1980.pdf";
    public static final String TEST_DOCUMENT_NAME_SMALL = "pia-09-lame-1980-small.pdf";
    public static final String TEST_DOCUMENT = TEST_DOCUMENT_DIR + TEST_DOCUMENT_NAME;

    public static final String TEST_IMAGE_NAME = "5127663428_42ef7f4463_b.jpg";
    public static final String TEST_IMAGE = TEST_IMAGE_DIR + TEST_IMAGE_NAME;
    public static final String TEST_IMAGE_NAME2 = "handbook_of_archaeology.jpg";
    public static final String TEST_IMAGE2 = TEST_IMAGE_DIR + TEST_IMAGE_NAME2;

    public static String INSTITUTION_NAME = "Arizona State University";
    public static String DEFAULT_FIRST_NAME = "Test";
    public static String DEFAULT_LAST_NAME = "Person";
    public static String DEFAULT_EMAIL = "test@example.com";
    public static String DOCUENT_DATE_CREATED = "document.date";
    public static String TEST_DOCUMENT_ID = "4232";
    public static String TEST_KML = TEST_ROOT_DIR + TEST_GIS_DIR + "kml/doc.kml";

    public static String FAULTY_ARCHIVE = "broken.zip";
    public static String GOOD_ARCHIVE = "good.zip";
    public static String SPITAL_DB_NAME = "Spital Abone database.mdb";

    public static final String DOCUMENT_FIELD_TITLE = "document.title";
    public static final String DOCUMENT_FIELD_DESCRIPTION = "document.description";

    // public static final String COPYRIGHT_HOLDER_TYPE = "copyrightHolderType";
    public static final String COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME = "copyrightHolderProxies.institution.name";

    public static final Long NO_ASSOCIATED_PROJECT = -1L;

    // max number of screenshots per test (to protect against infinite loops killing our disk space)
    int MAX_SCREENSHOTS_PER_TEST = 100;
    // time conversion constant
    int MILLIS_PER_SECOND = 1000;
    // regex pattern for js error that typically occurs when rendering google maps in test environment
    public static final Pattern REGEX_GOOGLE_QUOTA_SERVICE_RECORD_EVENT = Pattern.compile(Pattern
            .quote("maps.googleapis.com/maps/api/js/QuotaService.RecordEvent"));
    // [[{"message":"errorEvent::(no error message)","tag":"<script async=\"\" src=\"//use.typekit.net/czp6njc.js\"></script>","time":"0.293s"},
    // {"message":"errorEvent::(no error message)","tag":"<script src=\"https://ssl.google-analytics.com/ga.js\" async=\"\" type=\"text/javascript\"></script>","time":"0.294s"}]]
    public static final Pattern REGEX_TYPEKIT = Pattern.compile("use.typekit.net");
    public static final Pattern REGEX_GOOGLE_ANALYTICS = Pattern.compile("ssl.google-analytics.com");
}
