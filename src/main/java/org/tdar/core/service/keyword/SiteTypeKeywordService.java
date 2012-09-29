package org.tdar.core.service.keyword;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.dao.keyword.SiteTypeKeywordDao;

/**
 * $Id$
 * 
 * @author Matt Cordial
 * @version $Revision$
 */
@Service
public class SiteTypeKeywordService 
extends HierarchicalKeywordService<SiteTypeKeyword, SiteTypeKeywordDao>
implements SuggestedKeywordService<SiteTypeKeyword> {

    @Autowired
	public void setDao(SiteTypeKeywordDao dao) {
		super.setDao(dao);
	}
	
	@Override
	protected SiteTypeKeyword createNew() {
		SiteTypeKeyword key = new SiteTypeKeyword();
		key.setSelectable(false);
		key.setApproved(false);
		return key;
	}

	@Override
	public List<SiteTypeKeyword> findAllApproved() {
		return getDao().findAllByProperty("approved", true);
	}
}
