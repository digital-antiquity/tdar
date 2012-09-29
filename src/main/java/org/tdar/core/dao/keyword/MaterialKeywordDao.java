package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.MaterialKeyword;

/**
 * $Id$
 * 
 * @author Matt Cordial
 * @version $Revision$
 */
@Component
public class MaterialKeywordDao extends KeywordDao<MaterialKeyword> {

    public MaterialKeywordDao() {
        super(MaterialKeyword.class);
    }
}
