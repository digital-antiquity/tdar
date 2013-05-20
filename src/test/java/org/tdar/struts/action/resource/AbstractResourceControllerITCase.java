/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.service.AccountService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.ResourceCreatorProxy;

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

    @Autowired
    AccountService accountService;

    public static void loadResourceFromId(AbstractResourceController<?> controller, Long id) throws TdarActionException {
        controller.setId(id);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        if (controller instanceof AbstractInformationResourceController) {
            ((AbstractInformationResourceController<?>) controller).loadInformationResourceProperties();
        }
    }

    public ResourceCreatorProxy getNewResourceCreator(String last, String first, String email, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        Person p = rcp.getPerson();
        rcp.getPerson().setLastName(last);
        rcp.getPerson().setFirstName(first);
        rcp.getPerson().setEmail(email);
        // id may be null
        rcp.getPerson().setId(id);
        Institution inst = new Institution();
        inst.setName("University of TEST");
        p.setInstitution(inst);
        rcp.setRole(role);
        return rcp;
    }

}
