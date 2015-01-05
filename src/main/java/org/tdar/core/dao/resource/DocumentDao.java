package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Document;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class DocumentDao extends ResourceDao<Document> {

    public DocumentDao() {
        super(Document.class);
    }

}
