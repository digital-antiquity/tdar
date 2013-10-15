package org.tdar.core.bean.citation;

import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents source collection reference-type annotations.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Entity
@Table(name = "source_collection")
@org.hibernate.annotations.Table(appliesTo = "source_collection", indexes = {
        @Index(name = "source_collection_resource_id_idx", columnNames = "resource_id")
})
public class SourceCollection extends Citation {
    private static final long serialVersionUID = 129719231908607137L;

}
