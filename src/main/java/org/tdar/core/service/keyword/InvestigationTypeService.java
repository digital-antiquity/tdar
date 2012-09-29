package org.tdar.core.service.keyword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.dao.keyword.InvestigationTypeDao;

/**
 * $Id$
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Service
public class InvestigationTypeService extends KeywordService<InvestigationType, InvestigationTypeDao>{

    @Autowired
	public void setDao(InvestigationTypeDao dao) {
		super.setDao(dao);
	}

	
	@Override
	protected InvestigationType createNew() {
		return new InvestigationType();
	}

}
