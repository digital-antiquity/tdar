/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Resource;

public enum SortOption {
    RELEVANCE(null, "Relevance", false),
    ID(null, "ID",false),
    ID_REVERSE(null, "ID (Most Recent)", true),
    COLLECTION_TITLE(ResourceCollection.class, "Title"),
    COLLECTION_TITLE_REVERSE(ResourceCollection.class, "Title (Z-A)", true),
    TITLE(Resource.class, "Title"),
    TITLE_REVERSE(Resource.class, "Title (Z-A)", true),
    PROJECT(Resource.class, "Project" ),
    DATE(Resource.class, "Date", false),
    DATE_REVERSE(Resource.class, "Date (Most Recent)", true),
    DATE_UPDATED(Resource.class, "Date Updated", false),
    DATE_UPDATED_REVERSE(Resource.class, "Date Updated (Most Recent)", true),
    RESOURCE_TYPE(Resource.class, "Resource Type"),
    RESOURCE_TYPE_REVERSE(Resource.class, "Resource Type (Z-A)", true),
    LABEL(Keyword.class, "Label"),
    LABEL_REVERSE(Keyword.class, "Label",true),
    CREATOR_NAME(Creator.class, "Name"),
    CREATOR_NAME_REVERSE(Creator.class, "Name", true),
    FIRST_NAME(Person.class, "First Name"),
    FIRST_NAME_REVERSE(Person.class, "First Name (Reversed)", true),
    LAST_NAME(Person.class, "Last Name"),
    LAST_NAME_REVERSE(Person.class, "Last Name (Reversed)", true);

    private String label;
    private String sortField;
    private int luceneSortType;
    private boolean reversed;
    private Class<? extends Indexable> context;

    public static SortOption getDefaultSortOption() {
        return RELEVANCE;
    }

    private SortOption(Class<? extends Indexable> context, String label) {
        this(context, label, false);
    }

    private SortOption(Class<? extends Indexable> context, String label, boolean reversed) {
        this.setLabel(label);
        this.setSortField(sortField);
        this.setReversed(reversed);
        this.setContext(context);
    }

    public static List<SortOption> getOptionsForContext(Class<? extends Indexable> cls) {
        List<SortOption> toReturn = new ArrayList<SortOption>();
        for (SortOption option : SortOption.values()) {
            if ((option.getContext() == null) || cls.isAssignableFrom(option.getContext())) {
                toReturn.add(option);
            }
        }
        return toReturn;
    }

    public static List<SortOption> getOptionsForResourceCollectionPage() {
        List<SortOption> options = SortOption.getOptionsForContext(Resource.class);
        options.remove(SortOption.RESOURCE_TYPE);
        options.remove(SortOption.RESOURCE_TYPE_REVERSE);
        options.add(0, SortOption.RESOURCE_TYPE);
        options.add(1, SortOption.RESOURCE_TYPE_REVERSE);
        return options;
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

    public static <I extends Indexable> List<SortOption> getApplicableSortOptions(Class<I> context) {
        List<SortOption> sortOptions = new ArrayList<SortOption>();
        for (SortOption sortOption : SortOption.values()) {
            if ((sortOption.context == null) || context.equals(sortOption.context)) {
                sortOptions.add(sortOption);
            }
        }
        return sortOptions;
    }

    public String getSortOrder() {
        // SOLR's sorting seems to be reverse of what we're used to, so for most cases, we invert what they're asking for
        // the exception is relevancy
        if (this == RELEVANCE) {
            return "desc";
        }
        if (!reversed) {
            return "asc";
        }
        return "desc";
    }
}