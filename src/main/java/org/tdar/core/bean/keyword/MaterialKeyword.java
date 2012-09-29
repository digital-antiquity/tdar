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
@Table(name="material_keyword")
@XStreamAlias("materialKeyword")
@Indexed(index = "Keyword")
public class MaterialKeyword extends ControlledKeyword.Base<MaterialKeyword> {
	
	private static final long serialVersionUID = -8439705822874264175L;
	
}
