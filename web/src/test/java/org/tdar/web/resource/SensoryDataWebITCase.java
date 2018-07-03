package org.tdar.web.resource;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

@RunWith(MultipleWebTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
public class SensoryDataWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    String SDOC_TITLE = "a sensory document";
    String SDOC_DESC = "a sensory document description ";
    // FIXME: move this to TestConstants
    public static final String SDOC_FIELD_TITLE = "sensoryData.title";
    public static final String SDOC_FIELD_DESCRIPTION = "sensoryData.description";
    public static String REGEX_SENSORY_DATA_VIEW = "\\/sensory-data\\/\\d+\\/(.+)$";
    HashMap<String, String> sensoryHash = new HashMap<String, String>();
    private boolean oldSensoryModel = false;

    @Test
    public void testCreateSensoryDocumentMinimal() {
        gotoPage("/sensory-data/add");
        setInput(SDOC_FIELD_TITLE, SDOC_TITLE);
        setInput("sensoryData.date", "1943");
        setInput(SDOC_FIELD_DESCRIPTION, SDOC_DESC);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        submitForm();
    }

    @Test
    public void testCreateSensoryDocumentComplete() {
        createSensoryDocument();
    }

    private void createSensoryDocument() {
        gotoPage("/sensory-data/add");
        // setInput("ticketId", "127");
        sensoryHash.put("projectId", "-1");
        // sensoryHash.put("status", "ACTIVE");
        sensoryHash.put("status", "DRAFT");
        sensoryHash.put("sensoryData.title", "some title");
        sensoryHash.put("sensoryData.monumentNumber", "ig88");
        sensoryHash.put("sensoryData.description", "a description goes here");
        sensoryHash.put("sensoryData.surveyDateBegin", "03/09/1974");
        sensoryHash.put("sensoryData.surveyDateEnd", "03/09/1975");
        sensoryHash.put("sensoryData.surveyConditions", "conditionsval");
        sensoryHash.put("sensoryData.date", "1943");
        sensoryHash.put("sensoryData.scannerDetails", "scannerdetails val");
        sensoryHash.put("sensoryData.companyName", "companyname val");
        sensoryHash.put("sensoryData.estimatedDataResolution", "12345");
        sensoryHash.put("sensoryData.totalScansInProject", "12345");
        sensoryHash.put("sensoryData.planimetricMapFilename", "planmap_filename");
        sensoryHash.put("sensoryData.controlDataFilename", "controldata_filename");
        sensoryHash.put("sensoryData.rgbDataCaptureInfo", "rgb datacap val");
        sensoryHash.put("sensoryData.finalDatasetDescription", "finaldatasetsarchive val");
        if (oldSensoryModel) {
            sensoryHash.put("sensoryDataScans[0].filename", "filename0");
            sensoryHash.put("sensoryDataScans[0].transformationMatrix", "matrix0");
            // sensoryHash.put("sensoryDataScans[0].monumentName", "objmon_name0");
            sensoryHash.put("sensoryDataScans[0].scanDate", "11/01/2002");
            sensoryHash.put("sensoryDataScans[0].pointsInScan", "0");
            sensoryHash.put("sensoryDataScans[0].scannerTechnology", "TIME_OF_FLIGHT");
            sensoryHash.put("sensoryDataScans[0].resolution", "123450");
            sensoryHash.put("sensoryDataScans[0].tofReturn", "First Return");
            sensoryHash.put("sensoryDataScans[0].cameraExposureSettings", "123450");
            sensoryHash.put("sensoryDataScans[0].scanNotes", "scan notes 0");
            sensoryHash.put("sensoryDataScans[1].filename", "filename1");
            sensoryHash.put("sensoryDataScans[1].transformationMatrix", "matrix1");
            // sensoryHash.put("sensoryDataScans[1].monumentName", "objmon_name1");
            sensoryHash.put("sensoryDataScans[1].scanDate", "11/03/1974");
            sensoryHash.put("sensoryDataScans[1].pointsInScan", "1");
            sensoryHash.put("sensoryDataScans[1].scannerTechnology", "TRIANGULATION");
            sensoryHash.put("sensoryDataScans[1].resolution", "123451");
            sensoryHash.put("sensoryDataScans[1].triangulationDetails", "12345");
            sensoryHash.put("sensoryDataScans[1].scanNotes", "123451");
            sensoryHash.put("sensoryDataImages[0].filename", "imgname0");
            sensoryHash.put("sensoryDataImages[0].description", "imgdesc0");
            sensoryHash.put("sensoryDataImages[1].filename", "imgname1");
            sensoryHash.put("sensoryDataImages[1].description", "imgdesc1");
        }

        sensoryHash.put("sensoryData.registeredDatasetName", "regdatasetname_val");
        sensoryHash.put("sensoryData.registrationErrorUnits", "12345");
        sensoryHash.put("sensoryData.finalRegistrationPoints", "12345");
        sensoryHash.put("sensoryData.preMeshDatasetName", "premeshdatasetname val");
        sensoryHash.put("sensoryData.preMeshPoints", "12345");
        sensoryHash.put("sensoryData.meshDatasetName", "meshdatasetname val");
        sensoryHash.put("sensoryData.meshTriangleCount", "12345");
        sensoryHash.put("sensoryData.meshAdjustmentMatrix", "adj_matrix val");
        sensoryHash.put("sensoryData.meshProcessingNotes", "additional processing notes val");
        sensoryHash.put("sensoryData.decimatedMeshDataset", "decimatedmeshname val");
        sensoryHash.put("sensoryData.decimatedMeshOriginalTriangleCount", "12345");
        sensoryHash.put("sensoryData.decimatedMeshTriangleCount", "12345");
        sensoryHash.put("coverageDates[0].dateType", "CALENDAR_DATE");
        sensoryHash.put("creditProxies[0].role", "CONTACT");
        sensoryHash.put("resourceNotes[0].type", "GENERAL");
        sensoryHash.put("sensoryData.pointDeletionSummary", "premeshPointDeletionSummary");

        // sensoryHash.put("confidential", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.turntableUsed", "true"); // setting checkbox/radio

        if (oldSensoryModel) {
            sensoryHash.put("sensoryDataScans[0].matrixApplied", "true"); // setting checkbox/radio
            sensoryHash.put("sensoryDataScans[1].matrixApplied", "true"); // setting checkbox/radio
        }

        sensoryHash.put("sensoryData.premeshOverlapReduction", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.premeshSmoothing", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.premeshSubsampling", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.premeshColorEditions", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.meshRgbIncluded", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.meshdataReduction", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.meshHolesFilled", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.meshSmoothing", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.meshColorEditions", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.meshHealingDespiking", "true"); // setting checkbox/radio
        sensoryHash.put("sensoryData.rgbPreservedFromOriginal", "true"); // setting checkbox/radio

        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // sensoryHash.put(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            sensoryHash.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        // fixme: use two hashes: both hashes feed setInput() but only use one for asserts
        for (String key : sensoryHash.keySet()) {
            setInput(key, sensoryHash.get(key));
        }
        submitForm();
        // confirm we landed on the view page
        String path = internalPage.getUrl().getPath().toLowerCase();
        logger.debug("path is:" + path);
        assertTrue("expecting to be on view page. Actual path:" + path + " \n" + getPageText(), path.matches(REGEX_SENSORY_DATA_VIEW));

        clickLinkWithText("edit");
        for (String key : sensoryHash.keySet()) {
            String val = sensoryHash.get(key);
            if (key.contains("Ids") || key.contains("upload") || val.toUpperCase().equals(val)) {
                continue;
            }

            if (key.contains("[0]") || key.contains("[1]")) {
                assertTextPresent(sensoryHash.get(key));
                continue;
            }
            assertTrue("element:" + key + " is set to:" + val, checkInput(key, val));
        }

        // modify some of the fields, make sure they changed
        sensoryHash.put("status", "DRAFT");
        sensoryHash.put("sensoryData.title", "some titlea");
        sensoryHash.put("sensoryData.monumentNumber", "ig88a");
        sensoryHash.put("sensoryData.description", "a description goes herea");
        sensoryHash.put("sensoryData.surveyDateBegin", "11/03/1974");
        sensoryHash.put("sensoryData.surveyDateEnd", "11/01/2002");
        sensoryHash.put("sensoryData.surveyConditions", "conditionsvala");
        sensoryHash.put("sensoryData.scannerDetails", "scannerdetails vala");
        sensoryHash.put("sensoryData.companyName", "companyname vala");
        sensoryHash.put("sensoryData.planimetricMapFilename", "planmap_filenamea");
        sensoryHash.put("sensoryData.controlDataFilename", "controldata_filenamea");
        sensoryHash.put("sensoryData.rgbDataCaptureInfo", "rgb datacap vala");
        sensoryHash.put("sensoryData.finalDatasetDescription", "finaldatasetsarchive vala");

        // remvove a scan and confirm it's gone
        HashMap<String, String> sensoryHash2 = new HashMap<String, String>();
        String removedName = null;
        if (oldSensoryModel) {
            HtmlInput htmlInput = (HtmlInput) htmlPage.getElementByName("sensoryDataScans[1].filename");
            removedName = htmlInput.getValueAttribute();
            sensoryHash2.put("sensoryDataScans[1].id", "");
            sensoryHash2.put("sensoryDataScans[1].filename", "");
            sensoryHash2.put("sensoryDataScans[1].transformationMatrix", "");
            // sensoryHash2.put("sensoryDataScans[1].monumentName", "");
            sensoryHash2.put("sensoryDataScans[1].scanDate", "");
            sensoryHash2.put("sensoryDataScans[1].matrixApplied", "");
            sensoryHash2.put("sensoryDataScans[1].pointsInScan", "");
            sensoryHash2.put("sensoryDataScans[1].scannerTechnology", "");
            sensoryHash2.put("sensoryDataScans[1].resolution", "");
            sensoryHash2.put("sensoryDataScans[1].triangulationDetails", "");
            sensoryHash2.put("sensoryDataScans[1].scanNotes", "");
            sensoryHash2.put("sensoryDataScans[1].tofReturn", "");
            sensoryHash2.put("sensoryDataScans[1].phaseFrequencySettings", "");
            sensoryHash2.put("sensoryDataScans[1].phaseNoiseSettings", "");
            sensoryHash2.put("sensoryDataScans[1].scanNotes", "");
            sensoryHash2.put("sensoryDataScans[1].cameraExposureSettings", "");
        }
        for (String key : sensoryHash.keySet()) {
            setInput(key, sensoryHash.get(key));
        }

        for (String key : sensoryHash2.keySet()) {
            try {
                HtmlElement input = getInput(key);
                logger.debug("removing: {}", input.asXml());
                getInput(key).remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.trace(getPageText());

        submitForm();
        path = internalPage.getUrl().getPath().toLowerCase();
        logger.debug("path is:" + path);
        assertTrue("expecting to be on view page. Actual path:" + path, path.matches(REGEX_SENSORY_DATA_VIEW));

        if (oldSensoryModel) {
            assertTextNotPresent(removedName);
        }
        assertTextPresent("some titlea");
        // confirm we landed on the view page
    }
}
