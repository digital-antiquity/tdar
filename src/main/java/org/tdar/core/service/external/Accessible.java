package org.tdar.core.service.external;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

/*
 * Interface between @link AbstractPersistableController and @link AuthenticationAndAuthorizationService to help govern access to view and edit pages
 */
public interface Accessible {

    boolean canEdit(Person authenticatedUser, Persistable item);

    boolean canView(Person authenticatedUser, Persistable item);

}
