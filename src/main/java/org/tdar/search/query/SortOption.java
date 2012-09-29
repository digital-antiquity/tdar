/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.SortField;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Resource;

public enum SortOption {
    RELEVANCE(null, "Relevance", null, SortField.SCORE,false ),
    ID(null, "ID", QueryFieldNames.ID, SortField.INT, false),
    ID_REVERSE(null, "ID (Reversed)", QueryFieldNames.ID, SortField.INT, true),
    TITLE(Resource.class, "Title", QueryFieldNames.TITLE_SORT),
    TITLE_REVERSE(Resource.class, "Title (Z-A)", QueryFieldNames.TITLE_SORT, true),
    PROJECT(Resource.class, "Project", QueryFieldNames.PROJECT_TITLE_SORT),
    DATE(Resource.class, "Date", QueryFieldNames.DATE_CREATED, SortField.INT, false),
    DATE_REVERSE(Resource.class, "Date (Reversed)", QueryFieldNames.DATE_CREATED, SortField.INT, true),
    RESOURCE_TYPE(Resource.class, "Resource Type", QueryFieldNames.RESOURCE_TYPE),
    RESOURCE_TYPE_REVERSE(Resource.class, "Resource Type (Z-A)", QueryFieldNames.RESOURCE_TYPE, true),
    LABEL(Keyword.class, "Label", QueryFieldNames.LABEL_SORT), 
    LABEL_REVERSE(Keyword.class, "Label", QueryFieldNames.LABEL_SORT, true), 
    CREATOR_NAME(Creator.class, "Name", QueryFieldNames.CREATOR_NAME_SORT),               
    CREATOR_NAME_REVERSE(Creator.class, "Name", QueryFieldNames.CREATOR_NAME_SORT, true),
    FIRST_NAME(Person.class, "First Name", QueryFieldNames.FIRST_NAME_SORT),
    FIRST_NAME_REVERSE(Person.class, "First Name", QueryFieldNames.FIRST_NAME_SORT, true),
    LAST_NAME(Person.class, "Last Name", QueryFieldNames.LAST_NAME_SORT),
    LAST_NAME_REVERSE(Person.class, "Last Name", QueryFieldNames.LAST_NAME_SORT, true);

    private String label;
    private String sortField;
    private int luceneSortType;
    private boolean reversed;
    private Class<? extends Indexable> context;

    private SortOption(Class<? extends Indexable> context, String label, String sortField, boolean reversed) {
        this(context, label, sortField, SortField.STRING, reversed);
    }

    private SortOption(Class<? extends Indexable> context, String label, String sortField, int sortType, boolean reversed) {
        this.setLabel(label);
        this.setSortField(sortField);
        this.setReversed(reversed);
        this.setLuceneSortType(sortType);
        this.setContext(context);
    }

    public static List<SortOption> getOptionsForContext(Class<? extends Indexable> cls) {
        List<SortOption> toReturn = new ArrayList<SortOption>();
        for (SortOption option : SortOption.values()) {
            if (option.getContext() == null || cls.isAssignableFrom(option.getContext())) {
                toReturn.add(option);
            }
        }
        return toReturn;
    }

    private SortOption(Class<? extends Indexable> context, String label, String sortField) {
        this(context, label, sortField, false);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @param sortField
     *            the sortField to set
     */
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    /**
     * @return the sortField
     */
    public String getSortField() {
        return sortField;
    }

    /**
     * @param reversed
     *            the reversed to set
     */
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    /**
     * @return the reversed
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * @param luceneSortType
     *            the luceneSortType to set
     */
    public void setLuceneSortType(int luceneSortType) {
        this.luceneSortType = luceneSortType;
    }

    /**
     * @return the luceneSortType
     */
    public int getLuceneSortType() {
        return luceneSortType;
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(Class<? extends Indexable> context) {
        this.context = context;
    }

    /**
     * @return the context
     */
    public Class<? extends Indexable> getContext() {
        return context;
    }
}