package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.TemporalKeyword;

/**
 * $Id$
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class TemporalKeywordDao extends KeywordDao<TemporalKeyword> {
    
    public TemporalKeywordDao() {
        super(TemporalKeyword.class);
    }

}
