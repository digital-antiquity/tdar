package org.tdar.search.query;

/**
 * These field name represent fields in one of the SOLR indexes. (see resources-schema.xml, for example)
 * 
 * @author abrin
 *
 */
public interface QueryFieldNames {

    String PROJECT_ID = "project.id";
    String RESOURCE_TYPE = "resourceType";
    String STATUS = "status";
    String ACTIVE_START_DATE = "activeCoverageDates.startDate";
    String ACTIVE_END_DATE = "activeCoverageDates.endDate";
    String DATE = "date";
    String DATE_UPDATED = "dateUpdated";
    String DATE_CREATED = "dateCreated";
    String DOT = ".";
    String IR = "informationResources.";
    String PROJECT_TITLE = "project.name";
    String PROJECT_TITLE_AUTOCOMPLETE = "project.name_autocomplete";
    String DOCUMENT_TYPE = "documentType";
    String INTEGRATABLE = "integratableOption";
    String SUBMITTER_ID = "submitter.id";
    String ACTIVE_SITE_TYPE_KEYWORDS = "activeSiteTypeKeywords";
    String ACTIVE_MATERIAL_KEYWORDS = "activeMaterialKeywords";
    String ACTIVE_CULTURE_KEYWORDS = "activeCultureKeywords";
    String ACTIVE_SITE_NAME_KEYWORDS = "activeSiteNameKeywords";
    String ACTIVE_INVESTIGATION_TYPES = "activeInvestigationTypes";
    String ACTIVE_GEOGRAPHIC_KEYWORDS = "activeGeographicKeywords";
    String ACTIVE_OTHER_KEYWORDS = "activeOtherKeywords";
    String ACTIVE_LATITUDE_LONGITUDE_BOXES = "activeLatitudeLongitudeBoxes";
    String RESOURCE_COLLECTION_NAME_AUTOCOMPLETE = "resourceCollections.name_autocomplete";
    String RESOURCE_COLLECTION_NAME = "resourceCollections.name";
    String RESOURCE_COLLECTION_NAME_PHRASE = "resourceCollections.name_phrase";
    String MAXX = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "maxx";
    String MAXY = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "maxy";
    String MINY = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "miny";
    String MINX = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "minx";
    String SCALE = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "scale";
    String MINXPRIME = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "minxPrime";
    String MAXXPRIME = ACTIVE_LATITUDE_LONGITUDE_BOXES + DOT + "maxxPrime";

    String ACTIVE_TEMPORAL_KEYWORDS = "activeTemporalKeywords";
    // String ACTIVE_SITE_TYPE_KEYWORDS_LABEL = ACTIVE_SITE_TYPE_KEYWORDS +
    // DOT_LABEL;
    // String IR_ACTIVE_CULTURE_KEYWORDS_LABEL = IR + ACTIVE_CULTURE_KEYWORDS +
    // DOT_LABEL;
    // String IR_ACTIVE_SITE_TYPE_KEYWORDS_LABEL = IR +
    // ACTIVE_SITE_TYPE_KEYWORDS + DOT_LABEL;
    String ACTIVE_COVERAGE_TYPE = "activeCoverageDates.dateType";
    String NAME = "name";
    String TITLE = NAME;
    String FIRST_NAME = "firstName";
    String LAST_NAME = "lastName";
    String EMAIL = "email";
    String ID = "id";
    String ALL = "all";
    String ALL_PHRASE = "all_phrase";
    String CONTENT = "content";
    String DESCRIPTION = "description";
    String PROJECT_TITLE_SORT = "project.name_sort";
    String LABEL_SORT = "label_sort";
    String FIRST_NAME_SORT = "firstName_sort";
    String LAST_NAME_SORT = "lastName_sort";
    String CREATOR_NAME_SORT = "creator_name_sort";

    String COLLECTION_TYPE = "collectionType";
    String RESOURCE_USERS_WHO_CAN_VIEW = "usersWhoCanView";
    String RESOURCE_USERS_WHO_CAN_MODIFY = "usersWhoCanModify";
    String COLLECTION_USERS_WHO_CAN_ADMINISTER = "usersWhoCanAdminister";
    String COLLECTION_USERS_WHO_CAN_VIEW = RESOURCE_USERS_WHO_CAN_VIEW;
    String COLLECTION_USERS_WHO_CAN_MODIFY = RESOURCE_USERS_WHO_CAN_MODIFY;
    // String RESOURCE_COLLECTION_PUBLIC_IDS = "publicCollectionIds";

    String RESOURCE_ACCESS_TYPE = "resourceAccessType";
    String PROPER_NAME = "properName";
    String RESOURCE_CREATORS_PROPER_NAME = "activeResourceCreators.creator." + PROPER_NAME;
    String RESOURCE_PROVIDER_ID = "RESOURCE_PROVIDER";
    String CATEGORY_ID = "categoryVariable.id";
    String CATEGORY_LABEL = "categoryVariable.label";
    String HIDDEN = "hidden";
    String TOP_LEVEL = "topLevel";
    String RESOURCE_OWNER = "resourceOwner";
    String DATE_CREATED_DECADE = "decadeCreated";
    String CREATOR_ROLE_IDENTIFIER = "crid";
    String IR_CREATOR_ROLE_IDENTIFIER = IR + CREATOR_ROLE_IDENTIFIER;
    String FILENAME = "filename";
    String DATA_VALUE_PAIR = "dataValuePair";
    String SITE_CODE = "activeSiteCodes";
    String TITLE_PHRASE = "name_phrase";
    String DESCRIPTION_PHRASE = "description_phrase";
    String COLLECTION_NAME_PHRASE = TITLE_PHRASE;
    String NAME_PHRASE = TITLE_PHRASE;
    String NAME_TOKEN = NAME;
    String USERNAME = "username";
    String PROPER_AUTO = "name_autocomplete";
    String COLLECTION_HIDDEN_WITH_RESOURCES = "visibleInSearch";

    // These are resources that are directly part of the collection, but not including collection IDs of the parent or grandparent, etc. collections. (I.e. they
    // are managed).
    String RESOURCE_COLLECTION_DIRECT_MANAGED_IDS = "directSharedCollectionIds";

    // all shared collections (current collection, plus all ancestors)
    String RESOURCE_COLLECTION_MANAGED_IDS = "sharedCollectionIds";

    // These are IDs of resources that are in the directly in the "List Collection" (i.e., unmanaged).
    String RESOURCE_COLLECTION_DIRECT_UNMANAGED_IDS = "directListCollectionIds";

    String RESOURCE_COLLECTION_UNMANAGED_IDS = "listCollectionIds";

    String LAST_NAME_AUTO = "lastName_autocomplete";
    String FIRST_NAME_AUTO = "firstName_autocomplete";
    String NAME_AUTOCOMPLETE = "name_autocomplete";
    String NAME_SORT = "name_sort";
    String GENERAL_TYPE = "type";
    String OBJECT_TYPE = "objectType";
    String OBJECT_TYPE_SORT = "objectTypeSort";
    String RESOURCE_TYPE_SORT = OBJECT_TYPE_SORT;
    String REGISTERED = "registered";
    String CONTIRBUTOR = "contributor";
    String INSTITUTION_NAME = "institution.name";
    String INSTITUION_ID = "institution.id";
    String LATITUDE_LONGITUDE_BOXES = "latitudeLongitudeBoxes";
    String CLASS = "_class";
    String CREATOR_ROLE = "activeResourceCreators.role";
    String BOOKMARKED_RESOURCE_PERSON_ID = "bookmarkedResource.person.id";
    String ACRONYM = "acronym";
    String DOCUMENT_SUB_TYPE = "subType";
    String DEGREE = "degree";
    String SERIES_NAME = "series.name";
    String BOOK_TITLE = "bookTitle";
    String JOURNAL_NAME = "journal.name";
    String ISSN = "issn";
    String ISBN = "isbn";
    String RESOURCE_LANGUAGE = "resourceLanguage";
    String METADATA_LANGUAGE = "metadataLanguage";
    String DOI = "doi";
    String RESOURCE_IDS = "resourceIds";
    String COLLECTION_PARENT = "parentId";
    String COLLECTION_PARENT_LIST = "parentIdList";
    String _ID = "_id";
    String VALUE = "value";
    String COLUMN_ID = "columnId";
    String VALUE_PHRASE = "value_phrase";
    String VALUE_EXACT = "value_exact";
    String RESOURCE_COLLECTION_IDS = "allCollectionIds";
    String FILE_IDS = "file.id";
    String ACTIVE_LATITUDE_LONGITUDE_BOXES_IDS = "activeLatitudeLongitudeBoxes.id";
    String RESOURCE_CREATOR_ROLE_IDS = "rcroleIds";
    String TOTAL_FILES = "totalFiles";
    String SCORE = "score";
    String RESOURCE_ID = "resource.id";
    String CORE = "_core";
    String ACTIVE_GEOGRAPHIC_ISO = "geographic.ISO";
    String SERIES_NUMBER = "series.number";
    String PUBLISHER_LOCATION = "publisher.location";
    String PUBLISHER_ID = "publisher.id";
    String COPY_LOCATION = "copy.location";
    String START_PAGE = "page.start";
    String END_PAGE = "page.end";
    String JOURNAL_NUMBER = "journal.number";
    String EFFECTIVELY_PUBLIC = "effectivelyPublic";
    String RESOURCE_ANNOTATION = "resourceAnnotationPair";
    String COLLECTION_USERS_WHO_CAN_REMOVE = "usersWhoCanRemove";
    String COLLECTION_USERS_WHO_CAN_ADD = "usersWhoCanAdd";
}
