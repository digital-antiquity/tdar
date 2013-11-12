package org.tdar.core.bean;

import org.tdar.utils.MessageHelper;

/*
 * This interface is designed to ensure that fields are available for basic searhing
 * NOTE: HibernateSearch does not have the ability to inherit the @Field annotations
 * 
 * NOTE: THIS IS NOT USED
 */
public interface SimpleSearch {
    enum SimpleSearchType {
        RESOURCE(MessageHelper.getMessage("simpleSearchType.resource")),
        COLLECTION(MessageHelper.getMessage("simpleSearchType.collection"));

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

    static final String TITLE_SORT_REGEX = "^([\\s\\W]|The |A |An )+";

    String getTitleSort();

    String getTitle();

    String getDescription();

    String getUrlNamespace();

}
