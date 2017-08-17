package org.tdar.core.service.external;

import java.util.List;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

/*
 * Interface between @link AbstractPersistableController and @link AuthenticationAndAuthorizationService to help govern access to view and edit pages
 */
public interface Accessible {

    boolean canEdit(TdarUser authenticatedUser, Persistable item);

    boolean canView(TdarUser authenticatedUser, Persistable item);

    List<Resource> findEditableResources(TdarUser knownPerson, boolean b, List<ResourceType> types);

}
