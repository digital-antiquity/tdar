/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.resource.Project;

/**
 * @author Adam Brin
 * 
 */
public class ObfuscationServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ObfuscationService obfuscationService;

    private List<Long> authorizedUserIds = new ArrayList<Long>();

    @SuppressWarnings("unused")
    @Test
    @Rollback(false)
    public void testObfuscationService() {
        Project project = genericService.find(Project.class, 3805l);
        assertNotObfuscated(project);
        logger.debug("submitter: {} ", project.getSubmitter());
        logger.debug("{}", project.getSharedCollections());

        // setup a fake user on the resource collection (just in case)
        ResourceCollection internalResourceCollection = project.getInternalResourceCollection();
        Long collectionId = internalResourceCollection.getId();

        for (AuthorizedUser user : internalResourceCollection.getAuthorizedUsers()) {
            logger.debug("{}", user);
            authorizedUserIds.add(user.getId());
        }

        obfuscationService.obfuscate(project, null);
        logger.debug("submitter: {} ", project.getSubmitter());
        // test that the obfuscation is correct
        assertIsObfuscated(project);
        // remaining assertions occur in verifyPostObfuscationData to ensure that we are in a fresh new transaction.
    }

    @AfterTransaction
    public void verifyPostObfuscationData() {
        // These remaining assertions must occur within a new transaction to ensure that the obfuscation changes
        // didn't "stick" to the entity.
        runInNewTransaction(new TransactionCallback<Project>() {
            @Override
            public Project doInTransaction(TransactionStatus status) {
                Project project = genericService.find(Project.class, 3805L);
                assertNotObfuscated(project);
                return project;
            }

        });
    }

    private void assertNotObfuscated(Project project) {
        assertNotNull(project.getSubmitter().getEmail());
        assertNotNull(project.getUpdatedBy().getEmail());
        assertFalse(project.getSubmitter().isObfuscated());
        assertFalse(project.getUpdatedBy().isObfuscated());
        assertFalse(project.getFirstLatitudeLongitudeBox().isObfuscated());
    }

    private void assertIsObfuscated(Project project) {
        assertNull(project.getSubmitter().getEmail());
        assertNull(project.getUpdatedBy().getEmail());
        assertTrue(project.getSubmitter().isObfuscated());
        assertTrue(project.getUpdatedBy().isObfuscated());
        assertTrue(project.getFirstLatitudeLongitudeBox().isObfuscated());
    }
}
