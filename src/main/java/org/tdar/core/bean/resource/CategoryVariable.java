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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

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
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Field
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String label;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private CategoryType type;

    @ManyToOne
    // @IndexedEmbedded(depth=1)
    private CategoryVariable parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<CategoryVariable> children;

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
        return new TreeSet<String>(getSynonyms());
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

    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CategoryVariable getParent() {
        return parent;
    }

    public void setParent(CategoryVariable parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Set<CategoryVariable> getChildren() {
        return children;
    }

    public void setChildren(Set<CategoryVariable> children) {
        this.children = children;
    }

    @Override
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
    public SortedSet<CategoryVariable> getSortedChildren() {
        TreeSet<CategoryVariable> sortedChildren = new TreeSet<CategoryVariable>();
        sortedChildren.addAll(this.getChildren());
        return sortedChildren;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
