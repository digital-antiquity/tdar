package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
@Table(name = "ontology_node")
public class OntologyNode extends Persistable.Base implements Comparable<OntologyNode> {

    private static final long serialVersionUID = 6997306639142513872L;

    @ManyToOne(optional = false)
    @XStreamOmitField
    private Ontology ontology;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ontologyNode")
    private Set<DataValueOntologyNodeMapping> dataValueOntologyNodeMappings = new HashSet<DataValueOntologyNodeMapping>();

    @Column(name = "interval_start")
    private Integer intervalStart;
    @Column(name = "interval_end")
    private Integer intervalEnd;

    @Column(name = "display_name")
    private String displayName;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @ElementCollection()
    @JoinTable(name = "ontology_node_synonym")
    private Set<String> synonyms;

    private String index;

    private String iri;

    // @Column(unique=true)
    private String uri;

    @Column(name = "import_order")
    private Long importOrder;

    public OntologyNode() {
    }

    public OntologyNode(Long id) {
        setId(id);
    }

    @XmlTransient
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

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String toString() {
        return "label: " + iri + ", uri: " + uri;
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(iri);
    }

    @Transient
    public int getNumberOfParents() {
        if (StringUtils.isEmpty(index))
            return 0;
        return StringUtils.split(index, '.').length;
    }

    public int compareTo(OntologyNode other) {
        return index.compareTo(other.index);
    }

    public Set<DataValueOntologyNodeMapping> getDataValueOntologyNodeMappings() {
        return dataValueOntologyNodeMappings;
    }

    public void setDataValueOntologyNodeMappings(Set<DataValueOntologyNodeMapping> dataValueOntologyNodeMappings) {
        this.dataValueOntologyNodeMappings = dataValueOntologyNodeMappings;
    }

    /**
     * Returns a List<String> representing the actual data value strings within
     * the DataTableColumn that have been mapped to this OntologyNode.
     * 
     * @param column
     * @return
     */
    @Transient
    public List<String> getMappedDataValues(DataTableColumn column) {
        if (CollectionUtils.isEmpty(dataValueOntologyNodeMappings)) {
            return Collections.emptyList();
        }
        ArrayList<String> mappedValues = new ArrayList<String>();
        Long columnId = column.getId();
        for (DataValueOntologyNodeMapping mapping : dataValueOntologyNodeMappings) {
            if (columnId.equals(mapping.getDataTableColumn().getId())) {
                mappedValues.add(mapping.getDataValue());
            }
        }
        return mappedValues;
    }

    @Transient
    public String getIndentedLabel() {
        StringBuilder builder = new StringBuilder(index).append(' ').append(iri);
        return builder.toString();
    }

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

    /**
     * @return the importOrder
     */
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
        equivalenceSet.add(iri.toLowerCase());
        return equivalenceSet;
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
                    logger.trace("\tcomparing " + displayName_ + "<>" + existingDisplayName + " --> equivalent");
                    return true;
                }
            }
        }
        return false;
    }


    public static final OntologyNode NULL = new OntologyNode() {

        private static final long serialVersionUID = 2863511954419520195L;
        
        @Override
        public String getDisplayName() {
            return "";
        }
    };

    
}
