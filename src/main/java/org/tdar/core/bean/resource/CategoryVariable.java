package org.tdar.core.bean.resource;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.tdar.core.bean.Persistable;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * $Id$
 * 
 * A domain variable (faunal variable, etc.) belonging to the system's master ontology.
 * 
 * 
 * FIXME: does each individual domain context variable really represent a
 * Resource? If so, should extend Resource instead. However, it seems the
 * entire master ontology should be a Resource, not each individual element in
 * the master ontology, which is what each instance of this class represents.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Table(name = "category_variable")
public class CategoryVariable extends Persistable.Base implements Comparable<CategoryVariable> {

    /**
     * 
     */
    private static final long serialVersionUID = -7579426625034598257L;

    @Column(nullable = false)
    private String name;

    private String label;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @ManyToOne
    private CategoryVariable parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<CategoryVariable> children;

    @OneToMany(mappedBy = "categoryVariable")
    @XStreamOmitField
    private Set<CodingSheet> codingSheets;

    @OneToMany(mappedBy = "categoryVariable")
    @XStreamOmitField
    private Set<Ontology> ontologies;

    @Column(name = "encoded_parent_ids")
    private String encodedParentIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ElementCollection()
    @JoinTable(name = "category_variable_synonyms")
    private Set<String> synonyms;

    public Set<String> getSynonyms() {
        return synonyms;
    }

    @Transient
    public SortedSet<String> getSortedSynonyms() {
        return new TreeSet<String>(synonyms);
    }

    @Transient
    public SortedSet<String> getSortedSynonyms(Comparator<String> comparator) {
        TreeSet<String> sortedSet = new TreeSet<String>(comparator);
        sortedSet.addAll(synonyms);
        return sortedSet;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<CodingSheet> getCodingSheets() {
        return codingSheets;
    }

    public void setCodingSheets(Set<CodingSheet> codingSheets) {
        this.codingSheets = codingSheets;
    }

    public Set<Ontology> getOntologies() {
        return ontologies;
    }

    public void setOntologies(Set<Ontology> ontologies) {
        this.ontologies = ontologies;
    }

    @Transient
    public Long getRootParentId() {
        if (encodedParentIds == null) {
            return null;
        }
        int dotIndex = encodedParentIds.indexOf(".");
        String rootParentId = (dotIndex == -1) ? encodedParentIds : encodedParentIds.substring(0, dotIndex);
        return Long.valueOf(rootParentId);
    }

    public CategoryVariable getParent() {
        return parent;
    }

    public void setParent(CategoryVariable parent) {
        this.parent = parent;
    }

    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<CategoryVariable> getChildren() {
        return children;
    }

    public void setChildren(Set<CategoryVariable> children) {
        this.children = children;
    }

    public int compareTo(CategoryVariable candidate) {
        return name.compareTo(candidate.name);
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public void setType(String type) {
        setType(CategoryType.valueOf(type));
    }

    @Transient
    public boolean isCategory() {
        return type.equals(CategoryType.CATEGORY);
    }

    @Transient
    public SortedSet<CategoryVariable> getAllChildren() {
        TreeSet<CategoryVariable> allChildren = new TreeSet<CategoryVariable>();
        allChildren.addAll(children);
        for (CategoryVariable child : children) {
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    @Transient
    public Integer[] getParentIds() {
        String[] parentStringIds = encodedParentIds.split("\\.");
        Integer[] parentIds = new Integer[parentStringIds.length];
        for (int i = 0; i < parentStringIds.length; i++) {
            parentIds[i] = Integer.valueOf(parentStringIds[i]);
        }
        return parentIds;
    }

    public String getEncodedParentIds() {
        return encodedParentIds;
    }

    public void setEncodedParentIds(String encodedParentIds) {
        this.encodedParentIds = encodedParentIds;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
