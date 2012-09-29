package org.tdar.core.bean.keyword;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "culture_keyword")
@XStreamAlias("cultureKeyword")
@Indexed(index = "Keyword")
public class CultureKeyword extends HierarchicalKeyword<CultureKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = -7196238088495993840L;
    private boolean approved;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = true)
    private CultureKeyword parent;

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(CultureKeyword parent) {
        this.parent = parent;
    }

    /**
     * @return the parent
     */
    public CultureKeyword getParent() {
        return parent;
    }
}