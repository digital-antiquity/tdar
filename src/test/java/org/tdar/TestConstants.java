package org.tdar;

public interface TestConstants {
    /** id for a user that has admin rights **/
    public static Long ADMIN_USER_ID = 8093L;
    /** id for project that has children **/
    public static Long PARENT_PROJECT_ID = 3805L;
    public static Long TEST_INSTITUTION_ID = 12088L;
    public final static int DEFAULT_PORT = 8180;
    public final static String DEFAULT_BASE_URL = "http://localhost:" + DEFAULT_PORT;

    public final static String TEST_ROOT_DIR = "src/test/resources/";

    public static String FILESTORE_PATH = "target/generated/filestore/";

    public static final String TEST_BULK_DIR = TEST_ROOT_DIR + "bulk/";
    public static String TEST_SENSORY_DIR = TEST_ROOT_DIR + "sensory/";
    public static final String TEST_DOCUMENT_DIR = TEST_ROOT_DIR + "documents/";
    public static final String TEST_ONTOLOGY_DIR = TEST_ROOT_DIR + "ontology/";
    public static final String TEST_IMAGE_DIR = TEST_ROOT_DIR + "images/";
    public static String TEST_XML_DIR = TEST_ROOT_DIR + "xml/";
    public static String TEST_CODING_SHEET_DIR = TEST_ROOT_DIR + "coding sheet/";
    public static final String TEST_DATA_INTEGRATION_DIR = TEST_ROOT_DIR + "data_integration_tests/";

    // public static final String EVMPP_FAUNA = "evmpp-fauna.xls";
    public static Long USER_ID = 8092L;
    public static String USERNAME = "test@tdar.org";
    public static String PASSWORD = "test";
    public static int ADMIN_PROJECT_ID = 1;
    public static int PROJECT_ID = 2;
    public static String ADMIN_USERNAME = "admin@tdar.org";
    public static String ADMIN_PASSWORD = "admin";
    public static Long ADMIN_INDEPENDENT_PROJECT_ID = 1L;
    public static final String TEST_DOCUMENT_NAME = "pia-09-lame-1980.pdf";
    public static final String TEST_DOCUMENT = TEST_DOCUMENT_DIR + TEST_DOCUMENT_NAME;

    public static final String TEST_IMAGE_NAME = "5127663428_42ef7f4463_b.jpg";
    public static final String TEST_IMAGE = TEST_IMAGE_DIR + TEST_IMAGE_NAME;

    public static String INSTITUTION_NAME = "Arizona State University";
    public static String DEFAULT_FIRST_NAME = "Test";
    public static String DEFAULT_LAST_NAME = "Person";
    public static String DEFAULT_EMAIL = "test@example.com";

    public static final String DOCUMENT_FIELD_TITLE = "document.title";
    public static final String DOCUMENT_FIELD_DESCRIPTION = "resource.description";

}
