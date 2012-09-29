package org.tdar.core.service.keyword;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.dao.keyword.OtherKeywordDao;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Service
@Transactional
public class OtherKeywordService extends KeywordService<OtherKeyword, OtherKeywordDao> {

	@Override
	protected OtherKeyword createNew() {
		return new OtherKeyword();
	}
	
}
