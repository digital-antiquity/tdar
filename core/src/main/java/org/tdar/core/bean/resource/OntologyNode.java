package org.tdar.core.bean.resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonIdNameFilter;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * <p>
 * Persistent entity representing a specific node in the ontology.
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "ontology_node", indexes = {
        @Index(name = "ontology_node_interval_start_index", columnList = "interval_start"),
        @Index(name = "ontology_node_interval_end_index", columnList = "interval_end"),
        @Index(name = "ontology_node_index", columnList = "index")
})
public class OntologyNode extends AbstractPersistable implements Comparable<OntologyNode>, Slugable {

    private static final long serialVersionUID = 6997306639142513872L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final OntologyNode NULL = new OntologyNode() {
        private static final long serialVersionUID = 2863511954419520195L;

        @Override
        public String getDisplayName() {
            return "";
        }
    };

    @ManyToOne(optional = false)
    private Ontology ontology;

    @Column(name = "interval_start")
    private Integer intervalStart;

    @Column(name = "interval_end")
    private Integer intervalEnd;

    @Column(name = "display_name")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String displayName;

    @Length(max = FieldLength.FIELD_LENGTH_2048)
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(name = "ontology_node_synonym")
    private Set<String> synonyms;

    private String index;

    private String iri;

    // @Column(unique=true)
    private String uri;

    @Column(name = "import_order")
    private Long importOrder;

    // is this ontology node a synonym of another ontology node?
    private transient boolean synonym;
    // true if this ontology node or its children doesn't have any mapped data
    private transient boolean mappedDataValues;
    private transient boolean parent;
    private transient Map<DataTableColumn, Boolean> columnHasValueMap = new HashMap<>();

    private transient OntologyNode parentNode;
    private transient Set<OntologyNode> synonymNodes = new HashSet<>();

    public OntologyNode() {
    }

    public OntologyNode(String iri, String label) {
        this.iri = iri;
        this.displayName = label;
    }

    public OntologyNode(Long id) {
        setId(id);
    }

    @XmlElement(name = "ontologyRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public Integer getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(Integer start) {
        this.intervalStart = start;
    }

    public Integer getIntervalEnd() {
        return intervalEnd;
    }

    public void setIntervalEnd(Integer end) {
        this.intervalEnd = end;
    }

    @JsonView({ JsonIntegrationDetailsFilter.class, JsonIdNameFilter.class })
    public String getIri() {
        return iri;
    }

    public void setIri(String label) {
        this.iri = label;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("id:%-8s label:%-25s url:%s ", getId(), getDisplayName(), iri);
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(iri);
    }

    @Transient
    public int getNumberOfParents() {
        if (StringUtils.isEmpty(index)) {
            return 0;
        }
        return StringUtils.split(index, '.').length;
    }

    @Override
    public int compareTo(OntologyNode other) {
        return ObjectUtils.compare(index, other.getIndex());
    }

    @Transient
    public String getIndentedLabel() {
        StringBuilder builder = new StringBuilder(index).append(' ').append(iri);
        return builder.toString();
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImportOrder(Long importOrder) {
        this.importOrder = importOrder;
    }

    public Long getImportOrder() {
        return importOrder;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<String> getEquivalenceSet() {
        HashSet<String> equivalenceSet = new HashSet<String>();
        for (String synonym : getSynonyms()) {
            equivalenceSet.add(synonym.toLowerCase());
        }
        equivalenceSet.add(displayName.toLowerCase());
        equivalenceSet.add(StringUtils.lowerCase(getNormalizedIri()));
        return equivalenceSet;
    }

    public String getNormalizedIri() {
        return OntologyNode.normalizeIri(iri);
    }

    public static String normalizeIri(String iriInput) {
        String iri_ = StringUtils.trim(iriInput);
        iri_ = StringUtils.replace(iri_, ".", "_");
        // backwards compatibility to help with mappings which start with digests
        if ((iri_ != null) && iri_.matches("^\\_\\d.*")) {
            return StringUtils.substring(iri_, 1);
        } else {
            return iri_;
        }
    }

    @Transient
    public boolean isEquivalentTo(OntologyNode existing) {
        // easy cases
        if (existing == null) {
            return false;
        }
        if (equals(existing)) {
            return true;
        }

        logger.trace("testing synonyms");
        for (String displayName_ : getEquivalenceSet()) {
            for (String existingDisplayName : existing.getEquivalenceSet()) {
                if (existingDisplayName.equalsIgnoreCase(displayName_)) {
                    logger.trace("\tcomparing {} <> {}", displayName, existingDisplayName);
                    return true;
                }
            }
        }
        return false;
    }

    @XmlTransient
    public OntologyNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(OntologyNode parentNode) {
        this.parentNode = parentNode;
    }

    @XmlTransient
    public Set<OntologyNode> getSynonymNodes() {
        return synonymNodes;
    }

    public void setSynonymNodes(Set<OntologyNode> synonymNodes) {
        this.synonymNodes = synonymNodes;
    }

    @Transient
    public boolean isChildOf(OntologyNode parentNode) {
        return (parentNode != null) && (parentNode.getIntervalStart() < getIntervalStart()) && (parentNode.getIntervalEnd() >= getIntervalEnd());
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    @XmlTransient
    public boolean isSynonym() {
        return synonym;
    }

    public void setSynonym(boolean synonym) {
        this.synonym = synonym;
    }

    public String getFormattedNameWithSynonyms() {
        if (CollectionUtils.isNotEmpty(getSynonyms())) {
            String txt = String.format("%s (%s)", getDisplayName(), StringUtils.join(getSynonyms(), ", "));
            return txt;
        } else {
            return getDisplayName();
        }
    }

    @Transient
    public boolean isDisabled() {
        return !mappedDataValues;
    }

    @Transient
    public boolean isMappedDataValues() {
        return mappedDataValues;
    }

    public void setMappedDataValues(boolean mappedDataValues) {
        this.mappedDataValues = mappedDataValues;
    }

    public Map<DataTableColumn, Boolean> getColumnHasValueMap() {
        return columnHasValueMap;
    }

    public void setColumnHasValueMap(Map<DataTableColumn, Boolean> columnHasValueMap) {
        this.columnHasValueMap = columnHasValueMap;
    }

    @Override
    @XmlTransient
    @Transient
    @JsonIgnore
    public String getSlug() {
        return UrlUtils.slugify(getIri());
    }

}
