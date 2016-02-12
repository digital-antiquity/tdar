package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.SupportsResource;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * OntologyS in tDAR are InformationResources with a collection of OntologyNodeS that can be categorized.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision $
 */
@Entity
//@Indexed
@Table(name = "ontology", indexes = { @Index(name = "ontology_catvar_id", columnList = "category_variable_id") })
@XmlRootElement(name = "ontology")
public class Ontology extends InformationResource implements SupportsResource {

    private static final long serialVersionUID = -5871337600253105652L;

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    //@IndexedEmbedded(depth = 1)
    private CategoryVariable categoryVariable;

    @OneToMany(mappedBy = "ontology", cascade = CascadeType.ALL)
    private List<OntologyNode> ontologyNodes = new ArrayList<>();

    private transient Map<Long, OntologyNode> idMap = new WeakHashMap<>();
    private transient Map<String, OntologyNode> iriMap = new WeakHashMap<>();
    private transient Map<String, OntologyNode> nameMap = new WeakHashMap<>();

    public final static Comparator<OntologyNode> IMPORT_ORDER_COMPARATOR = new Comparator<OntologyNode>() {
        @Override
        public int compare(OntologyNode o1, OntologyNode o2) {
            int comparison = o1.getImportOrder().compareTo(o2.getImportOrder());
            if (comparison == 0) {
                // use default comparison by index
                return o1.compareTo(o2);
            }
            return comparison;
        }
    };

    public Ontology() {
        setResourceType(ResourceType.ONTOLOGY);
    }

    @Override
    @JsonView(JsonIntegrationFilter.class)
    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    @Override
    public void setCategoryVariable(CategoryVariable categoryVariable) {
        this.categoryVariable = categoryVariable;
    }

    public Map<Long, OntologyNode> getIdToNodeMap() {
        HashMap<Long, OntologyNode> idToNodeMap = new HashMap<>();
        for (OntologyNode node : ontologyNodes) {
            idToNodeMap.put(node.getId(), node);
        }
        return idToNodeMap;
    }

    /**
     * Returns a list of internal IDs (not database IDs) mapped to list of child ontology nodes.
     * 
     * @return
     */
    public SortedMap<Integer, List<OntologyNode>> toOntologyNodeMap() {
        List<OntologyNode> sortedOntologyNodes = getSortedOntologyNodes();
        TreeMap<Integer, List<OntologyNode>> map = new TreeMap<>();
        for (OntologyNode node : sortedOntologyNodes) {
            Integer intervalStart = node.getIntervalStart();
            String index = node.getIndex();
            for (String indexId : StringUtils.split(index, '.')) {
                Integer parentId = Integer.valueOf(indexId);
                // don't include this node if the parent id is the same as this node's interval start
                if (parentId.equals(intervalStart)) {
                    continue;
                }
                List<OntologyNode> children = map.get(parentId);
                if (children == null) {
                    children = new ArrayList<>();
                    map.put(parentId, children);
                }
                children.add(node);
            }
        }
        return map;
    }

    @Transient
    public OntologyNode getNodeByName(String name) {
        if (MapUtils.isEmpty(nameMap)) {
            initializeNameAndIriMaps();
        }
        return nameMap.get(name);
    }

    @Transient
    public OntologyNode getNodeByIri(String iri) {
        if (MapUtils.isEmpty(iriMap)) {
            initializeNameAndIriMaps();
        }
        return iriMap.get(iri);
    }

    private void initializeNameAndIriMaps() {
        for (OntologyNode node : getOntologyNodes()) {
            nameMap.put(node.getDisplayName(), node);
            iriMap.put(node.getNormalizedIri(), node);
        }
    }

    public void clearTransientMaps() {
        nameMap.clear();
        iriMap.clear();
        idMap.clear();
    }
    
    @Transient
    public OntologyNode getNodeByNameIgnoreCase(String name) {
        for (OntologyNode node : getOntologyNodes()) {
            if (StringUtils.equalsIgnoreCase(node.getDisplayName(), name)) {
                return node;
            }
        }
        return null;
    }

    public List<OntologyNode> getSortedOntologyNodes() {
        // return ontology nodes by natural order.
        return getSortedOntologyNodes(null);
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    @JsonProperty(value = "nodes")
    public List<OntologyNode> getSortedOntologyNodesByImportOrder() {
        return getSortedOntologyNodes(IMPORT_ORDER_COMPARATOR);
    }

    public List<OntologyNode> getSortedOntologyNodes(Comparator<OntologyNode> comparator) {
        ArrayList<OntologyNode> sortedNodes = new ArrayList<>(getOntologyNodes());
        Collections.sort(sortedNodes, comparator);
        return sortedNodes;
    }

    @XmlElementWrapper(name = "ontologyNodes")
    @XmlElement(name = "ontologyNode")
    public List<OntologyNode> getOntologyNodes() {
        return ontologyNodes;
    }

    public void setOntologyNodes(List<OntologyNode> ontologyNodes) {
        this.ontologyNodes = ontologyNodes;
    }

    public OntologyNode getOntologyNodeById(Long id) {
        if (idMap.isEmpty()) {
            for (OntologyNode node : getOntologyNodes()) {
                idMap.put(node.getId(), node);
            }
        }
        return idMap.get(id);
    }

}
