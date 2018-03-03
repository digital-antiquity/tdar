package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * Represents an entry in a CodingSheet consisting of a String code (key),
 * the String term (value) that the code is mapped to, and an optional description.
 * 
 * CodingRules can also be mapped to an OntologyNode in an Ontology, which is essential for the way we
 * currently perform data integration, where different datasets can be compared through their mappings
 * to nodes in a common ontology.
 */
@Entity
@Table(name = "coding_rule", indexes = {
        @Index(name = "coding_rule_coding_sheet_id_idx", columnList = "coding_sheet_id"),
        @Index(name = "coding_rule_term_index", columnList = "term"),
        @Index(name = "coding_rule_ontology_node_id_idx", columnList = "ontology_node_id")
})
public class CodingRule extends AbstractPersistable implements Comparable<CodingRule>, HasStatic {

    private static final long serialVersionUID = -577936920767925065L;

    @Override
    @XmlTransient
    @Transient
    public boolean isStatic() {
        return false;
    }

    public static final CodingRule NULL = new CodingRule() {

        private static final long serialVersionUID = -4642755669423445084L;

        @Override
        public String getTerm() {
            return "NULL";
        }

        @Override
        public String getCode() {
            return "__NULL";
        }

        public String getFormattedTerm() {
            return getTerm();
        }

        @Override
        public boolean isStatic() {
            return true;
        }

    };

    public static final CodingRule MISSING = new CodingRule() {

        private static final long serialVersionUID = 7942595387432781223L;

        @Override
        public String getTerm() {
            return "MISSING";
        }

        @Override
        public String getCode() {
            return "__MISSING";
        }

        public String getFormattedTerm() {
            return getTerm();
        }

        @Override
        public boolean isStatic() {
            return true;
        }

    };

    public static final CodingRule UNMAPPED = new CodingRule() {

        private static final long serialVersionUID = -5773650328175904771L;

        @Override
        public String getTerm() {
            return "UNMAPPED";
        }

        @Override
        public String getCode() {
            return "__UNMAPPED";
        }

        public String getFormattedTerm() {
            return getTerm();
        }

        @Override
        public boolean isStatic() {
            return true;
        }

    };

    @ManyToOne(optional = false)
    @JoinColumn(name = "coding_sheet_id")
    private CodingSheet codingSheet;

    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String code;

    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String term;

    // FIXME: use a Lob instead?
    @Column(length = 2000)
    @Length(max = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ontology_node_id")
    private OntologyNode ontologyNode;

    private transient long count = -1L;

    private transient List<Long> mappedToData = new ArrayList<>();

    private transient List<OntologyNode> suggestions = new ArrayList<>();

    public CodingRule() {
    }

    public CodingRule(CodingSheet codingSheet, String value) {
        this(codingSheet, value, value, "", null);
    }

    public CodingRule(CodingSheet codingSheet, String code, String term, String description) {
        this(codingSheet, code, term, description, null);
    }

    public CodingRule(CodingSheet codingSheet, String code, String term, String description, OntologyNode node) {
        setCodingSheet(codingSheet);
        setCode(code);
        setTerm(term);
        setDescription(description);
        setOntologyNode(node);
        // FIXME: must be careful when adding "this" to collections inside a constructor to avoid NPEs from uninitialized instance variables.
        codingSheet.getCodingRules().add(this);
    }

    public CodingRule(String unmappedValue, Long count) {
        setTerm(term);
        setCount(count);
        setOntologyNode(OntologyNode.NULL);
    }

    public String getCode() {
        return code;
    }

    @Override
    public List<?> getEqualityFields() {
        // ab probably okay as not nullable fields
        return Arrays.asList(getCode());
    }

    public void setCode(String code) {
        this.code = sanitize(code);
    }

    // strips leading zeros and trims whitespace from string.
    private static String sanitize(String string_) {
        String string = string_;
        if (StringUtils.isEmpty(string)) {
            return null;
        }
        if (string != null) {
            string = string.trim();
        }
        return string;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trimToNull(description);
    }

    @XmlElement(name = "codingSheetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CodingSheet getCodingSheet() {
        return codingSheet;
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.codingSheet = codingSheet;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s, %s}", code, term, description, getOntologyNode());
    }

    /**
     * Default implementation of compareTo using the code.
     */
    @Override
    public int compareTo(CodingRule other) {
        try {
            // first try integer comparison instead of String lexicographic comparison
            return Integer.valueOf(code).compareTo(Integer.valueOf(other.code));
        } catch (NumberFormatException exception) {
            return code.compareTo(other.code);
        }
    }

    public OntologyNode getOntologyNode() {
        return ontologyNode;
    }

    public void setOntologyNode(OntologyNode ontologyNode) {
        this.ontologyNode = ontologyNode;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setSuggestions(List<OntologyNode> suggestions) {
        this.suggestions = suggestions;
    }

    @XmlTransient
    public List<OntologyNode> getSuggestions() {
        return suggestions;
    }

    public boolean isMappedToData(DataTableColumn col) {
        return mappedToData.contains(col.getId());
    }

    public void setMappedToData(DataTableColumn col) {
        mappedToData.add(col.getId());
    }

    @XmlTransient
    public String getFormattedTerm() {
        if (StringUtils.equalsIgnoreCase(getCode(), getTerm())) {
            return getTerm();
        }

        return String.format("%s (%s)", getTerm(), getCode());
    }
}
