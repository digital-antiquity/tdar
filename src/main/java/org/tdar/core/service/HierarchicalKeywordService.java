package org.tdar.core.service;

import java.util.List;

import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.dao.keyword.HierarchicalKeywordDao;

/**
 * $Id$
 * 
 * @author <a href="matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public abstract class HierarchicalKeywordService<K extends HierarchicalKeyword<K>, D extends HierarchicalKeywordDao<K>> 
extends KeywordService<K, D> {
	
	public List<K> findAllDescendants(K keyword) {
		return getDao().findAllDescendants(keyword);
	}
	
}
