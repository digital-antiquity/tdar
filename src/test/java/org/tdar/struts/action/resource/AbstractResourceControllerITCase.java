/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import org.tdar.struts.action.AbstractControllerITCase;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractResourceControllerITCase extends AbstractControllerITCase {
    /*
     * FIXME: figure out if we can load the resource within a new transaction.
     * otherwise since we're running within the same transaction Hibernate's first-level cache
     * will return the same resource that we saved initially as opposed to loading it again
     * within a new transaction for a new web request.
     */
    // @Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.SERIALIZABLE)

    public static void loadResourceFromId(AbstractResourceController<?> controller, Long id) {
        controller.setId(id);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        if (controller instanceof AbstractInformationResourceController) {
            ((AbstractInformationResourceController<?>) controller).loadInformationResourceProperties();
        }
    }
}
