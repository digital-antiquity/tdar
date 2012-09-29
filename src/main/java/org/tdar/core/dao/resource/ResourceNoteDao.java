package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.dao.Dao;

/**
 * $Id$ 
 * <p>
 * Provides hibernate DAO access for ResourceNotes.
 *
 * @author Adam Brin
 * @version $Revision$
 */
@Component
public class ResourceNoteDao extends Dao.HibernateBase<ResourceNote> {

    public ResourceNoteDao() {
        super(ResourceNote.class);
    }

    public String getDefaultOrderingProperty() {
        return "timestamp";
    }

}
