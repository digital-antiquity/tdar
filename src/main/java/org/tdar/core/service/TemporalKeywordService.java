package org.tdar.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.dao.keyword.TemporalKeywordDao;


/**
 * $Id$
 *  
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional
@Service
public class TemporalKeywordService extends KeywordService<TemporalKeyword, TemporalKeywordDao> {
    
    protected TemporalKeyword createNew() {
        return new TemporalKeyword();
    }
    
}
