package org.tdar.core.bean.resource;

import java.util.Comparator;
import java.util.LinkedHashSet;
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

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * A top-level domain variable (faunal variable, etc.) belonging to the system's master ontology.
 * 
 * Ideally these should be represented as OntologyNodes in an actual tDAR Ontology.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
@Entity
@Table(name = "category_variable")
public class CategoryVariable extends AbstractPersistable implements Comparable<CategoryVariable> {

    private static final long serialVersionUID = -7579426625034598257L;

    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String label;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
//    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private CategoryType type;

    @ManyToOne
    private CategoryVariable parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<CategoryVariable> children = new LinkedHashSet<>();

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
        return new TreeSet<>(getSynonyms());
    }

    @Transient
    public SortedSet<String> getSortedSynonyms(Comparator<String> comparator) {
        TreeSet<String> sortedSet = new TreeSet<>(comparator);
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

    @JsonView(JsonLookupFilter.class)
    @JsonInclude(Include.NON_NULL)
    public String getParentCategoryName() {
        if (parent != null) {
            return parent.getName();
        }
        return null;
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
        return new TreeSet<>(getChildren());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
