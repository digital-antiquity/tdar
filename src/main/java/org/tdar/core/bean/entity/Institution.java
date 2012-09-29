package org.tdar.core.bean.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.tdar.core.bean.BulkImportField;
import org.tdar.index.analyzer.AutocompleteAnalyzer;
import org.tdar.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;

/**
 * $Id$
 * 
 * Records the relevant information regarding an institution.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "institution")
@Indexed(index = "Institution")
@DiscriminatorValue("INSTITUTION")
@XmlRootElement(name = "institution")
public class Institution extends Creator implements Comparable<Institution> {

    private static final long serialVersionUID = 892315581573902067L;

    public Institution() {
    }

    public Institution(String name) {
        this.name = name;
    }

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "name", "url" };

    private static final String ACRONYM_REGEX = "(?:.+)(?:[\\(\\[\\{])(.+)(?:[\\)\\]\\}])(?:.*)";

    @Column(nullable = false, unique = true)
    @BulkImportField(label="Institution Name",comment=BulkImportField.CREATOR_INSTITUTION_DESCRIPTION,order=10)
    private String name;

    private String url;

    private String location;

    public int compareTo(Institution candidate) {
        return name.compareTo(candidate.name);
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = true)
    private Institution parentInstitution;

    @ElementCollection()
    @JoinTable(name = "institution_synonym")
    private Set<String> alternateNames;

    @XmlElement
    //FIXME: this seemingly conflicts w/ @Field annotations on Creator.getName(). Figure out which declaration is working
    @Fields({ @Field(name = "name_auto", analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)),
            @Field(analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)) }) 
    public String getName() {
        if (parentInstitution != null) {
            return parentInstitution.getName() + " : " + name;
        }
        return name;
    }

    public String getProperName() {
        return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    @Transient
    @Field(name = "acronym", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class))
    public String getAcronym() {
        Pattern p = Pattern.compile(ACRONYM_REGEX);
        Matcher m = p.matcher(getName());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String toString() {
        return name;
    }

    public Institution getParentInstitution() {
        return parentInstitution;
    }

    public void setParentInstitution(Institution parentInstitution) {
        this.parentInstitution = parentInstitution;
    }

    /**
     * @param alternateNames
     *            the alternateNames to set
     */
    public void setAlternateNames(Set<String> alternateNames) {
        this.alternateNames = alternateNames;
    }

    /**
     * @return the alternateNames
     */
    @XmlTransient
    public Set<String> getAlternateNames() {
        return alternateNames;
    }

    @Override
    public CreatorType getCreatorType() {
        return CreatorType.INSTITUTION;
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(name);
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        return JSON_PROPERTIES;
    }

}
