package org.tdar.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.dao.keyword.GeographicKeywordDao;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Service
@Transactional
public class GeographicKeywordService extends KeywordService<GeographicKeyword, GeographicKeywordDao> {
    
    public GeographicKeyword createNew() {
        return new GeographicKeyword();
    }

}
