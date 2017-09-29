package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Action;

@Transactional
public class LuceneExcelExportControllerITCase extends AbstractSearchControllerITCase {

    // the first few rows of the export have stats, column names, spacing, yada yada...
    private static final int EXCEL_EXPORT_HEADER_ROWCOUNT = 5;

    @Autowired
    SearchIndexService searchIndexService;

    private TdarUser currentUser = null;

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testExcelExport() throws InstantiationException, IllegalAccessException, ParseException, FileNotFoundException, IOException,
            InvalidFormatException, TdarActionException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        // currentUser = getBasicUser();
        AdvancedSearchDownloadAction controller = generateNewInitializedController(AdvancedSearchDownloadAction.class,
                genericService.find(TdarUser.class, getBasicUserId()));

        File tempFile = search(controller);

        Workbook workbook = WorkbookFactory.create(new FileInputStream(tempFile));
        Sheet sheet = workbook.getSheet("results");
        Assert.assertTrue(sheet.getLastRowNum() - EXCEL_EXPORT_HEADER_ROWCOUNT >  4);
    }
    
    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testExcelExportAdmin() throws InstantiationException, IllegalAccessException, ParseException, FileNotFoundException, IOException,
            InvalidFormatException, TdarActionException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        // currentUser = getBasicUser();
        AdvancedSearchDownloadAction controller = generateNewInitializedController(AdvancedSearchDownloadAction.class, genericService.find(TdarUser.class, getAdminUserId()));

        File tempFile = search(controller);

        Workbook workbook = WorkbookFactory.create(new FileInputStream(tempFile));
        Sheet sheet = workbook.getSheet("results");
        Assert.assertTrue(sheet.getLastRowNum() - EXCEL_EXPORT_HEADER_ROWCOUNT >  4);
    }

    private File search(AdvancedSearchDownloadAction controller) throws ParseException, TdarActionException, IOException, FileNotFoundException {
        controller.setServletRequest(getServletRequest());

        doSearch( controller,"");
        assertEquals(Action.SUCCESS, controller.viewExcelReport());
        assertFalse(controller.getSearchPhrase() + " should not have bold tag", controller.getSearchPhrase().toLowerCase().contains("<b>"));
        File tempFile = File.createTempFile("report", ".xls");
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        long copyLarge = IOUtils.copyLarge(controller.getInputStream(), fileOutputStream);

        fileOutputStream.close();
        logger.debug("tempFile: {}", tempFile);
        return tempFile;
    }

    @Test
    @Rollback(true)
    public void testExcelFailUnauthenticatedExport() throws InstantiationException, IllegalAccessException, ParseException, FileNotFoundException, IOException,
            TdarActionException {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        currentUser = null;
        AdvancedSearchDownloadAction controller = generateNewInitializedController(AdvancedSearchDownloadAction.class,
                genericService.find(TdarUser.class, getBasicUserId()));
        controller.setSessionData(new SessionData()); // create unauthenticated session
        getServletRequest().setAttribute("RequestURI", "http://www.test.com");
        // controller = generateNewInitializedController(AdvancedSearchController.class);

        controller.setServletRequest(getServletRequest());
        TdarActionException except = null;
        try {
            controller.viewExcelReport();
        } catch (TdarActionException e) {
            except = e;
        }
        assertNotNull(except);
        assertEquals(StatusCode.UNAUTHORIZED.getHttpStatusCode(), except.getStatusCode());
    }

    @Override
    public TdarUser getSessionUser() {
        return currentUser;
    }

}
