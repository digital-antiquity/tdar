package org.tdar.core.service.keyword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.dao.keyword.MaterialKeywordDao;

/**
 * $Id$
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Service
public class MaterialKeywordService extends KeywordService<MaterialKeyword, MaterialKeywordDao>{

    @Autowired
	public void setDao(MaterialKeywordDao dao) {
		super.setDao(dao);
	}
	
	@Override
	protected MaterialKeyword createNew() {
		return new MaterialKeyword();
	}

}
