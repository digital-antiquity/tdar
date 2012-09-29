package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.InvestigationType;

/**
 * $Id$
 * 
 * @author Matt Cordial
 * @version $Revision$
 */
@Component
public class InvestigationTypeDao extends KeywordDao<InvestigationType> {

    public InvestigationTypeDao() {
        super(InvestigationType.class);
    }
}
