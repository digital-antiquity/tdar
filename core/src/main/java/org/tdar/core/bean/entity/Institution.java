package org.tdar.core.bean.entity;

import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Validatable;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * $Id$
 * 
 * Records the relevant information regarding an institution.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "institution", indexes = {
        @Index(name = "institution_name_key", columnList = "name")
})
@DiscriminatorValue("INSTITUTION")
@XmlRootElement(name = "institution")
@Check(constraints = "email <> ''")
public class Institution extends Creator<Institution> implements Comparable<Institution>, Dedupable<Institution>, Validatable {

    private static final long serialVersionUID = 892315581573902067L;

    private static final String ACRONYM_REGEX = "(?:.+)(?:[\\(\\[\\{])(.+)(?:[\\)\\]\\}])(?:.*)";

    @Transient
    private static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "id", "dateCreated", "description", "dateUpdated", "url",
            "parentInstitution", "parentinstitution_id", "synonyms", "status", "occurrence", "browseOccurrence", "hidden" };

    @Column(nullable = false, unique = true)
    @BulkImportField(key = "CREATOR_INSTITUTION", order = 10)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Override
    public int compareTo(Institution candidate) {
        return name.compareTo(candidate.name);
    }

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH }, fetch = FetchType.LAZY, optional = true)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Institution parentInstitution;

    @Column(unique = true, nullable = true)
    @Length(min = 1, max = FieldLength.FIELD_LENGTH_255)
    private String email;

    public Institution() {
    }

    public Institution(String name) {
        this.name = name;
    }

    @Override
    @XmlElement
    public String getName() {
        return name;
    }

    @Override
    public String getProperName() {
        if (parentInstitution != null) {
            return parentInstitution.getName() + " : " + getName();
        }
        return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public String getAcronym() {
        Pattern p = Pattern.compile(ACRONYM_REGEX);
        Matcher m = p.matcher(getName());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Institution getParentInstitution() {
        return parentInstitution;
    }

    public void setParentInstitution(Institution parentInstitution) {
        this.parentInstitution = parentInstitution;
    }

    @Override
    public CreatorType getCreatorType() {
        return CreatorType.INSTITUTION;
    }

    public static String[] getIgnorePropertiesForUniqueness() {
        return IGNORE_PROPERTIES_FOR_UNIQUENESS;
    }

    @Override
    public Set<Obfuscatable> obfuscate() {
        return null;
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(name);
    }

    @Override
    public boolean isValid() {
        return isValidForController() && (getId() != null);
    }

    @Override
    public boolean hasNoPersistableValues() {
        if (StringUtils.isBlank(getName())) {
            return true;
        }
        return false;
    }

    @Override
    public Date getDateUpdated() {
        return super.getDateUpdated();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (StringUtils.isBlank(email)) {
            this.email = null;
        } else {
            this.email = email;
        }
    }

}
