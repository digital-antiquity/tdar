package org.tdar.core.bean.resource;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * $Id$
 * <p>
 * A coding sheet contains a set of CodingRules and is associated to a domain context variable in the master ontology.  
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 543$
 */
@Entity
@Indexed
@Table(name = "coding_sheet")
public class CodingSheet extends InformationResource {

    private static final long serialVersionUID = 7782805674943954511L;

    @ManyToOne
    @JoinColumn(name = "category_variable_id") 
    private CategoryVariable categoryVariable;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="codingSheet")
    @IndexedEmbedded
    private Set<CodingRule> codingRules = new LinkedHashSet<CodingRule>();
    
    @OneToMany(mappedBy="defaultCodingSheet")
    @XStreamOmitField
    private Set<DataTableColumn> associatedDataTableColumns = new HashSet<DataTableColumn>();
    
    public CodingSheet() {
    	setResourceType(ResourceType.CODING_SHEET);
    }
    
    public Set<CodingRule> getCodingRules() {
        return codingRules;
    }

    public void setCodingRules(Set<CodingRule> codingRules) {
        this.codingRules = codingRules;
    }

    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    public void setCategoryVariable(CategoryVariable domainContextVariable) {
        this.categoryVariable = domainContextVariable;
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
    public Map<String, String> toCodingRulesMap() {
        Map<String, String> map = new HashMap<String, String>();
        for (CodingRule codingRule : codingRules) {
            map.put(codingRule.getCode(), codingRule.getTerm());
        }
        return map;
    }

    public Set<DataTableColumn> getAssociatedDataTableColumns() {
        return associatedDataTableColumns;
    }

    public void setAssociatedDataTableColumns(Set<DataTableColumn> associatedDataTableColumns) {
        this.associatedDataTableColumns = associatedDataTableColumns;
    }
    
}
