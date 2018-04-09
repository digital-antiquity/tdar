package org.tdar.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum ProjectionModel {
    HIBERNATE_DEFAULT,
    LUCENE_ID_ONLY,
    RESOURCE_PROXY,
    LUCENE;

    private List<String> projections = new ArrayList<>();

    public List<String> getProjections() {
        if (this == ProjectionModel.LUCENE) {
            return Arrays.asList(QueryFieldNames.NAME, QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES_IDS,
                    QueryFieldNames.NAME_SORT, QueryFieldNames.SUBMITTER_ID, QueryFieldNames.PROJECT_TITLE, QueryFieldNames.PROJECT_ID,
                    QueryFieldNames.RESOURCE_COLLECTION_MANAGED_IDS, QueryFieldNames.RESOURCE_COLLECTION_UNMANAGED_IDS,
                    QueryFieldNames.DESCRIPTION, QueryFieldNames.DATE, QueryFieldNames.RESOURCE_CREATOR_ROLE_IDS,
                    QueryFieldNames.FILE_IDS, QueryFieldNames.STATUS, QueryFieldNames.RESOURCE_TYPE, QueryFieldNames.RESOURCE_ACCESS_TYPE);
        }
        return projections;
    }

    public void setProjections(List<String> projections) {
        this.projections = projections;
    }

    public static Collection<String> getDefaultProjections() {
        return Arrays.asList(QueryFieldNames._ID, QueryFieldNames.ID, QueryFieldNames.CLASS, QueryFieldNames.SCORE);
    }

}
