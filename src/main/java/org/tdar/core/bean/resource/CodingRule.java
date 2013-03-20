package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Id$
 */
@Entity
@Table(name = "coding_rule")
public class CodingRule extends Persistable.Base implements Comparable<CodingRule> {

    private static final long serialVersionUID = -577936920767925065L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coding_sheet_id")
    @ContainedIn
    private CodingSheet codingSheet;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @Field
    private String term;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ontology_node_id")
    private OntologyNode ontologyNode;

    private transient long count = -1L;

    private transient List<Long> mappedToData = new ArrayList<Long>();

    private transient List<OntologyNode> suggestions = new ArrayList<OntologyNode>();

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
        codingSheet.getCodingRules().add(this);
        setCode(code);
        setTerm(term);
        setDescription(description);
        setOntologyNode(node);
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
        //ab probably okay as not nullable fields
        return Arrays.asList(getCode());
    }

    public void setCode(String code) {
        this.code = sanitize(code);
    }

    // strips leading zeros and trims whitespace from string.
    private static String sanitize(String string) {
        if (string == null || string.isEmpty())
            return null;
        try {
            Integer integer = Integer.parseInt(string);
            string = String.valueOf(integer);
        } catch (NumberFormatException exception) {
            if (string != null) {
                string = string.trim();
            }
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
        this.description = description.trim();
    }

    @XmlElement(name = "codingSheetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CodingSheet getCodingSheet() {
        return codingSheet;
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.codingSheet = codingSheet;
    }

    public String toString() {
        return String.format("{%s, %s, %s, %s}", code, term, description, getOntologyNode());
    }

    /**
     * Default implementation of compareTo using the code.
     */
    public int compareTo(CodingRule other) {
        try {
            // try to use integer comparison instead of String lexicographic comparison
            return Integer.valueOf(code).compareTo(Integer.valueOf(other.code));
        } catch (NumberFormatException exception) {
            return code.compareTo(other.code);
        }
    }

    /**
     * @return the ontologyNode
     */
    public OntologyNode getOntologyNode() {
        return ontologyNode;
    }

    /**
     * @param ontologyNode
     *            the ontologyNode to set
     */
    public void setOntologyNode(OntologyNode ontologyNode) {
        this.ontologyNode = ontologyNode;
    }

    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }

    /**
     * @param count
     *            the count to set
     */
    public void setCount(long count) {
        this.count = count;
    }

    public void setSuggestions(List<OntologyNode> suggestions) {
        this.suggestions = suggestions;
    }

    @XmlTransient
    @JSONTransient
    public List<OntologyNode> getSuggestions() {
        return suggestions;
    }

    public boolean isMappedToData(DataTableColumn col) {
        return mappedToData.contains(col.getId());
    }

    public void setMappedToData(DataTableColumn col) {
        this.mappedToData.add(col.getId());
    }
}
