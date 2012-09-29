package org.tdar.core.bean.citation;

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
public class RelatedComparativeCollection extends Citation {

    private static final long serialVersionUID = 7272722671720761334L;

}
