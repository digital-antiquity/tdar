package org.tdar.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.dao.keyword.SiteNameKeywordDao;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Service
@Transactional
public class SiteNameKeywordService extends KeywordService<SiteNameKeyword, SiteNameKeywordDao> {

	@Override
	protected SiteNameKeyword createNew() {
		return new SiteNameKeyword();
	}
}
