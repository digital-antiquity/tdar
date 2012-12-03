<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<@edit.title />


<meta name="lastModifiedDate" content="$Id$"/>

</head>
<body>
<@edit.sidebar /> 
<@edit.subNavMenu />
<h1>Editing:<span> <#if resource.title?has_content>${resource.title}<#else>New Sensory Data Object</#if> </span></h1>
<@s.form name='resourceMetadataForm' id='frmSensoryData'  cssClass="span9 form-horizontal" method='post' action='save' enctype='multipart/form-data'>
<@edit.basicInformation "sensory object" "sensoryData">
    <span tiplabel="Object / Monument Number" tooltipcontent="The ID number or code, if applicable, of the object or monument">
    <@s.textfield maxLength="255" name="sensoryData.monumentNumber" cssClass="input-xxlarge" label="Object / Monument #" labelposition="left" />
    </span>
</@edit.basicInformation>
<@edit.citationInfo "sensoryData" />

<@edit.asyncFileUpload "Sensory Data Files" true />

<@edit.allCreators 'Sensory Data Creators' authorshipProxies 'authorship' />

<div class="well-alt" id="divSurveyInfo">
    <h2>Survey Information</h2>
    <div class='control-group' tiplabel="Survey Date(s)" tooltipcontent="Date of survey, or date range of survey.">
    <@s.textfield label="Survey Begin" id="txtSurveyDateBegin" name="sensoryData.surveyDateBegin" cssClass="shortfield date formatUS" placeholder="mm/dd/yyyy" />
    <@s.textfield label="Survey End" id="txtSurveyDateEnd" name="sensoryData.surveyDateEnd" cssClass="right-shortfield date formatUS" placeholder="mm/dd/yyyy" />
<#-- FIXME: need to convert surveyDateEnd and surveyDateBegin to short forms when editing existing sensory data
value="<#if sensoryData.surveyDateEnd??><@view.shortDate sensoryData.surveyDateEnd /></#if>"
-->
    <!-- FIXME: why is this commented out?
    <span tiplabel="" tooltipcontent=""><@s.textfield maxLength="255" name="sensoryData.surveyLocation" cssClass="input-xxlarge" label="Survey Location" labelposition="left" title="Survey Location" /></span>
    -->
    <@s.textfield maxLength="255" name="sensoryData.surveyConditions" 
        tiplabel="Survey Conditions" tooltipcontent="The overall weather trend during survey (sunny, overcast, indoors, etc.)"
        cssClass="input-xxlarge" label="Conditions" labelposition="left" />
    <div tiplabel="Scanner Details" tooltipcontent="Details of the instrument(s) with serial number(s) and scan units">
    <@s.textfield maxLength="255" name="sensoryData.scannerDetails" cssClass="input-xxlarge" label="Scanner Details" labelposition="left" />
    </div>
    <div tiplabel="Company / Operator Name" tooltipcontent="Details of company and scan operator name">
    <@s.textfield maxLength="255" name="sensoryData.companyName" cssClass="input-xxlarge" label="Company Name" labelposition="left" />
    </div>
    <div tiplabel="Estimated Data Resolution" tooltipcontent="The estimated data resolution across the monument or object">
    <@s.textfield maxLength="255" name="sensoryData.estimatedDataResolution" cssClass="shortfield number" label="Data Resolution" labelposition="left" />
    </div>
    <div tiplabel="Total Number of Scans in Project" tooltipcontent="Total number of scans">
    <@s.textfield maxLength="255" name="sensoryData.totalScansInProject" cssClass="right-shortfield number" label="# Scans" labelposition="left" />
    </div>
    <div tiplabel="Turntable used" tooltipcontent="Check this box if a turntable was used for this survey.">
        <@s.checkbox  label="Turntable Used" name="sensoryData.turntableUsed"  id="cbTurntableUsed"  />
    </div>
    <div tiplabel="Planimetric Map Filename" tooltipcontent="If applicable, then provide the image name.">
    <@s.textfield maxLength="255" name="sensoryData.planimetricMapFilename" cssClass="reallyinput-xxlarge" label="Planimetric Map Filename" labelposition="top" />
    </div>
    <div tiplabel="Control Data Filename" tooltipcontent="If control data was collected, enter the control data filename.">
    <@s.textfield maxLength="255" name="sensoryData.controlDataFilename" cssClass="reallyinput-xxlarge" label="Control Data Filename" labelposition="top" />
    </div>
    <div tiplabel='RGB Data Capture Information' tooltipcontent="Please specify it is (1) internal or external and (2) describe any additional lighting systems used if applicable">
    <@s.textarea name="sensoryData.rgbDataCaptureInfo" id="rgbDataCaptureInfo" cssClass="resizable input-xxlarge" label="RGB Data Capture Information" labelposition="top" rows="5" />
    </div>
    <div tiplabel="Description of Final Datasets for Archive" tooltipcontent="What datasets will be archived (include file names if possible).">
        <@s.textarea name="sensoryData.finalDatasetDescription" cssClass="resizable input-xxlarge" label="Description of Final Datasets for Archive" labelposition="top" rows="5" />
    </div>
    </div>
</div>

<div class="well-alt" id="divScanInfo">
    <#assign _scans=sensoryDataScans />
    <#if _scans.isEmpty()>
    <#assign _scans=blankSensoryDataScan />
    </#if>  
    <#-- FIXME: this entire table needs to be reworked -->
    <h2>This Scan Information Table Needs To Be Reworked Entirely</h2>
    <table id="tblsensoryDataScans" class="repeatLastRow field inline-controls" addAnother="add another scan" callback="scanAdded">
        <tbody>
        <#list _scans as _scan>
        <tr id="sensoryDataScanRow_${_scan_index}_">
            <td class="enhancedTableRow">
            <div class="control-group">
                <div class="span4">
                    <span tiplabel="Scan Filename" tooltipcontent="The name of the scan. A suggested filename for original raw scans for archiving is in this format: ProjectName_scan1.txt.">
                    <@s.textfield maxLength="255" name="sensoryDataScans[${_scan_index}].filename" placeholder="Filename" cssClass="shortfield" />
                    </span>
                    <@s.hidden name="sensoryDataScans[${_scan_index}].id" />
                    <span tiplabel="Object / Monument Name" tooltipcontent="Name of monument or object being scanned">
                    <@s.textfield maxLength="255"    name="sensoryDataScans[${_scan_index}].monumentName" placeholder="Object / Monument Name" cssClass="shortfield" />
                    </span>
                </div>
                <div class="span4">
                    <#assign _scanDate="" />
                    <#if _scan.scanDate?? >
                      <#assign _scanDate><@view.shortDate _scan.scanDate!"" /></#assign>
                    </#if>
                    <span tiplabel="Scan Date" tooltipcontent="Date the object/monument was scanned"><@s.textfield maxLength="255"    name="sensoryDataScans[${_scan_index}].scanDate" value="${_scanDate}" placeholder="mm/dd/yyyy" cssClass=" date" /> </span>
                    <span tiplabel="Data Resolution"  tooltipcontent="Fixed resolution or data resolution at specific range.">
                        <@s.textfield maxLength="255"    name="sensoryDataScans[${_scan_index}].resolution" placeholder="Resolution" cssClass="number" />
                    </span>
                    <span tiplabel="Number of Points in Scan" tooltipcontent="Number of points generated in scan">
                    <@s.textfield maxLength="255" name="sensoryDataScans[${_scan_index}].pointsInScan" placeholder="# points" cssClass="shortfield number" />
                    </span>
                </div>
            </div>
            <div class="controls-row">
                <div class="span4">
                    <span tiplabel="Scan Transformation Matrix" tooltipcontent="The name of the transformation matrix used in Global Registration. Suggested file name: ProjectName_scan1_mtrx.txt">
                        <@s.textfield maxLength="255"    name="sensoryDataScans[${_scan_index}].transformationMatrix" placeholder="Transformation Matrix" cssClass="" />
                    </span>
    
                    <span class="checkboxLabel"  tooltipcontent="Check this box if transformation matrix has been applied to the archived scan">
                        <@s.checkbox label="Matrix Applied to Scan" name="sensoryDataScans[${_scan_index}].matrixApplied" id="cbMatrixApplied_${_scan_index}_" />
                    </span>
              </div>
          </div>
        <div class='well-alt'>
        <h2>Scanner Technology</h2>
        <@s.select  headerValue="" headerKey="" name='sensoryDataScans[${_scan_index}].scannerTechnology' listValue="label" list='%{scannerTechnologyTypes}' labelposition="left" cssClass="scannerTechnology shortfield" />
        <span class="scantech-fields-tof">
        <@s.select name="sensoryDataScans[${_scan_index}].tofReturn" emptyOption='true' list=['First Return','Last Return']
        label='Return Type' labelposition="left" cssClass="scantech-fields-tof" /></span>
        <@s.textfield maxLength="255" name="sensoryDataScans[${_scan_index}].phaseFrequencySettings" placeholder="Frequency Settings" 
        cssClass="shortfield scantech-fields-phase" />
        <@s.textfield maxLength="255" name="sensoryDataScans[${_scan_index}].phaseNoiseSettings" placeholder="Noise Settings" 
        cssClass="shortfield scantech-fields-phase" />
        <@s.textfield maxLength="255" name="sensoryDataScans[${_scan_index}].cameraExposureSettings" placeholder="Camera Exposure Settings" 
        cssClass="shortfield scantech-fields-phase scantech-fields-tof" />
        <@s.textfield maxLength="255"  name="sensoryDataScans[${_scan_index}].triangulationDetails" placeholder="Lens/FOV Details" 
        cssClass="shortfield scantech-fields-tri" />
        </div>
        <div class='well-alt'>
        <span tiplabel="Additional Scan Notes" tooltipcontent="Additional notes related to this scan">
        <@s.textarea name="sensoryDataScans[${_scan_index}].scanNotes" label="Scan Notes" labelposition="top" cssClass="resizable input-xxlarge" rows="5" />
        </span>
        </div>
            </td>
            <td class="enhancedTableRow">
                <@edit.clearDeleteButton id="sensoryDataScanRow" />
            </td>
        </tr>
        </#list>
        </tbody>
    </table>
</div>
<style>
 .enhancedTableRow {
    padding-bottom: 5px;
    border-bottom: 1px solid #333;
    padding-top: 5px;
}
</style>
<div class="well-alt" id="divImageInfo">
    <#assign _images=sensoryDataImages />
    <#if _images.isEmpty()>
    <#assign _images=blankSensoryDataImage />
    </#if>  
    <h2>Image Information</h2>
    <table id="tblsensoryDataImages" class="repeatLastRow inline-controls" addAnother="add another image">
        <thead>
            <tr>
                <th>Name</th>
                <th colspan="2">Description</th>
            </tr>
        </thead>
        <tbody>
            <#list _images as _image>
            <tr id="sensoryDataImagesRow_${_image_index}_" class="width99percent">
                
                <td class="">
                    <@s.hidden name="sensoryDataImages[${_image_index}].id" />
                    <@s.textfield maxLength="255" name="sensoryDataImages[${_image_index}].filename"  />
                </td>
                <td><@s.textfield maxLength="255" name="sensoryDataImages[${_image_index}].description" /></td>
                <td><@edit.clearDeleteButton id="sensoryDataImagesRow" /></td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<div tooltipfor="divImageInfo" class="hidden">
    <div>
        Use this section to specify information about reference images included in with this resource.
        <dl>
            <dt>Name</dt>
            <dd>The filename of the reference image</dd>
            <dt>Description</dt>
            <dd>Description of the image</dd>
        </dl>
    </div>    
</div>



<div class="well-alt" id="divRegistrationInfo">
    <h2>Registration Information</h2>
    <span tiplabel="Name of Registered Dataset" tooltipcontent="Filename for the dataset, a suggested naming structure for registered dataset for archiving: ProjectName_GR.txt">
        <@s.textfield maxLength="255" name="sensoryData.registeredDatasetName" label="Dataset Name" cssClass="input-xxlarge" labelposition="left" />
    </span>
    <br />
    <span tiplabel="Registration Method" tooltipcontent="Provide a brief description of the methods used to register the point cloud (e.g. 'Individual scans were aligned using N Point pairs').">
        <@s.textfield maxLength="255" name="sensoryData.registrationMethod" label="Reg. Method" cssClass="input-xxlarge" labelposition="left" />
    </span>
    <br />
    <span tiplabel="Registration Error" tooltipcontent="Total RMS error from global registration in scan units.">
        <@s.textfield maxLength="255" name="sensoryData.registrationErrorUnits" cssClass="shortfield number" label="Reg. Error" labelposition="left" />
    </span>
    <span tiplabel="Total Number of points In File" tooltipcontent="Total number of points in finalregistered point cloud">
        <@s.textfield maxLength="255" name="sensoryData.finalRegistrationPoints" cssClass="right-shortfield number" label="# Points in File" labelposition="left" />
    </span>
</div>



<div class="well-alt" id="divMeshIfno">
    <h2>Mesh Information</h2>

    <h4>Pre-mesh</h4>
    <span tiplabel="Name of Mesh Dataset" tooltipcontent="The filename, a suggested naming convention for the polygonal mesh dataset is *ProjectName_origmesh">
        <@s.textfield maxLength="255" name="sensoryData.preMeshDatasetName" cssClass="input-xxlarge" label="Dataset Name" labelposition="left" />
    </span>
    <br />
    <span tiplabel="Number of Points in File" tooltipcontent="Total number of points in the edited premesh point cloud">
        <@s.textfield maxLength="255" name="sensoryData.preMeshPoints" cssClass="shortfield number" label="# Points in File" labelposition="left" />
    </span>
    <br />
    <div class="control-group" tiplabel="Processing Operations" tooltipcontent="Check all the processing operations that apply">
        <label class="control-label">Processing Operations</label>
        <div class="controls">
        <@s.checkbox name="sensoryData.premeshOverlapReduction" cssClass="" label="Overlap Reduction"  />
        <@s.checkbox name="sensoryData.premeshSmoothing" cssClass="" label="Smoothing" />
        <@s.checkbox name="sensoryData.premeshSubsampling"  label="Subsampling"  />
        <@s.checkbox name="sensoryData.premeshColorEditions"  label="Color Editions"  />
        </div>
    </div>
    <span tiplabel="Point Editing Summary" tooltipcontent="Include a description of major editing operations (IE overlap reduction, point deletion, etc...) that have been performed on the dataset">
        <@s.textarea  name="sensoryData.pointDeletionSummary" cssClass="resizable input-xxlarge" label="Point Editing Summary" labelposition="top" rows="5" />
    </span>
    <br />
    
    
    <h4>Polygonal Mesh Metadata</h4>
    <span tiplabel="Name of Mesh Dataset" tooltipcontent="The filename, a suggested naming convention for the polygonal mesh dataset is *ProjectName_origmesh">
        <@s.textfield maxLength="255" name="sensoryData.meshDatasetName" cssClass="input-xxlarge" label="Dataset Name" labelposition="left" />
    </span>
    <br />
    <span tiplabel="Total Triangle Count (post editing, predecimation)" tooltipcontent="Total number of triangles in the mesh file">
        <@s.textfield maxLength="255" name="sensoryData.meshTriangleCount" cssClass="shortfield number" label="# Triangles" labelposition="left" />
    </span>
    <br />
    <span tiplabel="Coordinate System Adjustment" tooltipcontent="If present, the transformation matrix filename">
        <@s.textfield maxLength="255" name="sensoryData.meshAdjustmentMatrix" cssClass="input-xxlarge" label="Adj. Matrix" labelposition="left" />
    </span>
    <br />
    <div class="control-group" tiplabel="Processing Operations" tooltipcontent="Check all the processing operations that apply">
        <label class="control-label">Processing Operations</label>
        <div class="controls">
            <@s.checkbox name="sensoryData.meshRgbIncluded" cssClass="" label="RGB Color Included" />
            <@s.checkbox name="sensoryData.meshdataReduction" cssClass="" label="Data Reduction" />
            <@s.checkbox name="sensoryData.meshSmoothing" cssClass="" label="Smoothing" />
            <@s.checkbox name="sensoryData.meshHolesFilled" cssClass="" label="Holes Filled"  />
            <@s.checkbox name="sensoryData.meshColorEditions" cssClass="" label="Color Editions"  /> 
            <@s.checkbox name="sensoryData.meshHealingDespiking" cssClass="" label="Healing/De-spiking"  /> 
        </div>
    </div>
    <br />
    <@s.textarea name="sensoryData.meshProcessingNotes" cssClass="resizable input-xxlarge" label="Additional Processing Notes" labelposition="top" rows="5" />
    <br />
    
    <h4>Decimated Polygonal Mesh Metadata / Triangle Counts</h4>
    <span tiplabel="Name of Decimated Mesh Dataset" tooltipcontent="The file name, a suggested naming convention for the decimated polygonal mesh dataset is ProjectName_decimesh_50pcnt for decimated mesh e.g. by 50%.">
        <@s.textfield maxLength="255" name="sensoryData.decimatedMeshDataset" cssClass="input-xxlarge" label="Mesh Name" labelposition="left" />
    </span>
    <br />
    <span tiplabel="Total Original Triangle Count" tooltipcontent="Total Original Triangle Count">
        <@s.textfield maxLength="255" name="sensoryData.decimatedMeshOriginalTriangleCount" cssClass="shortfield number" label="# Original" labelposition="left" />
    </span>
    <span tiplabel="Decimated Triangle Count" tooltipcontent="Decimated Triangle Count">
        <@s.textfield maxLength="255" name="sensoryData.decimatedMeshTriangleCount" cssClass="right-shortfield number" label="# Decimated" labelposition="left" />
    </span>
    <br />
    <div class="well-alt" tiplabel="Processing Operations" tooltipcontent="Check all the processing operations that apply">
    <h4>Processing Operations</h4>
    <@s.checkbox name="sensoryData.rgbPreservedFromOriginal" cssClass="indent" label="RGB Color Included" labelposition="right" />
    </div>
</div>


<@edit.sharedFormComponents prefix="sensoryData"/>


</@s.form>
 
<@edit.asyncUploadTemplates />
<@edit.resourceJavascript formSelector="#frmSensoryData" selPrefix="#sensoryData" includeAsync=true includeInheritance=true />
<script>
$(function() {
    $('.scannerTechnology').each(
            function(i,elem){
                console.log('registering scanner tech select element');
                var scannerTechElem = elem;
                showScannerTechFields(scannerTechElem);
                $(scannerTechElem).change(function(){showScannerTechFields(scannerTechElem);});
            }
    );
});

</script>
 
</body>
</#escape>
