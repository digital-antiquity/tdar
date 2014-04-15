package org.tdar.odata.server;

public class Constant {

    static final String SERVICE_URL = "http://localhost:8888/test.svc/";
    // static final String SERVICE_URL = "http://localhost:8180/test.svc/";
    static final String META_DATA_URL = SERVICE_URL + "$metadata";
    static final String GRECIAN_URNS_FEED_URL = SERVICE_URL + "Grecian%20Urns";
    static final String GRECIAN_URNS_DATASET_NAME = "Grecian Urns";

    static final String TDATASETS_FEED_URL = SERVICE_URL + "TDataSets";
    static final String TDATATABLES_FEED_URL = SERVICE_URL + "TDataTables";

    static final String TEST_USER_NAME = "odata_test_user";
    static final String TEST_PASSWORD = "odata_test_password";

}
