package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import javax.persistence.Index;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.JSONTransient;

/**
 * $Id$
 * <p>
 * Represents a categorized set of CodingRules and may be bound to a specific ontology. DataTableColumns associated with a CodingSheet are automatically
 * translated.
 * 
 * CodingRules themselves can be mapped to a specific node in this CodingSheet's bound ontology.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 543$
 */
@Entity
// @Indexed(interceptor=DontIndexWhenGeneratedInterceptor.class)
@Indexed
@Table(name = "coding_sheet", indexes={
        @Index(name = "coding_catvar_id",columnList="category_variable_id"),
        @Index(name = "coding_sheet_default_ontology_id_idx", columnList="default_ontology_id")
})
@XmlRootElement(name = "codingSheet")
public class CodingSheet extends InformationResource implements SupportsResource {

    private static final long serialVersionUID = 7782805674943954511L;
    public static final String[] JSON_PROPERTIES = { "defaultOntology" };

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    @IndexedEmbedded(depth = 1)
    private CategoryVariable categoryVariable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "codingSheet")
    @IndexedEmbedded
    private Set<CodingRule> codingRules = new LinkedHashSet<CodingRule>();

    @OneToMany(mappedBy = "defaultCodingSheet")
    private Set<DataTableColumn> associatedDataTableColumns = new HashSet<DataTableColumn>();

    @ManyToOne
    @JoinColumn(name = "default_ontology_id")
    private Ontology defaultOntology;

    @Field
    private boolean generated = false;

    public CodingSheet() {
        setResourceType(ResourceType.CODING_SHEET);
    }

    public Set<CodingRule> getCodingRules() {
        return codingRules;
    }

    public void setCodingRules(Set<CodingRule> codingRules) {
        this.codingRules = codingRules;
    }

    @Override
    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    @Override
    public void setCategoryVariable(CategoryVariable categoryVariable) {
        this.categoryVariable = categoryVariable;
    }

    @Transient
    public SortedSet<CodingRule> getSortedCodingRules() {
        return new TreeSet<CodingRule>(getCodingRules());
    }

    @Transient
    public SortedSet<CodingRule> getSortedCodingRules(Comparator<CodingRule> comparator) {
        TreeSet<CodingRule> sortedCodingRules = new TreeSet<CodingRule>(comparator);
        sortedCodingRules.addAll(getCodingRules());
        return sortedCodingRules;
    }

    @Transient
    public Map<String, CodingRule> getCodeToRuleMap() {
        HashMap<String, CodingRule> map = new HashMap<String, CodingRule>();
        for (CodingRule codingRule : getCodingRules()) {
            map.put(codingRule.getCode(), codingRule);
        }
        return map;
    }

    @Transient
    public Map<String, List<CodingRule>> getTermToCodingRuleMap() {
        Map<String, List<CodingRule>> map = new HashMap<String, List<CodingRule>>();
        for (CodingRule codingRule : codingRules) {
            String term = codingRule.getTerm();
            List<CodingRule> rules = map.get(term);
            if (rules == null) {
                rules = new ArrayList<CodingRule>();
                map.put(term, rules);
            }
            rules.add(codingRule);
        }
        return map;
    }

    @Transient
    public Map<String, OntologyNode> getTermToOntologyNodeMap() {
        HashMap<String, OntologyNode> map = new HashMap<String, OntologyNode>();
        for (CodingRule codingRule : getCodingRules()) {
            map.put(codingRule.getTerm(), codingRule.getOntologyNode());
        }
        return map;
    }

    public List<CodingRule> getCodingRuleByTerm(String term) {
        List<CodingRule> rules = new ArrayList<CodingRule>();
        if (StringUtils.isEmpty(term)) {
            return null;
        }
        for (CodingRule rule : getCodingRules()) {
            if (StringUtils.equals(term, rule.getTerm())) {
                rules.add(rule);
            }
        }
        return rules;
    }

    public CodingRule getCodingRuleByCode(String code) {
        if (StringUtils.isNotEmpty(code)) {
            for (CodingRule rule : getCodingRules()) {
                if (StringUtils.equals(code, rule.getCode())) {
                    return rule;
                }
            }
        }
        return null;
    }

    @Transient
    public Map<String, List<Long>> getTermToOntologyNodeIdMap() {
        HashMap<String, List<Long>> map = new HashMap<String, List<Long>>();
        for (CodingRule codingRule : codingRules) {
            OntologyNode node = codingRule.getOntologyNode();
            if (node != null) {
                String term = codingRule.getTerm();
                if (!map.containsKey(term)) {
                    map.put(term, new ArrayList<Long>());
                }
                map.get(term).add(node.getId());
            }
        }
        return map;
    }

    public Set<DataTableColumn> getAssociatedDataTableColumns() {
        return associatedDataTableColumns;
    }

    public void setAssociatedDataTableColumns(Set<DataTableColumn> associatedDataTableColumns) {
        this.associatedDataTableColumns = associatedDataTableColumns;
    }

    /**
     * @return the defaultOntology
     */
    public Ontology getDefaultOntology() {
        return defaultOntology;
    }

    /**
     * @param defaultOntology
     *            the defaultOntology to set
     */
    public void setDefaultOntology(Ontology defaultOntology) {
        this.defaultOntology = defaultOntology;
    }

    private transient Map<Long, CodingRule> idMap = new HashMap<Long, CodingRule>();

    public CodingRule getCodingRuleById(Long id) {
        if (idMap.isEmpty()) {
            for (CodingRule node : getCodingRules()) {
                idMap.put(node.getId(), node);
            }
        }
        return idMap.get(id);
    }

    @Transient
    @JSONTransient
    @XmlTransient
    public Map<OntologyNode, List<CodingRule>> getNodeToDataValueMap() {
        HashMap<OntologyNode, List<CodingRule>> map = new HashMap<OntologyNode, List<CodingRule>>();
        for (CodingRule rule : getCodingRules()) {
            OntologyNode node = rule.getOntologyNode();
            if (node != null) {
                List<CodingRule> list = map.get(node);
                if (list == null) {
                    list = new ArrayList<CodingRule>();
                    map.put(node, list);
                }
                list.add(rule);
            }
        }
        return map;
    }

    public List<CodingRule> getMappedValues() {
        List<CodingRule> toReturn = new ArrayList<CodingRule>();
        for (CodingRule rule : getCodingRules()) {
            if (rule.getOntologyNode() != null) {
                toReturn.add(rule);
            }
        }
        return toReturn;
    }

    public List<CodingRule> findRuleMappedToOntologyNode(OntologyNode node) {
        if (node == null || CollectionUtils.isEmpty(getCodingRules()))
            return new ArrayList<CodingRule>();
        Map<OntologyNode, List<CodingRule>> nodeToDataValueMap = getNodeToDataValueMap();
        return nodeToDataValueMap.get(node);
        // for (CodingRule rule : getCodingRules()) {
        // if (rule == null || rule.getOntologyNode() == null)
        // continue;
        // // logger.trace("comparing: {} to {} ", rule.getTerm(), node.getIri());
        // if (rule.getOntologyNode().equals(node)) {
        // return rule;
        // }
        // }
        // return null;
    }

    /**
     * Returns true if this coding sheet was system generated as a result of associating an ontology but no coding sheet
     * with a data table column.
     * 
     * @return true if this coding sheet is a system generated coding sheet, false otherwise
     */
    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    @Override
    public String getAdditonalKeywords() {
        StringBuilder sb = new StringBuilder();
        if (getCategoryVariable() != null) {
            sb.append(getCategoryVariable().getLabel()).append(" ");
            if (getCategoryVariable().getParent() != null) {
                sb.append(getCategoryVariable().getParent().getLabel());
            }
        }
        return sb.toString();
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        ArrayList<String> allProperties = new ArrayList<String>(Arrays.asList(super.getIncludedJsonProperties()));
        allProperties.addAll(Arrays.asList(JSON_PROPERTIES));
        return allProperties.toArray(new String[allProperties.size()]);
    }

}
