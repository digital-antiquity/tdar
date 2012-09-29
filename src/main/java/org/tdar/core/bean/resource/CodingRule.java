package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.tdar.core.bean.Persistable;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Id$
 */
@Entity
@Table(name = "coding_rule")
public class CodingRule extends Persistable.Base
        implements Comparable<CodingRule> {

    private static final long serialVersionUID = -577936920767925065L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coding_sheet_id")
    @ContainedIn
    @XStreamOmitField
    private CodingSheet codingSheet;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @Field
    private String term;

    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = sanitize(code);
    }

    // strips leading zeros and trims whitespace from string.
    private static String sanitize(String string) {
        if (string == null || string.isEmpty())
            return null;
        try {
            Integer integer = Integer.parseInt(string);
            string = String.valueOf(integer);
        } catch (NumberFormatException exception) {
            if (string != null) {
                string = string.trim();
            }
        }
        return string;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public CodingSheet getCodingSheet() {
        return codingSheet;
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.codingSheet = codingSheet;
    }

    public String toString() {
        return String.format("[%s], [%s], [%s]", code, term, description);
    }

    /**
     * Default implementation of compareTo using the code.
     */
    public int compareTo(CodingRule other) {
        try {
            // try to use integer comparison instead of String lexicographic comparison
            return Integer.valueOf(code).compareTo(Integer.valueOf(other.code));
        } catch (NumberFormatException exception) {
            return code.compareTo(other.code);
        }
    }
}
