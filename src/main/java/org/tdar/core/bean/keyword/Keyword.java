package org.tdar.core.bean.keyword;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.tdar.core.bean.Persistable;
import org.tdar.index.AutocompleteAnalyzer;
import org.tdar.index.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.index.NonTokenizingLowercaseKeywordAnalyzer;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface Keyword extends Persistable {

    public String getLabel();

    public void setLabel(String label);

    public String getDefinition();

    public String getKeywordType();

    public void setDefinition(String definition);

    @MappedSuperclass
    @XmlType(name = "kwdbase")
    public static abstract class Base extends Persistable.Base implements Keyword {

        private static final long serialVersionUID = -7516574981065004043L;

        @Transient
        private final static String[] JSON_PROPERTIES = { "id", "label" };

        @Column(nullable = false, unique = true)
        @Fields({ @Field(name = "label", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
                @Field(name = "label_auto", analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)),
                @Field(name = "labelKeyword", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)) })
        private String label;

        @Lob
        @Type(type = "org.hibernate.type.StringClobType")
        private String definition;

        @Field
        @Transient
        public String getKeywordType() {
            return getClass().getSimpleName();
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String toString() {
            return label;
        }

        @Override
        public String[] getIncludedJsonProperties() {
            return JSON_PROPERTIES;
        }

    }
}
