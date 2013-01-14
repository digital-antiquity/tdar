package org.tdar.core.service.external;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

public interface Accessible {

    boolean canEdit(Person authenticatedUser, Persistable item);

    boolean canView(Person authenticatedUser, Persistable item);

}
