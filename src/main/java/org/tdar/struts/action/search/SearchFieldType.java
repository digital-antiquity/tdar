package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.HasLabel;

public enum SearchFieldType implements HasLabel {

    // basic fields
    ALL_FIELDS("allFields", SearchFieldGroup.BASIC_FIELDS, "All Fields"),
    TITLE("titles", SearchFieldGroup.BASIC_FIELDS, "Title"),
    CONTENTS("contents", SearchFieldGroup.BASIC_FIELDS, "Full-Text"),
    FILENAME("filenames", SearchFieldGroup.BASIC_FIELDS, "File Name"),
    RESOURCE_CREATOR_PERSON("resourceCreatorProxies", SearchFieldGroup.BASIC_FIELDS, "Person", false),
    RESOURCE_CREATOR_INSTITUTION("resourceCreatorProxies", SearchFieldGroup.BASIC_FIELDS, "Institution", false),
    TDAR_ID("resourceIds", SearchFieldGroup.BASIC_FIELDS, "Id"),
    COVERAGE_DATE_CALENDAR("coverageDates", SearchFieldGroup.BASIC_FIELDS, "Calendar Dates", false),
    COVERAGE_DATE_RADIOCARBON("coverageDates", SearchFieldGroup.BASIC_FIELDS, "RadioCarbon Dates", false),
    PROJECT("projects", SearchFieldGroup.BASIC_FIELDS, "Project", false),
    COLLECTION("collections", SearchFieldGroup.BASIC_FIELDS, "Collection", false),

    // freeform keywords
    FFK_GEOGRAPHIC("geographicKeywords", SearchFieldGroup.FREEFORM_KEYWORDS, "Geographic Keywords"),
    FFK_SITE("siteNames", SearchFieldGroup.FREEFORM_KEYWORDS, "Site Names"),
    FFK_SITE_TYPE("uncontrolledSiteTypes", SearchFieldGroup.FREEFORM_KEYWORDS, "Site Type"),
    FFK_CULTURAL("uncontrolledCultureKeywords", SearchFieldGroup.FREEFORM_KEYWORDS, "Culture Keywords"),
    FFK_TEMPORAL("temporalKeywords", SearchFieldGroup.FREEFORM_KEYWORDS, "Temporal Keywords"),
    FFK_GENERAL("otherKeywords", SearchFieldGroup.FREEFORM_KEYWORDS, "General Keywords"),

    // managed keywords
    KEYWORD_INVESTIGATION("investigationTypeIdLists", SearchFieldGroup.CONTROLLED_KEYWORDS, "Investigation Types", false),
    KEYWORD_SITE("approvedSiteTypeIdLists", SearchFieldGroup.CONTROLLED_KEYWORDS, "Site Type(Controlled)", false),
    KEYWORD_MATERIAL("materialKeywordIdLists", SearchFieldGroup.CONTROLLED_KEYWORDS, "Material Types", false),
    KEYWORD_CULTURAL("approvedCultureKeywordIdLists", SearchFieldGroup.CONTROLLED_KEYWORDS, "Culture Keywords", false),

    // TODO: add these
    CREATION_DECADE("creationDecades", SearchFieldGroup.EXPLORE, "Creation Decade", false),

    DATE_CREATED("createdDates", SearchFieldGroup.BASIC_FIELDS, "Year", false),
    DATE_REGISTERED("registeredDates", SearchFieldGroup.BASIC_FIELDS, "Date Registered", false),
    DATE_UPDATED("updatedDates", SearchFieldGroup.BASIC_FIELDS, "Date Updated", false);

    private String label = "";
    private SearchFieldGroup fieldGroup;
    private String fieldName = "";
    private boolean simple = true;

    private SearchFieldType() {
        this.fieldGroup = SearchFieldGroup.BASIC_FIELDS;
    }

    private SearchFieldType(String fieldName, SearchFieldGroup fieldGroup, String label, boolean simple) {
        this.label = label;
        this.fieldGroup = fieldGroup;
        this.fieldName = fieldName;
        this.simple = simple;
    }

    private SearchFieldType(String fieldName, SearchFieldGroup fieldGroup, String label) {
        this.label = label;
        this.fieldGroup = fieldGroup;
        this.fieldName = fieldName;
    }

    public static List<SearchFieldType> getSearchFieldTypesByGroup() {
        List<SearchFieldType> types = new ArrayList<SearchFieldType>();
        for (SearchFieldGroup group : SearchFieldGroup.values()) {
            for (SearchFieldType type : SearchFieldType.values()) {
                if (type.getFieldGroup() == group) {
                    types.add(type);
                }
            }
        }
        return types;
    }

    public String getLabel() {
        return this.label;
    }

    public SearchFieldGroup getFieldGroup() {
        return this.fieldGroup;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public boolean isSimple() {
        return simple;
    }

    public boolean isHidden() {
        if (this.fieldGroup == SearchFieldGroup.EXPLORE) {
            return true;
        }
        return false;
    }

}
