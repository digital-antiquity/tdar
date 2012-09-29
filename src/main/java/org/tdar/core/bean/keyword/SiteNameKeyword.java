package org.tdar.core.bean.keyword;

import javax.persistence.Entity;
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
@Table(name="site_name_keyword")
@XStreamAlias("siteNameKeyword")
@Indexed(index = "Keyword")
public class SiteNameKeyword extends UncontrolledKeyword.Base<SiteNameKeyword> {

    private static final long serialVersionUID = 60750909588980398L;
    
}
