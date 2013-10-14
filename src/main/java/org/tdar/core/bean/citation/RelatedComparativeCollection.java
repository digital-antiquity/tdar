package org.tdar.core.bean.citation;

import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Relation.comparativeCollection (resource level; repeatable) - If
 * identifications were made using specific comparative collections, list them
 * here.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Entity
@Table(name = "related_comparative_collection")
@org.hibernate.annotations.Table(appliesTo = "related_comparative_collection", indexes = {
        @Index(name = "related_comparative_collection_resource_id_idx", columnNames = "resource_id")
})
public class RelatedComparativeCollection extends Citation {

    private static final long serialVersionUID = 7272722671720761334L;

    public RelatedComparativeCollection() {
    }

    public RelatedComparativeCollection(String text) {
        setText(text);
    }

}
