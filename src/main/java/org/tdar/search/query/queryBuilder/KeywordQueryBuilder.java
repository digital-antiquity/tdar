package org.tdar.search.query.queryBuilder;

import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class KeywordQueryBuilder extends QueryBuilder {

	public KeywordQueryBuilder() {
		this.setClasses(new Class[] { CultureKeyword.class,
				GeographicKeyword.class, InvestigationType.class,
				MaterialKeyword.class, OtherKeyword.class,
				TemporalKeyword.class, SiteNameKeyword.class,
				SiteTypeKeyword.class, });
	}

}
