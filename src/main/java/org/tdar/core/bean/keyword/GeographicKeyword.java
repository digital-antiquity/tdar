package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.Length;

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
@Indexed(index = "Keyword")
public class GeographicKeyword extends UncontrolledKeyword.Base<GeographicKeyword> {

    private static final long serialVersionUID = 9120049059501138213L;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<GeographicKeyword> synonyms = new HashSet<GeographicKeyword>();

    public enum Level {
        CONTINENT("Continent"),
        COUNTRY("Country"),
        STATE("State / Territory"),
        COUNTY("County"),
        CITY("City"),
        FIPS_CODE("Fips Code"),
        ISO_COUNTRY("ISO Country Code");

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
    @Length(max = 50)
    private Level level;

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

    public Set<GeographicKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<GeographicKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

}
