package org.tdar.core.service.keyword;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.dao.keyword.CultureKeywordDao;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional
@Service
public class CultureKeywordService 
extends HierarchicalKeywordService<CultureKeyword, CultureKeywordDao> 
implements SuggestedKeywordService<CultureKeyword>{

	@Autowired
	public void setDao(CultureKeywordDao dao) {
		super.setDao(dao);
	}

	@Override
	protected CultureKeyword createNew() {
		CultureKeyword key = new CultureKeyword();
		key.setApproved(false);
		key.setSelectable(false);
		return key;
	}

	@Override
	public List<CultureKeyword> findAllApproved() {
		return getDao().findAllByProperty("approved", true);
	}
    
}
