package org.tdar.core.bean;

import org.tdar.core.bean.resource.Status;

/*
 * This interface is designed to ensure that fields are available for basic searhing
 * NOTE: HibernateSearch does not have the ability to inherit the @Field annotations
 * 
 * NOTE: THIS IS NOT USED
 */
@Deprecated
public interface SimpleSearch {

    enum SimpleSearchType {
        RESOURCE("Resource"),
        COLLECTION("Collection");
        
        private String label;

        SimpleSearchType(String label) {
            this.setLabel(label);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
    
    public static final String TITLE_SORT_REGEX = "^([\\s\\W]|The |A |An )+";

    public Long getIndexedId();
    
    public String getTitleSort();

    public String getTitle();

    public String getDescription();
    
    public String getKeywords();

    public String getUrlNamespace();
    
    public SimpleSearchType getSimpleSearchType();

    public Status getStatusForSearch();
}
