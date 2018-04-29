package org.tdar.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.search.service.query.RowSearchObject;

@Service
public class RowSearchService {

    @Transactional(readOnly=true)
    public List<RowResult> search(RowSearchObject rowSearchObect, TdarUser authenticatedUser) {
        // TODO Auto-generated method stub
        return null;
    }

    
}
