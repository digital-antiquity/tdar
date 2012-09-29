/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.SearchIndexService;

/**
 * @author Adam Brin
 * 
 */
public class ProjectDatatableControllerITCase extends AbstractAdminControllerITCase {

  @Autowired
  ProjectDatatableController controller;
  @Autowired
  SearchIndexService searchIndexService;

  public void initControllerFields() {
    searchIndexService.indexAll();
    controller.setIDisplayLength(10);
    controller.setIDisplayStart(0);
    controller.setSEcho(1);
    controller.setSSearch("");
    controller.setISortCol_0(0);
    controller.setSSortDir_0("asc");
    List<String> types = new ArrayList<String>();
    types.add("DOCUMENT");
    types.add("ONTOLOGY");
    types.add("CODING_SHEET");
    types.add("IMAGE");
    types.add("DATASET");
    controller.setTypes(types);
  }

  @Test
  public void testController() {
    initControllerFields();
    logger.info(controller.getAuthenticatedUser());
    controller.execute();
    BufferedReader reader = new BufferedReader(new InputStreamReader(controller.getJsonStream()));
    String line = "";
    StringBuffer json = new StringBuffer();
    try {
      while ((line = reader.readLine()) != null) {
        json.append(line);
      }
    } catch (Exception e) {
      // TODO: handle exception
    } finally {
      IOUtils.closeQuietly(reader);
    }
    logger.info(json);
    assertTrue(json.toString().contains("iTotalRecords"));
    assertTrue(json.toString().contains("HARP"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tdar.struts.action.AbstractControllerITCase#getController()
   */
  @Override
  protected TdarActionSupport getController() {
    return controller;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tdar.struts.action.AbstractAdminControllerITCase#getUserId()
   */
  @Override
  protected Long getUserId() {
    return 6L;
  }

}
