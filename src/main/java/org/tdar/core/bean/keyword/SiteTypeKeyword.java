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
@Table(name="site_type_keyword")
@XStreamAlias("siteTypeKeyword")
@Indexed(index = "Keyword")
public class SiteTypeKeyword extends HierarchicalKeyword<SiteTypeKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = 4043710177198125088L;
    private boolean approved;
    
    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE},fetch=FetchType.LAZY,optional=true)
    private SiteTypeKeyword parent;

    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public SiteTypeKeyword getParent() {
    	return parent;
    }

    public void setParent(SiteTypeKeyword parent) {
    	this.parent = parent;
    }
}
