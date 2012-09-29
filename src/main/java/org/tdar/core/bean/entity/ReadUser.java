/**
 * 
 */
package org.tdar.core.bean.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.tdar.core.bean.resource.Resource;

/**
 * A team of users who have only reading authority.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Entity
@Table(name = "read_user")
public class ReadUser extends ResourceUser {

    private static final long serialVersionUID = -1639906473734803406L;

    public ReadUser() {
    }

    public ReadUser(Resource resource, Person person) {
        setResource(resource);
        setPerson(person);
    }

}
