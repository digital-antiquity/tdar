package org.tdar.core.bean.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.tdar.core.bean.resource.Resource;

/**
 * Users that have full authority over data resources.
 * 
 * FIXME: could use a better name.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Entity
@Table(name = "full_user")
public class FullUser extends ResourceUser {

    private static final long serialVersionUID = -7646077168408430709L;

    public FullUser() {
    }

    public FullUser(Resource resource, Person person) {
        setResource(resource);
        setPerson(person);
    }

}
