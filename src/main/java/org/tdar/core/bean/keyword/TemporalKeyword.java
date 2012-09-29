package org.tdar.core.bean.keyword;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * $Id$
 * 
 * Temporal term coverage
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "temporal_keyword")
@XStreamAlias("temporalKeyword")
@Indexed(index = "Keyword")
public class TemporalKeyword extends UncontrolledKeyword.Base<TemporalKeyword> {

    private static final long serialVersionUID = -626136232824053935L;

}
