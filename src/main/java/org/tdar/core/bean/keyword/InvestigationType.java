package org.tdar.core.bean.keyword;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * $Id$
 * 
 *
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name="investigation_type")
@XStreamAlias("investigationType")
@Indexed(index = "Keyword")
public class InvestigationType extends ControlledKeyword.Base {

	private static final long serialVersionUID = 2557655317256194003L;

}
