package org.tdar.struts.action.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.api.integration.IntegrationPersistanceAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

public class DataIntegrationSaveITCase extends AbstractAdminControllerITCase {

    private String testJson = TestConstants.TEST_ROOT_DIR + "/data_integration_tests/json/test-integration.json";
    private String testInvalidJson = TestConstants.TEST_ROOT_DIR + "/data_integration_tests/json/test-invalid-integration-bad-id.json";
    private String testInvalidJsonBadFields = TestConstants.TEST_ROOT_DIR + "/data_integration_tests/json/test-invalid-integration-bad-column-fields.json";
    private String testInvalidJsonBadColType = TestConstants.TEST_ROOT_DIR + "/data_integration_tests/json/test-invalid-integration-bad-column-type.json";

    @Test
    @Rollback
    public void testBasicSave() throws Exception {
        IntegrationPersistanceAction action = setupBasic();
        assertTrue(action.getResult().getId() != null);
        assertTrue(CollectionUtils.isEmpty(action.getResult().getErrors()));
    }

    @Test
    @Rollback
    public void testSaveUpdate() throws Exception {
        IntegrationPersistanceAction action = setupBasic();
        Long id = action.getResult().getId();
        assertTrue(id != null);
        assertTrue(CollectionUtils.isEmpty(action.getResult().getErrors()));
        action = generateNewInitializedController(IntegrationPersistanceAction.class, getAdminUser());
        String integration = FileUtils.readFileToString(new File(testJson));
        action.setId(id);
        action.setIntegration(integration);
        action.prepare();
        String save = action.save();
        assertEquals(TdarActionSupport.SUCCESS, save);
        assertEquals(id, action.getResult().getId());
        assertTrue(CollectionUtils.isEmpty(action.getResult().getErrors()));
    }

    @Test
    @Rollback
    public void testUpdatePermissions() throws Exception {
        setIgnoreActionErrors(true);
        IntegrationPersistanceAction action = setupBasic();
        Long id = action.getResult().getId();
        assertTrue(id != null);
        assertTrue(CollectionUtils.isEmpty(action.getResult().getErrors()));
        action = generateNewInitializedController(IntegrationPersistanceAction.class, getBasicUser());
        String integration = FileUtils.readFileToString(new File(testJson));
        action.setId(id);
        action.setIntegration(integration);
        String msg = "";
        try {
            action.prepare();
            String save = action.save();
            assertNotEquals(TdarActionSupport.SUCCESS, save);
            assertFalse(CollectionUtils.isEmpty(action.getResult().getErrors()));
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertTrue(StringUtils.contains(msg, "does not have permissions"));
    }

    private IntegrationPersistanceAction setupBasic() throws IOException, Exception, TdarActionException, IntegrationDeserializationException {
        IntegrationPersistanceAction action = generateNewInitializedController(IntegrationPersistanceAction.class, getAdminUser());
        String integration = FileUtils.readFileToString(new File(testJson));
        action.setIntegration(integration);
        action.prepare();
        String save = action.save();
        assertEquals(TdarActionSupport.SUCCESS, save);
        return action;
    }

    @Test
    @Rollback
    public void testInvalidSave() throws Exception {
        setIgnoreActionErrors(true);
        IntegrationPersistanceAction action = generateNewInitializedController(IntegrationPersistanceAction.class, getAdminUser());
        String integration = FileUtils.readFileToString(new File(testInvalidJson));
        action.setIntegration(integration);
        action.prepare();
        action.validate();
        String save = action.save();
        assertEquals(TdarActionSupport.INPUT, save);
        assertTrue(action.getResult().getId() == null);
        logger.debug("errors: {}", action.getResult().getErrors());
        assertFalse(CollectionUtils.isEmpty(action.getResult().getErrors()));
    }

    @Test
    @Rollback
    public void testInvalidSaveBadColumnType() throws Exception {
        setIgnoreActionErrors(true);
        IntegrationPersistanceAction action = generateNewInitializedController(IntegrationPersistanceAction.class, getAdminUser());
        String integration = FileUtils.readFileToString(new File(testInvalidJsonBadColType));
        action.setIntegration(integration);
        action.prepare();
        String save = action.save();
        assertEquals(TdarActionSupport.INPUT, save);
        assertTrue(action.getResult().getId() == null);
        assertFalse(CollectionUtils.isEmpty(action.getResult().getErrors()));
    }

    @Test
    @Rollback
    public void testInvalidSaveBadField() throws Exception {
        setIgnoreActionErrors(true);
        IntegrationPersistanceAction action = generateNewInitializedController(IntegrationPersistanceAction.class, getAdminUser());
        String integration = FileUtils.readFileToString(new File(testInvalidJsonBadFields));
        action.setIntegration(integration);
        action.prepare();
        action.validate();
        String save = action.save();
        assertEquals(TdarActionSupport.INPUT, save);
        assertTrue(action.getResult().getId() == null);
        assertFalse(CollectionUtils.isEmpty(action.getResult().getErrors()));
    }
}
