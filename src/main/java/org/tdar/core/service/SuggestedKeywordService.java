package org.tdar.core.service;

import java.util.List;

import org.tdar.core.bean.keyword.SuggestedKeyword;

/**
 * $Id$
 * 
 * @author <a href="matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public interface SuggestedKeywordService<K extends SuggestedKeyword> {

	public List<K> findAllApproved();
	
}
