package org.tdar.core.bean;

import org.tdar.core.bean.entity.Person;

import java.util.Comparator;

/**
 * This interface is designed to ensure that fields are available for basic searhing
 * 
 * NOTE: THIS IS NOT USED
 */
public interface SimpleSearch extends Persistable {
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

    String TITLE_SORT_REGEX = "^([\\s\\W]|The |A |An )+";

    Comparator<SimpleSearch> TITLE_COMPARATOR = (c1, c2) -> c1.getTitleSort().compareTo(c2.getTitleSort());

    String getTitleSort();


    String getTitle();

    String getDescription();

    String getUrlNamespace();

    Person getSubmitter();
}
