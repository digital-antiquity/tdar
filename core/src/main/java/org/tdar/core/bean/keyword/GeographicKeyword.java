package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;
import org.tdar.core.bean.FieldLength;

/**
 * $Id$
 * 
 * Spatial coverage - geographic or jurisdictional terms (e.g., city, county,
 * state/province/department, country).
 * 
 * See http://www.getty.edu/research/conducting_research/vocabularies/ and
 * http://geonames.usgs.gov/pls/gnispublic/
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Table(name = "geographic_keyword")
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.GeographicKeyword")
@Cacheable
public class GeographicKeyword extends UncontrolledKeyword.Base<GeographicKeyword> {

    private static final long serialVersionUID = 9120049059501138213L;

    public GeographicKeyword(String string) {
        this.setLabel(string);
    }
    
    public GeographicKeyword() {
    }


    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<GeographicKeyword> synonyms = new HashSet<GeographicKeyword>();

    public enum Level {
        CONTINENT("Continent"),
        COUNTRY("Country"),
        STATE("State / Territory"),
        COUNTY("County"),
        CITY("City"),
        FIPS_CODE("Fips Code");

        private String label;

        private Level(String label) {
            this.setLabel(label);
        }

        private void setLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

    }

    @Enumerated(EnumType.STRING)
    @Column(length = FieldLength.FIELD_LENGTH_50)
    private Level level;
    
    @Column(length=FieldLength.FIELD_LENGTH_5)
    private String code;

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * @return the level
     */
    @XmlAttribute
    public Level getLevel() {
        return level;
    }

    public static String getFormattedLabel(String label, Level level) {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append(label.trim()).append(" (").append(level.getLabel()).append(")");
        return toReturn.toString();
    }

    @Override
    public Set<GeographicKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<GeographicKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.GEOGRAPHIC_KEYWORD.getUrlNamespace();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
