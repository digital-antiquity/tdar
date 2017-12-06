package org.tdar.struts.action.collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.api.collection.AddNewCollectionAction;

public class AddNewCollectionControllerITCase extends AbstractControllerITCase {

	private static final String NEW_COLLECTION_NAME = "Test Collection: (Created by automation test)";

	@Test
	@Rollback
	public void testCreateNewValidCollection(){
		AddNewCollectionAction controller = getController();
		setProperties(controller, NEW_COLLECTION_NAME);
		createNewCollection(controller);
		Map<String, Object> result = getResult(controller);
		assertTrue("result is successful", result.get("status").equals("success"));
		assertNotNull("result contains managed resource", result.get("id"));
	}
	
	@Test
	@Rollback
	public void testAddCollectionWithBlankName(){
		AddNewCollectionAction controller = getController();
		setProperties(controller, "");
		prepareAndValidate(controller);
		logger.debug("Action errors: {}", controller.getActionErrors());
		assertTrue("An action error was thrown", CollectionUtils.isNotEmpty(controller.getActionErrors()));
	}
	
	@Test
	@Rollback
	public void testAddCollectionWithNameAsOnlySpaces(){
		AddNewCollectionAction controller = getController();
		setProperties(controller, "          ");
		prepareAndValidate(controller);
		logger.debug("Action errors: {}", controller.getActionErrors());
		assertTrue("An action error was thrown", CollectionUtils.isNotEmpty(controller.getActionErrors()));
	}
	
	@Test
	@Rollback
	public void testAddCollectionWithNullName(){
		AddNewCollectionAction controller = getController();
		controller.setCollectionName(null);
		prepareAndValidate(controller);
		logger.debug("Action errors: {}", controller.getActionErrors());
		assertTrue("An action error was thrown", CollectionUtils.isNotEmpty(controller.getActionErrors()));
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getResult(AddNewCollectionAction controller){
		Map<String, Object> result = (Map<String, Object>) controller.getResultObject();
		logger.debug("Result is : {}",result);
		return result;
	}

	private AddNewCollectionAction getController(){
		return generateNewInitializedController(AddNewCollectionAction.class);
	}

	private void createNewCollection(AddNewCollectionAction controller){
		prepareAndValidate(controller);
		try {
			ignoreActionErrors(true);
			controller.createNewResourceCollection();
		} catch (Exception e) {
			logger.debug("Stack trace: {}" ,e.getStackTrace());;
		}
	}
	private void prepareAndValidate(AddNewCollectionAction controller){
		try {
			ignoreActionErrors(true);
			controller.prepare();
			controller.validate();
		} catch (Exception e) {
				fail("An exception occurred: "+e.getMessage());
		}
	}
	
	private void setProperties(AddNewCollectionAction controller, String collectionName){
		controller.setCollectionName(collectionName);
	}
}
