package org.tdar.core.bean.collection;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by jimdevos on 3/17/15.
 */
@Entity
@Table(name="whitelabel_collection")
@Inheritance(strategy = InheritanceType.JOINED)
public class WhitelabelCollection extends ResourceCollection{
    

}
