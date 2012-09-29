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
@Table(name="other_keyword")
@XStreamAlias("otherKeyword")
@Indexed(index = "Keyword")
public class OtherKeyword extends UncontrolledKeyword.Base<OtherKeyword> {

    private static final long serialVersionUID = -6649756235199570108L;

}
